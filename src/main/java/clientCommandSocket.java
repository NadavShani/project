import java.awt.*;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class clientCommandSocket extends Thread {

    private production prodInstance;
    private String server,command;
    private int port;
    public clientCommandSocket(production prod,String command,String server,int port) throws IOException {
        this.prodInstance = prod;
        this.command=command;
        this.server=server;
        this.port=port;
        start();
    }

    @Override
    public void run() {
        Socket socket = null;
        DataInputStream in = null;
        DataOutputStream out = null;
        String msg = null;
        try {
             socket  = new Socket(server, port);

             in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
             out = new DataOutputStream(socket.getOutputStream());

             LogPanel.logEvent("connected successfully to command socket on " + server);
             LogPanel.logEvent("Sending Command: " + command + " to: " + server);
             out.writeUTF(command);

             while (!in.readUTF().equals("command-ok")) ; /* waiting for bob to start */
             if(command.contains("server encryption")) {
                 HostInstance hi = this.prodInstance.getHostInstance(socket.getInetAddress().getHostAddress());
                if(command.contains("disable")){
                    hi.setServerEncrypting(false);
                    msg = "Server Encryption Disabled  - ok ";
                }
                else if(command.contains("enable")) {
                    hi.setServerEncrypting(true);
                    msg = "Server Encryption enabled  - ok ";
                }
                System.out.println(socket.getInetAddress().getHostAddress());

             }

            Notification popup = new Notification("Command Executed", msg);
            in.close();
            out.close();
            socket.close();

        } catch (IOException | AWTException e) {
            e.printStackTrace();
        }

    }
}
