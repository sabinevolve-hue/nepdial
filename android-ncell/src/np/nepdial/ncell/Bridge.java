package np.nepdial.ncell;

import android.webkit.JavascriptInterface;

/** The only native call exposed to the bundled page: run a read-only USSD code. */
class Bridge {
    private final MainActivity host;
    Bridge(MainActivity host) { this.host = host; }

    @JavascriptInterface
    public void ussd(final String code) {
        host.runOnUiThread(new Run(host, code));
    }
}
