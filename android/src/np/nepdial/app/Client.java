package np.nepdial.app;

import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/** Routes every link tap to MainActivity so tel: links dial directly. */
class Client extends WebViewClient {
    private final MainActivity host;
    Client(MainActivity host) { this.host = host; }

    @Override
    public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
        return host.handle(r.getUrl());
    }
}
