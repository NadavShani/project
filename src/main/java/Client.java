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

    // constructor to put ip address and port
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


              //  in.read(iv); /* bob iv */


                aes = new AES(alice.getSecret());
                byte [] iv = aes.getEncodedParams();
              //  AES aes2 = new AES(alice.getSecret(),iv);
                /* trying to encrypt message to bob */
               // while(in.available() < 1) /* not avliable */

                out.write(iv);
               // System.out.println(new String(iv));
                while (!in.readUTF().equals("iv-ok")) ;

              //  byte [] encrypted = aes.encrypt("hello world".getBytes());
              //  System.out.println("encyrpted is : " + new String(encrypted));

              //  out.write(encrypted);

               // byte [] fromServer = new byte[4096];
              //  in.read(fromServer);
              //  System.out.println(new String(Decode(fromServer)));
             //   System.out.println(new String(aes.decrypt(Decode(fromServer))));


            } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } //catch (IllegalBlockSizeException e) {
            // catch (IllegalBlockSizeException e) {
              //  e.printStackTrace();
           // } catch (BadPaddingException e) {
               // e.printStackTrace();
          //  } //catch (InvalidAlgorithmParameterException e) {
               // e.printStackTrace();
           // }
            //  e.printStackTrace();
           // } catch (BadPaddingException e) {
             //   e.printStackTrace();
           // }
            /* close connection */
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