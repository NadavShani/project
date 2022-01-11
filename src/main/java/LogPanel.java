import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/*
CLASS: LogPanel - this class is responsible for the logging window in the gui
*/


public class LogPanel extends JPanel {

    private static JTextArea logTextArea;
    private static JScrollPane scroll;
    private static JCheckBox showPayloadLogs;
    private static boolean showLogsEnable;

    public LogPanel(){

        setBackground(Color.white);
        setVisible(true);
        setLayout(new BorderLayout());
        add(new JLabel("<html> <font color='black'>System Logs:</font></html>"),BorderLayout.NORTH);
        showLogsEnable = true;
        showPayloadLogs = new JCheckBox("show packet payloads",true);
        showPayloadLogs.setBounds(100,100, 50,50);
        itemListener listener = new itemListener();
        showPayloadLogs.addItemListener(listener);
        add(showPayloadLogs,BorderLayout.SOUTH);
        logTextArea  = new JTextArea();
        logTextArea.setBorder(BorderFactory.createStrokeBorder(new BasicStroke(0.5f)));

        scroll = new JScrollPane(logTextArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        //add(scroll,BorderLayout.EAST);
        add(scroll,BorderLayout.CENTER);

    }

    public static final LogPanel getInstance(){
        return new LogPanel();
    }

    /* Append New Event Log */
    public static void logEvent(String event){
        String currentDateAndTime = DateTimeFormatter.ofPattern("MM-dd-yyyy - HH:mm:ss", Locale.ENGLISH).format(LocalDateTime.now());
        logTextArea.append("[" + currentDateAndTime + "] - " + event + "\n" );
    }

    public static JScrollPane getScroller(){
        return scroll;
    }
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

    public static boolean getShowLogsEnable(){
        return showLogsEnable;
    }

    class itemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            if(e.getSource() == showPayloadLogs ) {
                if(showLogsEnable){
                    showLogsEnable = false;
                    showPayloadLogs.setSelected(false);
                }
                else {
                    showLogsEnable = true;
                    showPayloadLogs.setSelected(true);
                }
            }
        }
    }


}
