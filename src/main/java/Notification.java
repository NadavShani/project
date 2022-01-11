import java.awt.*;
import java.awt.TrayIcon.MessageType;
import javax.swing.ImageIcon;
/*
CLASS: Notification - This class is responsible to pop up a windows notification
*/
public class Notification {

    public Notification(String title,String messgae) throws AWTException, java.net.MalformedURLException {
        if (SystemTray.isSupported())
            displayTray(title,messgae);
         else
            System.err.println("System tray not supported!");
    }
    /*
    Function: show notification to the client
    INPUT : title, msg
    OUTPUT: NULL
    */
    public void displayTray(String title,String message) throws AWTException, java.net.MalformedURLException {
        SystemTray tray = SystemTray.getSystemTray();
        Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
        TrayIcon trayIcon = new TrayIcon(image, "TcpEncrypt");
        trayIcon.setImageAutoSize(true);
        trayIcon.setToolTip("Secure Connection Established");
        tray.add(trayIcon);
        trayIcon.displayMessage(title, message, MessageType.INFO);
        try {
            Thread.sleep(3000);
        }
        catch (InterruptedException ie){
            ie.printStackTrace();
        }
        tray.remove(trayIcon);
    }
}