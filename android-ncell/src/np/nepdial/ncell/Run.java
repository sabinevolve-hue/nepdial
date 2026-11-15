package np.nepdial.ncell;

class Run implements Runnable {
    private final MainActivity host; private final String code;
    Run(MainActivity host, String code) { this.host = host; this.code = code; }
    public void run() { host.ussd(code); }
}
