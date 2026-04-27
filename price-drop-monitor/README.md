# Price Drop Monitor

A Java application that monitors Amazon product prices on a schedule, persists the history, and alerts the user when a meaningful price drop is detected.

---

## What it does

- Scrapes prices for a configurable list of Amazon products on a fixed interval
- Persists every check to a local SQLite database
- Detects price drops above a configurable threshold and surfaces them through a web dashboard with a banner alert and a desktop notification
- Visualizes price history per product as a line chart
- Logs every check, save, and drop detection so the system can be debugged from the logs alone

---

## Stack

| Layer | Choice |
|---|---|
| Language | Java 18 |
| Build | Maven |
| Scraping | Jsoup |
| Storage | SQLite (via JDBC) |
| Web server | Spark Java |
| Charts | Chart.js (CDN) |
| Logging | SLF4J + Logback |
| Tests | JUnit 5 |

---

## Requirements

- Java 18 or higher
- Maven 3.8+
- A modern browser (for the dashboard)

---

## Setup

```bash
git clone https://github.com/taiganarusawa/price-drop-monitor.git
cd price-drop-monitor
mvn clean install
```

---

## Configuration

All runtime configuration lives in `config.yaml` at the project root. Add or remove products by editing this file, so no code changes needed.

```yaml
products:
  - name: "Sony WH-1000XM5 Headphones"
    url: "https://www.amazon.com/dp/B09XS7JWHH"
  - name: "ThermoFlask Water Bottle"
    url: "https://www.amazon.com/dp/B00JEZ20GO"
  - name: "Logitech G Pro Keyboard"
    url: "https://www.amazon.com/dp/B07QQB9VCV"

checkIntervalMinutes: 30
priceDropThresholdPercent: 10.0

dashboardPort: 4567
scraperTimeoutSeconds: 10

notification:
  method: "dashboard"
```

| Field | Description |
|---|---|
| `products` | List of products to track. Each needs a name and an Amazon product URL. |
| `checkIntervalMinutes` | How often to scrape prices. The dashboard refreshes on the same interval. |
| `priceDropThresholdPercent` | Minimum percentage drop required to trigger a notification. |
| `notification.method` | Currently only `dashboard` is supported. |
| `dashboardPort` | Port for the web dashboard. |
| `scraperTimeoutSeconds` | How long to wait for the scraper before giving up. |

---

## Running

```bash
mvn exec:java
```

You should see logs like:

```
INFO  c.t.p.config.ConfigLoader - Configuration loaded successfully from config.yaml
INFO  c.t.p.db.DatabaseService - Database initialized successfully
INFO  c.t.p.service.Scheduler - Starting scheduler with interval of 30 minutes
INFO  c.t.p.service.PriceCheckService - Checking product: Sony WH-1000XM5 Headphones
INFO  c.t.p.db.DatabaseService - Price saved for ... $248.0
INFO  c.t.p.dashboard.DashboardServer - Dashboard server running at http://localhost:4567
```

Open **`http://localhost:4567`** in your browser to see the dashboard.

To stop the app, press `Ctrl+C`. The scheduler shuts down gracefully and the dashboard server stops.

---

## How to verify it works end to end

The application will scrape prices on the configured interval, but you don't need to wait for a real Amazon price drop to verify the notification system. Two ways to verify:

### 1. Verify scraping and persistence

Run the app and let one check cycle complete (about 5 seconds for the initial scrape). Then:

- Open the dashboard at `http://localhost:4567`, you should see three product cards with current prices and charts
- Inspect `prices.db` with any SQLite client (DB Browser for SQLite, the SQLite Viewer VS Code extension) and you can view each row with its URL, name, price, and timestamp.

### 2. Verify drop detection and notification

Easiest way to simulate a drop without waiting on Amazon:

1. Stop the app
2. Open `prices.db` in DB Browser for SQLite
3. Insert an inflated price record so the next real check looks like a drop:
   ```sql
   INSERT INTO price_history (product_url, product_name, price)
   VALUES ('https://www.amazon.com/dp/B09XS7JWHH', 'Sony Headphones (test)', 999.99);
   ```
4. Save changes and close DB Browser
5. Restart the app
6. On the next check cycle, the dashboard will show:
   - A red **PRICE DROP** banner on the affected product card
   - A **desktop notification** (if you granted permission)
   - A `WARN`-level log line: `PRICE DROP DETECTED for ...`

---

## Running the tests

```bash
mvn test
```

There are 14 tests across 4 layers — comparison logic, database, scraper parsing, and dashboard rendering. The scraper tests use static HTML so they don't depend on the network.

---

## Architecture

```
config.yaml
    |
    v
Scheduler  --(every N minutes)-->  PriceCheckService
                                        |
                                        v
                              [scrape -> compare -> save]
                                        |
                                        v
                                  SQLite (prices.db)
                                        ^
                                        |
                                  DashboardServer  -->  http://localhost:4567
                                                        (charts + alerts)
```

Each layer is decoupled and lives in its own package: `config`, `db`, `scraper`, `comparison`, `service`, `dashboard`.

---

## Project layout

```
price-drop-monitor/
├── src/
│   ├── main/
│   │   ├── java/com/taiga/pricemonitor/
│   │   │   ├── config/         YAML config loading
│   │   │   ├── db/             SQLite persistence
│   │   │   ├── scraper/        Jsoup-based Amazon scraper
│   │   │   ├── comparison/     Price drop detection logic
│   │   │   ├── service/        Scheduler and check orchestration
│   │   │   ├── dashboard/      Spark web server and HTML
│   │   │   └── App.java        Entry point
│   │   └── resources/
│   │       ├── dashboard.html  Dashboard UI (Chart.js + vanilla JS)
│   │       └── logback.xml     Logging configuration
│   └── test/                   Mirrors main package structure
├── config.yaml                 Runtime configuration
├── design-doc.md               Tradeoff discussion
├── AI-NOTES.md                 Notes on AI-assisted development
├── pom.xml
└── README.md
```

---

## Design notes, tradeoffs, and known limitations

See `design-doc.md` for the three main tradeoffs I considered (storage, notification, scraping) along with known limitations and what I'd change at scale.

See `AI-NOTES.md` for an honest account of one bug the AI generated that my tests caught.