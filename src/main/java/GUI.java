import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;

public class GUI extends JPanel {

    private JPanel menu;
    private JButton connections,ports,logs,sendCommand;
    private JPanel connectionPanel,commandPanel;
    private JTextField command;
    private LogPanel logPanel;

    protected  JPanel getConnectionPanel(){
        return connectionPanel;
    }

    public GUI(){

        setLayout(new BorderLayout());
        setBackground(Color.white);
        setVisible(true);

        ButtonListener buttonListener = new ButtonListener();

        menu = new JPanel();
        logPanel = LogPanel.getInstance();

        connections = new JButton("connections");
        ports = new JButton("ports");
        logs = new JButton("logs");
        connections.addActionListener(buttonListener);
        logs.addActionListener(buttonListener);

        menu.setLayout(new GridLayout(1, 7));
        menu.setBackground(Color.GRAY);
        menu.add(connections);
        //menu.add(ports);
        menu.add(logs);
        add(menu,BorderLayout.NORTH);

        connectionPanel = new JPanel();
        connectionPanel.add(new JLabel("<html> <font color='green'>Active Connections:</font></html>"));
        connectionPanel.setVisible(true);
        connectionPanel.setLayout(new GridLayout(10,1));

        add(connectionPanel,BorderLayout.CENTER);

        commandPanel = new JPanel();
        commandPanel.setLayout((new GridLayout(1,2)));
        commandPanel.setPreferredSize(new Dimension(50,30));
        command = new JTextField();
        sendCommand = new JButton("send");
        commandPanel.add(command);
        commandPanel.add(sendCommand);

        add(commandPanel,BorderLayout.SOUTH);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

    }

    private class ButtonListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            /* Switch To Logs Panel */
            if(e.getSource() == logs){
                remove(connectionPanel);
                add(logPanel,BorderLayout.CENTER);
                /*JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(getParent());
                System.out.println(parentFrame); */
                revalidate();
                repaint();
            }
            /* Switch To Connections Panel */
            else if(e.getSource() == connections){
                remove(logPanel);
                add(connectionPanel);
                revalidate();
                repaint();
            }

        }
    }


    public static void main(String [] args) {
/*
        JFrame frame = new JFrame("tcpEncrypt");
        GUI gui = new GUI();


        frame.add(gui);
        frame.setVisible(true);
        frame.setSize(800,600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


 */
    }













}



