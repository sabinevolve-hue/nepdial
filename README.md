# NepDial

Unofficial short-code helpers for Nepali mobile operators. Not affiliated with Ncell or Nepal Telecom.

| Path | What it is |
|---|---|
| `index.html` | NepDial: NTC and Ncell quick codes (web) |
| `ncell/` | Ncell app: live pack prices, data planner, all codes (web, installable) |
| `ncell/packs.json` | Pack catalogue, regenerated daily from ncell.com.np |
| `backend/fetch_packs.py` | The scraper that writes `packs.json` |
| `.github/workflows/packs.yml` | Runs the scraper every day and commits changes |
| `android/`, `android-ncell/` | Android wrappers (WebView; `tel:` links dial directly) |

## How the data backend works
There is no server. A scheduled GitHub Action runs `backend/fetch_packs.py`, which reads the
public Packs & Plans page, parses each pack card and writes `ncell/packs.json`. GitHub Pages serves
the file. If fewer than 10 packs parse (page layout changed), the job fails and the previous file stays.
The page also embeds a snapshot of the catalogue, so it works offline and before the first fetch.

## Rebuilding the Ncell page after a design change
`ncell/index.html` is self-contained. When packs change, only `packs.json` needs to change.

## Android builds
Built without Gradle: `aapt2 link` → `javac` → `d8` → `zipalign` → `apksigner` (platform 34, min SDK 23).
The signing key is held privately by the owner and is not in this repository.
