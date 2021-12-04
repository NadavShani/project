import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class AES {

    private SecretKeySpec secret;
    private Cipher cipher;
    private byte [] encodedParams;
    private boolean isEncryptMode;
    public AlgorithmParameters aesParams;

    public AES(byte [] sharedsecret) throws NoSuchPaddingException, NoSuchAlgorithmException, IOException, InvalidKeyException {
        this.secret = new SecretKeySpec(sharedsecret,0,16,"AES");
        this.cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        this.cipher.init(Cipher.ENCRYPT_MODE, this.secret);
        this.encodedParams = this.cipher.getParameters().getEncoded();
        this.aesParams = AlgorithmParameters.getInstance("AES");
        this.aesParams.init(encodedParams);
        isEncryptMode = true;
    }
    public AES(byte [] sharedsecret , byte [] encodedParams) throws NoSuchPaddingException, NoSuchAlgorithmException, IOException, InvalidKeyException, InvalidAlgorithmParameterException {
        this.secret = new SecretKeySpec(sharedsecret,0,16,"AES");
        this.cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        this.encodedParams = encodedParams;
        this.aesParams = AlgorithmParameters.getInstance("AES");
        this.aesParams.init(this.encodedParams);
       // this.cipher.init(Cipher.ENCRYPT_MODE, this.secret);


    }

    /*Get Secret Key As String from AES */
    public String getKeyFromAes(){
        return toHexString(this.secret.getEncoded());
    }

    public byte [] getEncodedParams (){
        return this.encodedParams;
    }

    public SecretKeySpec getAesSecret(){
        return this.secret;

    }

    public byte [] encrypt(byte [] clearText) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
       // this.cipher.init(Cipher.ENCRYPT_MODE, this.secret);
        if(isEncryptMode == false) {
            this.cipher.init(Cipher.ENCRYPT_MODE, this.secret);
            isEncryptMode = true;
        }
        return this.cipher.doFinal(clearText);
    }
    public byte [] decrypt(byte [] encryptedMessage) throws IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, IOException {
        if(isEncryptMode == true) {
            this.cipher.init(Cipher.DECRYPT_MODE, this.secret, this.aesParams);
            isEncryptMode = false;
        }
        return this.cipher.doFinal(encryptedMessage);


    }



    protected static String toHexString(byte[] block) {
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

    protected static void byte2hex(byte b, StringBuffer buf) {
        char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
                '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        int high = ((b & 0xf0) >> 4);
        int low = (b & 0x0f);
        buf.append(hexChars[high]);
        buf.append(hexChars[low]);
    }


}
