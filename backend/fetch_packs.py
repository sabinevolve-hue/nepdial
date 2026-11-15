#!/usr/bin/env python3
"""Fetch Ncell's public Packs & Plans page and write ncell/packs.json.

Runs on a schedule in GitHub Actions (see .github/workflows/packs.yml). The page embeds its
content as HTML inside __NEXT_DATA__; packs are `.plan-card` blocks grouped in `.tab-panel`s.
If the page shape changes and fewer than MIN_PACKS are found, exit non-zero and keep the old file.
"""
import datetime, html, json, re, sys, urllib.request

URL = "https://www.ncell.com.np/en/individual/packs-and-plans"
OUT = sys.argv[1] if len(sys.argv) > 1 else "ncell/packs.json"
MIN_PACKS = 10
UA = "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Mobile Safari/537.36"


def text(s):
    return re.sub(r"\s+", " ", html.unescape(re.sub(r"<[^>]+>", " ", s))).strip()


def gb(value):
    """'7 GB' -> 7.0, '500 MB' -> 0.5, 'Unlimited' -> None"""
    m = re.search(r"([\d.]+)\s*(GB|MB)", value or "", re.I)
    if not m:
        return None
    n = float(m.group(1))
    return round(n / 1024, 3) if m.group(2).upper() == "MB" else n


def main():
    req = urllib.request.Request(URL, headers={"User-Agent": UA})
    page = urllib.request.urlopen(req, timeout=40).read().decode("utf-8", "ignore")
    data = json.loads(re.search(r'<script id="__NEXT_DATA__"[^>]*>(.*?)</script>', page, re.S).group(1))
    content = re.sub(r"<style.*?</style>", "", data["props"]["pageProps"]["item"]["content_en"], flags=re.S)

    labels = [text(x) for x in re.findall(r'<label class="tab-label[^"]*"[^>]*>(.*?)</label>', content, re.S)]
    panels = re.split(r'<div class="tab-panel panel\d+">', content)[1:]
    packs = []
    for i, panel in enumerate(panels):
        tab = labels[i] if i < len(labels) else "Other"
        # a panel can hold several headed sections; remember the latest heading before each card
        pos = 0
        for card in re.finditer(r'<div class="plan-card">(.*?)(?=<div class="plan-card">|<div class="section-heading">|$)', panel, re.S):
            heads = re.findall(r"<h[23][^>]*>(.*?)</h[23]>", panel[: card.start()], re.S)
            body = card.group(1)
            name = re.search(r'class="plan-name"[^>]*>(.*?)</div>', body, re.S)
            price = re.search(r'class="amount"[^>]*>\s*([\d,\.]+)', body)
            validity = re.search(r'class="plan-validity"[^>]*>(.*?)</div>', body, re.S)
            feats = {text(k): text(v) for k, v in re.findall(r"<li>\s*<span>(.*?)</span>\s*<span class=\"feat-val\">(.*?)</span>", body, re.S)}
            links = re.findall(r'<a class="btn-action" href="([^"]+)"[^>]*>(.*?)</a>', body, re.S)
            if not name or not price:
                continue
            v = text(validity.group(1)) if validity else ""
            v = v or feats.get("Validity", "")
            days = re.search(r"(\d+)\s*Day", v, re.I)
            hours = re.search(r"(\d+)\s*Hour", v, re.I)
            packs.append({
                "tab": tab,
                "section": (lambda h: h if h and not h.startswith(("\"", "“")) and len(h) < 34 and "!" not in h else tab)(text(heads[-1]).rstrip(":") if heads else ""),
                "name": text(name.group(1)),
                "price": float(price.group(1).replace(",", "")),
                "validity": v.replace("Valid for ", ""),
                "days": int(days.group(1)) if days else (round(int(hours.group(1)) / 24, 2) if hours else None),
                "data_gb": gb(feats.get("Data") or feats.get("Data Volume") or feats.get("Resources") or feats.get("Resource")),
                "data_text": feats.get("Data") or feats.get("Data Volume") or "",
                "fup_gb": gb(feats.get("FUP")),
                "features": feats,
                "web": next((u for u, t in links if "Website" in t), None),
                "app": next((u for u, t in links if "App" in t), None),
            })
    if len(packs) < MIN_PACKS:
        sys.exit(f"Only {len(packs)} packs parsed; page layout probably changed. Keeping the previous file.")
    out = {"source": URL, "fetched": datetime.datetime.now(datetime.timezone.utc).strftime("%Y-%m-%dT%H:%MZ"), "tabs": labels, "packs": packs}
    json.dump(out, open(OUT, "w", encoding="utf-8"), ensure_ascii=False, indent=1)
    print(f"{len(packs)} packs -> {OUT}")


if __name__ == "__main__":
    main()
