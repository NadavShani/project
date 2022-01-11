import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
/*
CLASS: diffieThread - this class initialize diffie hellman in a new Thread
*/
public class diffieThread extends Thread {

    private String connecctToAddress;
    private int port;
    private Client client;
    private production prodInstance;

    public diffieThread(String connecctToAddress, int port,production prod){
        super();
        this.port = port;
        this.connecctToAddress = connecctToAddress;
        this.prodInstance=prod;

        start();
    }

    public Client getClient(){
        return this.client;
    }
    /*
    Function: run - thread code - This thread connects to a server at port 5301 and Initialize The Key Exchange
    INPUT : NULL
    OUTPUT: NULL
    */
    public void run(){
        try {
            client = new Client(connecctToAddress, port);
            prodInstance.addHostInstance(client.getSocket().getInetAddress(),client.getAes());
            Notification popup = new Notification("New Connection Established", "Key Exchanged With " + connecctToAddress);
        }
        catch (Exception e){
            e.printStackTrace();
            /* in order to permit the client to trying again in the future */
            prodInstance.sourcesTryingToDiffie.remove(connecctToAddress);
        }
    }
}
