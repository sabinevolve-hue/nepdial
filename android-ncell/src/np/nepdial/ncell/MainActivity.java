package np.nepdial.ncell;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

/** NepDial: bundled page in a WebView; tel: links are placed as calls directly (USSD runs without the dialer). */
public class MainActivity extends Activity implements DialogInterface.OnClickListener {
    private static final int REQ_CALL = 1;
    private WebView web;
    private Uri pending;
    private Uri confirmUri;

    @Override
    public void onClick(DialogInterface d, int which) {
        if (confirmUri != null) { Uri u = confirmUri; confirmUri = null; dial(u); }
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        web = new WebView(this);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setUserAgentString(s.getUserAgentString() + " NepDialApp/1.0");
        web.setWebViewClient(new Client(this));
        setContentView(web);
        if (b != null) web.restoreState(b); else web.loadUrl("file:///android_asset/index.html");
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        web.saveState(out);
    }

    boolean handle(Uri u) {
        String scheme = u.getScheme() == null ? "" : u.getScheme();
        if (scheme.equals("tel")) { confirmThenDial(u); return true; }
        if (scheme.equals("file")) return false;
        try {
            startActivity(new Intent(scheme.equals("sms") ? Intent.ACTION_SENDTO : Intent.ACTION_VIEW, u));
        } catch (Exception e) {
            Toast.makeText(this, "No app can open this", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    /** Balance transfers move money, so they need an explicit confirmation before running. */
    private void confirmThenDial(final Uri u) {
        String code = Uri.decode(u.getSchemeSpecificPart());
        if (code.startsWith("*422*") || code.startsWith("*17122*")) {
            confirmUri = u;
            new AlertDialog.Builder(this)
                .setTitle("Send balance?")
                .setMessage("This will run " + code + " and transfer balance. Continue?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Send", this).show();
        } else {
            dial(u);
        }
    }

    private void dial(Uri u) {
        if (checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            try {
                startActivity(new Intent(Intent.ACTION_CALL, u));
            } catch (Exception e) {
                openDialer(u);
            }
        } else {
            pending = u;
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, REQ_CALL);
        }
    }

    private void openDialer(Uri u) {
        try { startActivity(new Intent(Intent.ACTION_DIAL, u)); }
        catch (Exception e) { Toast.makeText(this, "This device cannot make calls", Toast.LENGTH_SHORT).show(); }
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] perms, int[] res) {
        if (code != REQ_CALL || pending == null) return;
        Uri u = pending; pending = null;
        if (res.length > 0 && res[0] == PackageManager.PERMISSION_GRANTED) {
            dial(u);
        } else {
            Toast.makeText(this, "Allow phone permission for one-tap dialing", Toast.LENGTH_LONG).show();
            openDialer(u);
        }
    }
}
