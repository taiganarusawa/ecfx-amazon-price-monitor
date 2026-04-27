# Design Doc — Price Drop Monitor

This document covers the three main tradeoffs I considered while building the project, plus a short list of known limitations and what I'd change at scale.

---

## Tradeoff 1: SQLite vs PostgreSQL

**The decision:** Use SQLite for storage, with the design abstracted enough to migrate to PostgreSQL later.

**Why:** ECFX's production stack uses PostgreSQL, so the obvious answer would have been to use it here. But PostgreSQL requires a running server, credentials, and a database created in advance. For a take-home project where the priority is "a reviewer can clone the repo and run it in five minutes," that setup friction is real. SQLite is just a file. No server, no port, no password — the database is created automatically on first run.

I wrote everything through the standard JDBC API rather than SQLite-specific extensions, so the migration path to PostgreSQL is a driver swap and a connection string change. No schema or query rewrites would be needed for this project's queries.

**At 10x scale (30+ products, multiple writers):** I would migrate to PostgreSQL. SQLite's single-writer model becomes a contention point under concurrent writes, and the `price_history` table would benefit from a proper index on `(product_url, checked_at)` for the dashboard's history queries — SQLite indexes are fine, but Postgres handles concurrent index reads under write load much more gracefully.

---

## Tradeoff 2: In-browser dashboard alert vs server-side email/Slack

**The decision:** Trigger notifications from the browser via a banner alert and a Web Notifications API desktop popup, rather than implementing email or Slack on the server.

**Why:** The brief listed several notification options and explicitly said "anything that a reviewer can verify works." Email requires SMTP credentials, Slack requires a webhook URL — both add friction for the reviewer running the project for the first time. The dashboard banner plus desktop notification is verifiable instantly: open the page, grant notification permission, simulate a drop. No accounts, no API keys.

The cost of this choice is real and worth being honest about: the user has to have the dashboard open (in some tab) for desktop notifications to fire. If the browser is closed, the notification is missed. That's a meaningful gap.

**The cleaner production design** would be a `Notifier` interface with multiple implementations (`DashboardNotifier`, `EmailNotifier`, `SlackNotifier`), wired up via `config.yaml`. The dashboard one would still exist, but email would be added as a server-side channel that fires regardless of whether the browser is open. The current implementation has the right shape for this — `PriceCheckService` already logs at WARN when a drop is detected, so adding a server-side notifier would just be subscribing to that signal.

---

## Tradeoff 3: Direct scraping vs the Amazon Product Advertising API

**The decision:** Direct HTML scraping with Jsoup, after confirming with the hiring manager that this approach was acceptable.

**Why:** The Product Advertising API is the production-correct answer — it's the official, ToS-compliant way to get pricing data. But it requires Amazon Associates approval, which can take days. Within the 7-day window for this project, that's a non-trivial blocker.

I emailed the hiring manager early to flag the tension between the brief asking for Amazon products and Amazon's ToS prohibiting automated scraping. He confirmed that direct scraping was acceptable for the scope of this project. So I went with Jsoup, but with mitigations: a real Chrome User-Agent header, an explicit `Accept-Language`, a 10-second timeout, and a fallback chain of CSS selectors to handle Amazon's inconsistent HTML across product types.

This is the most fragile part of the system. Amazon actively fights scrapers — during testing I logged real `ScraperException: Could not find product title` errors when their bot detection served a CAPTCHA page instead of the product page. The system handles this correctly (errors logged, scheduler continues, other products unaffected), but it's a known weakness. The dashboard surfaces this transparently to the user with a "scraper may be blocked" warning when data is stale.

**At scale, I would migrate to the official API.** The scraper layer is small and isolated (`AmazonScraper.java`), so swapping the implementation would touch one file. Everything downstream — the comparator, scheduler, database, dashboard — is source-agnostic.

---

## Other notes

**Scheduling.** I used Java's built-in `ScheduledExecutorService` rather than introducing Quartz or a similar library. For a single-instance app with simple fixed-rate scheduling, the standard library is enough. Quartz adds value when you need cron expressions, persistent jobs across restarts, or distributed scheduling — none of which apply here. At scale across multiple workers, I'd revisit, since two instances running independent schedulers would duplicate work.

**Concurrency.** The scheduler submits each product check as its own task to a fixed thread pool, so the three checks run in parallel within each cycle. A slow scrape on one product can't block the other two. Each task has its own try/catch block including a catch-all `Exception`, because uncaught exceptions in a `ScheduledExecutorService` silently kill the scheduled task forever — a notorious gotcha.

**Threshold.** I set `priceDropThresholdPercent` to 10% by default. 5% triggers too easily on cheap items (a $1 fluctuation on a $20 bottle), and 20% almost never fires. 10% is the empirical sweet spot for "real sale, not noise." A more sophisticated v2 would support tier-based thresholds — for example, requiring both a percentage drop *and* a minimum absolute dollar drop — but the brief allowed either, and the percent-only model is simpler and configurable.

**Logging.** I configured Logback to suppress framework chatter (Jetty, Spark, SQLite driver) at WARN level so the application's own logs at INFO are readable. Every price check, scheduler cycle, drop detection, and database save is logged with enough context to debug from logs alone, which the brief explicitly called out.

---

## Known limitations

- **Amazon bot detection.** Covered above. The mitigation is to fail loudly and keep running.
- **No duplicate-notification guard.** If the app crashes after detecting a drop but before the next cycle saves the new price, the same drop could be flagged twice on restart. A `notifications_sent` table tracking `(product_url, price)` pairs would solve this. Listed as a stretch goal in the brief; left out for scope.
- **Browser-bound notifications.** Covered in Tradeoff 2. The fix is a server-side notifier channel.
- **No retry/backoff on scrape failures.** A failed scrape waits for the next scheduled cycle rather than retrying immediately. Acceptable at 30-minute cadence, would matter at higher frequencies.
- **Hardcoded port and timeout.** The dashboard port (4567) and scrape timeout (10s) are constants in code. They should arguably be in `config.yaml`. Minor scope cut.
