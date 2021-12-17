import java.awt.*;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class clientCommandSocket extends Thread {

    private String server,command;
    private int port;
    public clientCommandSocket(String command,String server,int port) throws IOException {
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
             out.writeUTF(command);

             while (!in.readUTF().equals("command-ok")) ; /* waiting for bob to start */
             if(command.contains("server encryption"))
                 msg = "Server Encryption Disabled  - ok ";
             Notification popup = new Notification("Command Executed", msg);

        } catch (IOException | AWTException e) {
            e.printStackTrace();
        }

    }
}
