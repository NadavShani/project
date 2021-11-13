import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HttpsURLConnection;

public class client2 {

    /**
     * @param args
     */
    public static void main(String[] args) {
        client2 tester = new client2();
        try {
            tester.testConnectionTo("https://51.116.128.233:8443/test");
           // tester.testConnectionTo("https://google.com");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public client2() {
        super();
    }

    public void testConnectionTo(String aURL) throws Exception {
        URL destinationURL = new URL(aURL);
        HttpsURLConnection conn = (HttpsURLConnection) destinationURL
                .openConnection();
        conn.setDoOutput(true);
        conn.connect();


       // String s = new String(conn.getInputStream().readAllBytes());
      //  System.out.println(s);

        byte [] msg = new byte[11];
        msg = "hello world".getBytes();
       // conn.getOutputStream().write(msg);

/*
        Certificate[] certs = conn.getServerCertificates();
        for (Certificate cert : certs) {
            System.out.println("Certificate is: " + cert);
            if(cert instanceof X509Certificate) {
                try {
                    ( (X509Certificate) cert).checkValidity();
                    System.out.println("Certificate is active for current date");
                } catch(CertificateExpiredException cee) {
                    System.out.println("Certificate is expired");
                }
            }
        }

 */
    }


}