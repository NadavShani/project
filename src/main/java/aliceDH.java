import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.*;
import java.security.interfaces.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import javax.crypto.interfaces.*;
import com.sun.crypto.provider.SunJCE;

/* bob is server */
public class aliceDH {


    private KeyFactory aliceKeyFac;
    private KeyPairGenerator aliceKpairGen;
    private KeyPair aliceKpair;
    private KeyAgreement aliceKeyAgree;
    private X509EncodedKeySpec x509KeySpec;
    private byte[] alicePubKeyEnc; /* bob public */
    private byte[] aliceSharedSecret; /* bob secret */

    public aliceDH(){
        aliceKeyFac = null;
        aliceKpairGen = null;
        aliceKpair = null;
        aliceKeyAgree = null;
        x509KeySpec = null;
        alicePubKeyEnc = null;
        aliceSharedSecret = null;



    }

    protected byte [] getSecret() {
        return this.aliceSharedSecret;
    }

    public byte[] getPublicKey(){
        return alicePubKeyEnc;
    }

    public void start()  {
        LogPanel.logEvent("ALICE: Starting DH ...");
        try {
            generatePublicKeyFrom();
        }
        catch(NoSuchAlgorithmException | InvalidKeySpecException | InvalidAlgorithmParameterException | InvalidKeyException e){
            e.printStackTrace();
        }
    }

    public void generateSecret(){
        aliceSharedSecret = aliceKeyAgree.generateSecret();
        LogPanel.logEvent("Alice secret: " +
                toHexString(aliceSharedSecret));
    }

    public void phase(byte [] bobPubKeyEnc ) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
        aliceKeyFac = KeyFactory.getInstance("DH");
        x509KeySpec = new X509EncodedKeySpec(bobPubKeyEnc);
        PublicKey bobPubKey = aliceKeyFac.generatePublic(x509KeySpec);
        LogPanel.logEvent("ALICE: Execute PHASE1 ...");
        aliceKeyAgree.doPhase(bobPubKey, true);
    }


    public void generatePublicKeyFrom() throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException, InvalidKeyException {

        /*
         * Alice creates her own DH key pair with 2048-bit key size
         */

        LogPanel.logEvent("ALICE: Generate DH keypair ...");
        KeyPairGenerator aliceKpairGen = KeyPairGenerator.getInstance("DH");
        aliceKpairGen.initialize(2048);
        KeyPair aliceKpair = aliceKpairGen.generateKeyPair();

        // Alice creates and initializes her DH KeyAgreement object

        LogPanel.logEvent("ALICE: Initialization ...");
        aliceKeyAgree = KeyAgreement.getInstance("DH");
        aliceKeyAgree.init(aliceKpair.getPrivate());

        // Alice encodes her public key, and sends it over to Bob.
        alicePubKeyEnc = aliceKpair.getPublic().getEncoded();
    }


    private static void byte2hex(byte b, StringBuffer buf) {
        char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
                '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        int high = ((b & 0xf0) >> 4);
        int low = (b & 0x0f);
        buf.append(hexChars[high]);
        buf.append(hexChars[low]);
    }

    /*
     * Converts a byte array to hex string
     */
    private static String toHexString(byte[] block) {
        StringBuffer buf = new StringBuffer();
        int len = block.length;
        for (int i = 0; i < len; i++) {
            byte2hex(block[i], buf);
            if (i < len-1) {
                buf.append(":");
            }
        }
        return buf.toString();
    }



}
