# AI-NOTES

This project was built with Claude assistance. This file documents one specific case where the AI's output was confidently wrong, and how I caught it.

---

## The bug: SQLite timestamp precision in `getLastPrice()`

Early in the build, I asked the AI to generate the database layer. It produced this query for fetching the most recent price for a product:

```sql
SELECT price FROM price_history
WHERE product_url = ?
ORDER BY checked_at DESC
LIMIT 1
```

It looked correct to me, so I accepted it and moved on. It passed my manual testing, which was just checking that a price got saved and could be retrieved.

The bug only surfaced when I wrote unit tests. One test inserted three records back-to-back at $50, $45, then $40, and asked for the most recent. It returned **$50**, the *first* one, not the last.

## Why it failed

SQLite's `CURRENT_TIMESTAMP` only has **second-level precision**. My test inserted three rows in the same millisecond, so all three got the same `checked_at` value. The `ORDER BY checked_at DESC` had no way to distinguish them, and SQLite returned them in an undefined order.

In production this would almost never happen, as checks are 30 minutes apart, so timestamps are always unique. But on a single check cycle where multiple products get saved within the same second, or in any test that inserts records back-to-back, this query is non-deterministic. Claude initially didn't mention this. If I hadn't written a test that inserted records rapidly, this would have shipped and I might never have caught it, since the real-world conditions that expose it are pretty rare.

## The fix

Add a tiebreaker on the auto-incrementing `id` column, which is guaranteed to be unique and monotonically increasing:

```sql
SELECT price FROM price_history
WHERE product_url = ?
ORDER BY checked_at DESC, id DESC
LIMIT 1
```

I applied the same fix to `getPriceHistory()` for consistency.

## Smaller things the AI got wrong

**The dashboard HTML and `StringBuilder`.** When I asked for the dashboard layer, Claude generated an HTML page using a long chain of `.append()` calls in Java, building the entire page string-by-string. It worked, but the code was nearly unreadable, and creating a separate HTML file would have been clearer. I had to push back specifically on the readability before Claude suggested moving the HTML to `src/main/resources/dashboard.html` and loading it as a static file. The lesson here is that AI can generate working code that fulfills the requirements but isn't necessarily the most maintainable or readable way to do it. I'll need to make sure to review all of the code it generates for readability and maintainability, not just correctness.

**Wrong claim about desktop notifications.** While testing notifications, I noticed they weren't firing when I was actively on the dashboard. Claude told me confidently that this was browser default behavior that notifications are suppressed when the page is the focused tab. I almost wrote that into my design doc as a known limitation, but when I actually tested it by switching to a different app, the notifications worked fine. The lesson here is that AI can confidently assert incorrect information about how third-party libraries or browser APIs work, so it is always important to verify them through testing or documentation to make sure that is actually how they behave.

## What I took from this

After creating this project with the help of AI, I learned that AI-generated code can be subtly wrong in a way that doesn't surface until specific conditions hit it. The query was syntactically correct and made sense when checking it first, and it worked fine under most real world conditions. The only thing that exposed the bug was a test that actually verified what the function was supposed to do rather than just checking that the code ran without errors. This was similar for the dashboard HTML and the notification behavior as well. The common note I'm taking from this is to always write unit tests along with testing manually to verify that the code is actually doing what it's supposed to do, and to verify any claims about external behavior instead of just trusting the explanation.