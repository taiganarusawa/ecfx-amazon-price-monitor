# Design Doc — Price Drop Monitor

This document covers the three main tradeoffs I considered while building the project, plus a short list of known limitations and what I'd change at scale in the future.

---

## Tradeoff 1: SQLite vs PostgreSQL

**The decision:** Use SQLite for storage, and design it in a way that would allow an easy migration to PostgreSQL if needed.

**Why:** Since ECFX uses PostgreSQL, I was initially thinking about using it here, but given the scale and scope of this project, I decided that SQLite would do the same job. Since I am currently tracking only 3 products and the write load is very low, SQLite's simplicity and setup is a better fit. If I were to use PostgreSQL, it would require the reviewer to have a local Postgres instance running, which adds friction to getting the project up and running.

I wrote everything through the standard JDBC API instead of using SQLite-specific extensions, so if I ever needed to migrate to PostgreSQL, it would just be a driver swap and a connection string change. None of the queries themselves would need to change.

If this were running at 10x scale, like 30+ products with multiple workers writing at the same time, I would migrate to PostgreSQL. SQLite serializes writes (only one writer at a time), which becomes a bottleneck under load. I'd also want an index on `(product_url, checked_at)` since that matches how the dashboard queries the history table.
---

## Tradeoff 2: In-browser dashboard alert vs server-side email/Slack

**The decision:** Trigger notifications from the browser via a banner alert and a Web Notifications API desktop popup.

**Why:** Initially I considered server-side notifications, such as an email or Slack message, which would actually catch the user even when the dashboard isn't open. However, implementing that would require additional setup (email server credentials, Slack app configuration) that could create friction for reviewer trying to get the project running. The in-browser notifications are simpler to implement and test within the scope of this project.

The downside is that the browser needs to be running for desktop notifications to fire though the dashboard tab doesn't have to be focused or even visible. The browser can be minimized, on a different desktop, or behind other apps and the notification still pops up. The only real gap is if the user closes the browser entirely, then they won't get notified until they open the dashboard again.

**The cleaner production design** would be to have the server trigger notifications through a channel, such as an email or Slack message, which would alert the user even if they're not actively monitoring the dashboard. The dashboard could still have its own alerts for users who have it open, but the server-side notifications would be more reliable for catching drops in real-time.

---

## Tradeoff 3: Direct scraping vs the Amazon Product Advertising API

**The decision:** Used Jsoup to directly scrape the price from the product page.

**Why:** The Product Advertising API is the production-correct answer here since it's the official, ToS-compliant way to get pricing data. The only reason I didn't use it in this project is that it requires Amazon Associates approval, which can take days to get. Since this project is meant to be a quick demo, I decided to go with the direct scraping.

The only problem with direct scraping is that Amazon actively fights scrapers. During testing I logged real `ScraperException: Could not find product title` errors when their bot detection served a CAPTCHA page instead of the actual product page. The system handles this correctly (errors logged, scheduler continues, other products unaffected). To make it more transparent for the user, the dashboard surfaces a "scraper may be blocked" warning when the data is stale.

At a real production scale, I would migrate to the official API to avoid any legal or technical issues. The scraper layer is small and isolated in `AmazonScraper.java`, so swapping the implementation would only touch one file.

---

## Other notes

**Scheduling.** I used Java's built-in `ScheduledExecutorService` instead of pulling in Quartz or a similar library. For a single-instance app with simple fixed-rate scheduling, the standard library does everything I need. If this were running across multiple workers, then that would require a distributed scheduler like Quartz, since `ScheduledExecutorService` only works within a single JVM and doesn't coordinate across instances.

**Concurrency.** The scheduler submits each product check as its own task to a fixed thread pool, so the three products run in parallel within each cycle. A slow or failing scrape on one product can't block the others. Each task also has a catch-all `Exception` block, as an uncaught exception inside `ScheduledExecutorService` would silently kill all future runs of that task, which would mean the schedule would just stop without any signs anything went wrong.

**Threshold.** I set `priceDropThresholdPercent` to 10% by default. I initially set it to 5%, but during testing I thought about it from the user's perspective - a 5% drop on a $100 item is only $5, which might not be worth the user's attention. A 10% drop is more likely to be meaningful and worth alerting the user about. This is configurable in `config.yaml` so users can set it to whatever they want.

**Logging.** I configured Logback to suppress framework noise (Jetty, Spark, the SQLite driver) at WARN level so my application's INFO logs stay readable. Every price check, scheduler cycle, drop detection, and database save gets logged with enough context to debug just from the logs, which the brief explicitly called out.

---

## Known limitations

- **Amazon bot detection.** Covered above. The mitigation is to fail loudly and keep running.
- **No duplicate-notification guard.** If the app crashes after detecting a drop but before the next cycle saves the new price, the same drop could be flagged twice on restart. A `notifications_sent` table tracking `(product_url, price)` pairs would solve this. Listed as a stretch goal in the brief; left out for scope.
- **Browser-bound notifications.** Covered in Tradeoff 2. The fix is a server-side notifier channel.
- **No retry/backoff on scrape failures.** A failed scrape waits for the next scheduled cycle rather than retrying immediately. Acceptable at 30-minute cadence, would matter at higher frequencies.
