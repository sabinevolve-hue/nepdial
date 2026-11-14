# NepDial Android app

A WebView wrapper around `../index.html` (bundled as `assets/index.html`). `tel:` links are placed with
`ACTION_CALL`, so USSD codes run in one tap. Balance-transfer codes ask for confirmation first.

Built without Gradle: `aapt2 link` -> `javac` -> `d8` -> `zipalign` -> `apksigner` (SDK platform 34, min SDK 23).
The release signing key is kept privately by the owner and is not in this repo.
