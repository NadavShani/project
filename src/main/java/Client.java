// A Java program for a Client
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;

/*
CLASS: Client - this class initialize diffie hellman and manage the algorithm
*/

public class Client
{
    // initialize socket and input output streams
    private Socket socket            = null;
    private DataInputStream  in   = null;
    private DataOutputStream out     = null;
    private aliceDH alice = null;
    private boolean isConnected = false;
    private AES aes;
    private final int maxAttempts = 3;


    protected AES getAes(){
        return this.aes;
    }
    protected Socket getSocket() {return socket;}

    public Client(){

    }

    /*
    Function: Client - this class initialize diffie hellman and manage the algorithm
    INPUT : server's address, server's socket port
    OUTPUT: NULL
    */
    public Client(String address, int port) throws Exception {

        isConnected=false;
        int attempt=0;
        /* establish a connection to host address */
        while(!isConnected) {
            try {
                socket = new Socket(address, port);
                LogPanel.logEvent("Connected");

                /* takes input from the server socket */
                in = new DataInputStream(
                        new BufferedInputStream(socket.getInputStream()));

                // sends output to the socket
                out = new DataOutputStream(socket.getOutputStream());
                isConnected = true;
            } catch (Exception e) {
                if(attempt < maxAttempts) {
                    attempt++;
                    LogPanel.logEvent("Time Out - Will Retry Again in 2 Seconds (attempt: " + attempt + "/" + maxAttempts + ") - " + address);
                    try {
                        Thread.sleep(500);
                    }
                    catch (InterruptedException t) {
                        t.printStackTrace();
                        System.exit(1);
                    }
                }
                else {
                    LogPanel.logEvent("Diffie Hellman Failed with: " + address);
                    throw new Exception("Diffie Hellman Failed with: " + address);
                }
            }
        }
        /* Startin DH */

        if(isConnected) {

            String line = "dh-start";
            byte[] payload = new byte[4096];
            try {
                alice = new aliceDH();
                alice.start();/* alice has generated her public */
                out.writeUTF("dh-start");
                while (!in.readUTF().equals("bob-ok")) ; /* waiting for bob to start */
                out.write(alice.getPublicKey()); /* alice is sending her public key to bob */
                while (!in.readUTF().equals("bob-public-ok")) ; /* waiting for bob public */
                in.read(payload); /* bob public */

                alice.phase(payload);
                alice.generateSecret();

                aes = new AES(alice.getSecret());
                byte [] iv = aes.getEncodedParams();

                out.write(iv);

                while (!in.readUTF().equals("iv-ok")) ;


            } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            }
            try
            {
                in.close();
                out.close();
                socket.close();
                LogPanel.logEvent("secret generated");
                LogPanel.logEvent("closing Socket");

            }
            catch(IOException i)
            {
                System.out.println(i);
            }
        }

    }

    public byte[] Decode(byte[] packet)
    {
        var i = 0;
        while(packet[i] != 0)
            i++;
        byte [] result = new byte[i];

        for(int index=0;index<i;index++)
            result[index]=packet[index];

        return result;
    }

    public static void main(String args[]) {

    }


}