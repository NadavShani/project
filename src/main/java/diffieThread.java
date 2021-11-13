import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

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

    public void run(){
        try {
            client = new Client(connecctToAddress, port);
            prodInstance.addHostInstance(client.getSocket().getInetAddress(),client.getAes());
        }
        catch (Exception e){
            e.printStackTrace();
            /* in order to premit the client to trying again in the future */
            prodInstance.sourcesTryingToDiffie.remove(connecctToAddress);
        }
    }
}
