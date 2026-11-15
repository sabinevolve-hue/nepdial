package np.nepdial.ncell;

import android.telephony.TelephonyManager;

/** Android 8+ callback carrying the operator's one-shot USSD answer. */
class UssdReply extends TelephonyManager.UssdResponseCallback {
    private final MainActivity host; private final String code;
    UssdReply(MainActivity host, String code) { this.host = host; this.code = code; }

    @Override
    public void onReceiveUssdResponse(TelephonyManager tm, String request, CharSequence response) {
        host.reply(code, String.valueOf(response), true);
    }

    @Override
    public void onReceiveUssdResponseFailed(TelephonyManager tm, String request, int failureCode) {
        host.ussdFailed(code);
    }
}
