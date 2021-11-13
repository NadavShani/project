import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;



public class LogPanel extends JPanel {

    private static JTextArea logTextArea;
    private static JScrollPane scroll;

    public LogPanel(){

        setBackground(Color.white);
        setVisible(true);
        setLayout(new BorderLayout());
        add(new JLabel("<html> <font color='black'>System Logs:</font></html>"),BorderLayout.NORTH);

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
        System.out.println();
    }


}
