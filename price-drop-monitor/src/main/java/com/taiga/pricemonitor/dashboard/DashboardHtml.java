package com.taiga.pricemonitor.dashboard;

import com.taiga.pricemonitor.config.ProductConfig;

import java.util.List;

public class DashboardHtml {

    public static String render(List<ProductConfig> products) {
        StringBuilder cards = new StringBuilder();
        for (ProductConfig product : products) {
            String safeId = product.getUrl().replaceAll("[^a-zA-Z0-9]", "");
            cards.append("<div class=\"card\" data-url=\"").append(escape(product.getUrl())).append("\">")
                 .append("<h2>").append(escape(product.getName())).append("</h2>")
                 .append("<div class=\"price-info\">")
                 .append("<div class=\"current-price\" id=\"price-").append(safeId).append("\">Loading...</div>")
                 .append("<div class=\"alert\" id=\"alert-").append(safeId).append("\" style=\"display:none;\"></div>")
                 .append("</div>")
                 .append("<canvas id=\"chart-").append(safeId).append("\"></canvas>")
                 .append("</div>");
        }

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head>")
            .append("<title>Price Drop Monitor</title>")
            .append("<script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script>")
            .append("<style>")
            .append("* { box-sizing: border-box; }")
            .append("body { font-family: -apple-system, BlinkMacSystemFont, sans-serif; background: #f5f5f7; margin: 0; padding: 24px; color: #1d1d1f; }")
            .append("h1 { text-align: center; margin-bottom: 32px; }")
            .append(".container { max-width: 1000px; margin: 0 auto; }")
            .append(".card { background: white; border-radius: 12px; padding: 24px; margin-bottom: 24px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }")
            .append(".card h2 { font-size: 18px; margin: 0 0 12px 0; }")
            .append(".current-price { font-size: 28px; font-weight: 600; color: #1d1d1f; margin-bottom: 12px; }")
            .append(".alert { background: #ffebee; color: #c62828; padding: 12px; border-radius: 8px; font-weight: 600; margin-bottom: 16px; }")
            .append("canvas { max-height: 300px; }")
            .append(".last-updated { text-align: center; color: #86868b; font-size: 14px; margin-top: 32px; }")
            .append("</style></head><body>")
            .append("<div class=\"container\">")
            .append("<h1>Price Drop Monitor</h1>")
            .append(cards)
            .append("<p class=\"last-updated\" id=\"last-updated\"></p>")
            .append("</div>")
            .append("<script>")
            .append("async function loadData() {")
            .append("  const response = await fetch('/api/history');")
            .append("  const data = await response.json();")
            .append("  for (const url in data) {")
            .append("    const product = data[url];")
            .append("    const safeId = url.replace(/[^a-zA-Z0-9]/g, '');")
            .append("    const history = product.history;")
            .append("    if (history.length === 0) continue;")
            .append("    const latest = history[history.length - 1];")
            .append("    document.getElementById('price-' + safeId).textContent = '$' + latest.price.toFixed(2);")
            .append("    if (history.length >= 2) {")
            .append("      const previous = history[history.length - 2];")
            .append("      if (latest.price < previous.price) {")
            .append("        const dropPercent = ((previous.price - latest.price) / previous.price * 100).toFixed(2);")
            .append("        const alertEl = document.getElementById('alert-' + safeId);")
            .append("        alertEl.style.display = 'block';")
            .append("        alertEl.textContent = 'PRICE DROP: $' + previous.price.toFixed(2) + ' -> $' + latest.price.toFixed(2) + ' (' + dropPercent + '% off)';")
            .append("      }")
            .append("    }")
            .append("    const ctx = document.getElementById('chart-' + safeId).getContext('2d');")
            .append("    new Chart(ctx, {")
            .append("      type: 'line',")
            .append("      data: {")
            .append("        labels: history.map(h => new Date(h.checkedAt).toLocaleString()),")
            .append("        datasets: [{")
            .append("          label: 'Price',")
            .append("          data: history.map(h => h.price),")
            .append("          borderColor: '#0071e3',")
            .append("          backgroundColor: 'rgba(0, 113, 227, 0.1)',")
            .append("          tension: 0.2,")
            .append("          fill: true")
            .append("        }]")
            .append("      },")
            .append("      options: {")
            .append("        responsive: true,")
            .append("        plugins: { legend: { display: false } },")
            .append("        scales: { y: { beginAtZero: false, ticks: { callback: v => '$' + v } } }")
            .append("      }")
            .append("    });")
            .append("  }")
            .append("  document.getElementById('last-updated').textContent = 'Last loaded: ' + new Date().toLocaleString();")
            .append("}")
            .append("loadData();")
            .append("setInterval(() => location.reload(), 60000);")
            .append("</script>")
            .append("</body></html>");

        return html.toString();
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}