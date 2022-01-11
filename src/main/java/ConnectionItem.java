import java.awt.*;
import java.time.LocalDateTime;
import javax.swing.*;

/*
CLASS: ConnectionItem - this class is depict the connections item in the gui window
*/
public class ConnectionItem extends JPanel {

    /* test constructor */
    public ConnectionItem(){
    }

    public ConnectionItem(HostInstance host){
        setLayout(new GridLayout(1, 8));
        setBorder(BorderFactory.createStrokeBorder(new BasicStroke(1.0f)));
        JLabel jlName = new JLabel(host.getHostName()); /* host name */
        JLabel jlIP = new JLabel(host.getHostIP()); /* host ip */
        JLabel jlLastTimeEstablished = new JLabel(new String(host.getLastTimeEstablished().toString())); /* host lastEstablishedDate */
        JLabel jlSecret = new JLabel(host.getKeySecretAsString()); /* host secret */


        add(jlName);
        add(jlIP);
        add(jlLastTimeEstablished);
        add(jlSecret);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

    }

}
