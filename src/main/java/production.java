import com.github.ffalcinelli.jdivert.*;
import com.github.ffalcinelli.jdivert.exceptions.*;
import com.github.ffalcinelli.jdivert.windivert.TemporaryDirManager;
import com.sun.jna.Platform;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;


public class production {

    private static int frameWidth = 800;
    private static int frameHeight = 600;
    private static int diffiePort = 5300;
    private static int managementPort = 5301;
    private ArrayList<HostInstance> hosts;
    private GUI gui;
    private JFrame frame;
    private WinDivert w;
    protected ArrayList<String> sourcesTryingToDiffie;

    public production(){

        /* Production Frame */
        frame = new JFrame("TcpEncrypt");
        gui = new GUI();

        frame.add(gui);
        frame.setVisible(true);
        frame.setSize(800,600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        /* hostInstances List */
        hosts = new ArrayList<HostInstance>();
        sourcesTryingToDiffie = new ArrayList<String>();
    }


    public static void main(String[] args) throws WinDivertException, FileNotFoundException {

        /* Production class */
        production prod = new production();

       // System.out.println("22222");

      //  System.loadLibrary("Windivert");
        /* read unsecured protocols file*/
        File file = new File("unsecured.config");
        Scanner myReader = new Scanner(file);
        ArrayList<Integer> unsecuredProtocols = new ArrayList<Integer>();
        while (myReader.hasNextLine())
            unsecuredProtocols.add(Integer.parseInt(myReader.nextLine()));

        String filter = new String();
        for(int i=0;i<unsecuredProtocols.size();i++) {
            if(i < unsecuredProtocols.size() -1)
                filter += "tcp.DstPort = " + unsecuredProtocols.get(i) + " or ";
            else
                filter += "tcp.DstPort = " + unsecuredProtocols.get(i);
        }

        /* Open Windivert Handle */
        prod.w = new WinDivert(filter);
        System.out.println(Platform.is64Bit());
        prod.w.open(); // packets will be captured from now on

        /** Main Loop **/
        while (true) {
            try {

                Packet packet = prod.w.recv();  // read a single packet
                /* Is Host Exists? */
                String hostAddress = packet.getIpv4().getDstAddrStr();
                boolean isHostExists = prod.IsHostExists(hostAddress);
                if(!isHostExists && !prod.sourcesTryingToDiffie.contains(hostAddress)){
                    prod.sourcesTryingToDiffie.add(hostAddress);
                    /* Send Notification To Server To Start Diffie Hellman - Until Success */
                    LogPanel.logEvent("Unsecured Protocol Detected (" + packet.getTcp().getDstPort() + ")  ...");
                    LogPanel.logEvent("Intializing diffie-hellman with target " + packet.getDstAddr() + " ...");
                    while(!prod.sendNotificationToHost(packet));
                    /* Connect Client */
                    diffieThread df = new diffieThread(hostAddress, diffiePort,prod);

                }
                else if(isHostExists) {

                    class RunMe implements Runnable {
                        private production prod;
                        private String hostAddress;
                        public RunMe(production prodInstance,String hostAddress){
                            this.prod=prodInstance;
                            this.hostAddress=hostAddress;
                        }

                        @Override
                        public void run() {
                            HostInstance hi = this.prod.getHostInstance(hostAddress);
                            try {
                                byte [] payload = hi.getAes().encrypt(packet.getPayload());
                                Packet p = this.prod.generateNewPacketWithPaylod(packet, payload);
                                p.recalculateChecksum(); //checksum
                                prod.w.send(p, true); //send to server
                                LogPanel.logEvent(new String("[ENC]: " + new String(packet.getPayload())));
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                    new RunMe(prod,hostAddress).run();
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /* if packet source ip is not in hosts list,  we should request server to start listening to diffie hellman */
    public boolean sendNotificationToHost(Packet old){
        LogPanel.logEvent("Notify: " + old.getDstAddr() + " to start diffie session");
        boolean success = false;
        try {
            Packet intializePacket = generateNewPacketWithPaylod(old, "start-diffie".getBytes());
            intializePacket.getTcp().setDstPort(managementPort);
            intializePacket.recalculateChecksum();
            w.send(intializePacket);
            success = true;

        }
        catch (WinDivertException w){
            w.printStackTrace();
            LogPanel.logEvent("Notify Again: " + old.getDstAddr() + " to start diffie session");
        }

        return success;
    }

    /* Add Host Instance To Production Host Instances, this function is called by diffie thread , also handle gui */
    public synchronized void addHostInstance(InetAddress hostAddress, AES aes){
        HostInstance hostInstance = new HostInstance(new Date(), DateTimeFormatter.ofPattern("MM-dd-yyyy - HH:mm:ss", Locale.ENGLISH).format(LocalDateTime.now()),hostAddress.getHostName(),hostAddress.getHostAddress(),aes);
        hosts.add(hostInstance);

        ConnectionItem connectionItem = new ConnectionItem(hostInstance);
        gui.getConnectionPanel().add(connectionItem);
        gui.repaint();
        frame.pack();
        frame.setSize(frameWidth,frameHeight);

        /* remove from list of trying in order to prevent cocurrency */
        sourcesTryingToDiffie.remove(hostAddress.getHostAddress());

    }

    /* get host from hostInstance list by ip , return null otherwise */
    public HostInstance getHostInstance(String ip){
        HostInstance result = null;
        for(HostInstance hi : hosts)
            if (hi.getHostIP().equals(ip))
                result = hi;

        return result;
    }

    public boolean IsHostExists(String ip){
        boolean result=false;
        for(HostInstance h : hosts) {
            if (h.getHostIP().equals(ip))
                result = true;
        }
        return result;
    }

    public Packet generateNewPacketWithPaylod(Packet oldPacket,byte [] newPayLoad) throws WinDivertException { /*old packet and new payload*/
        /* clone header */
        byte[] header = Util.getBytesAtOffset(ByteBuffer.wrap(oldPacket.getRaw()), 0, oldPacket.getRaw().length - oldPacket.getPayload().length); //clone header

        /* create new  raw */
        byte[] myraw = new byte[oldPacket.getHeadersLength() + newPayLoad.length];

        /* write header to new raw */
        Util.setBytesAtOffset(ByteBuffer.wrap(myraw), 0, header.length, header);

        /* write new payload to new raw */
        Util.setBytesAtOffset(ByteBuffer.wrap(myraw), header.length, newPayLoad.length, newPayLoad);

        /* create packet */
        Packet p = new Packet(myraw, oldPacket.getWinDivertAddress());
        p.getIpv4().setTotalLength(myraw.length);
        return p;

    }

    /************** GETTERS **************/
    protected ArrayList<HostInstance> getHosts(){
        return this.hosts;
    }

    protected int getFrameWidth(){
        return this.frameWidth;
    }

    protected int getFrameHeight(){
        return this.frameHeight;
    }


    protected static String toHexString(byte[] block) {
        StringBuffer buf = new StringBuffer();
        int len = block.length;
        for (int i = 0; i < len; i++) {
            byte2hex(block[i], buf);
            if (i < len-1) {
                buf.append(":");
            }
        }
        return buf.toString();
    }

    protected static void byte2hex(byte b, StringBuffer buf) {
        char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
                '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        int high = ((b & 0xf0) >> 4);
        int low = (b & 0x0f);
        buf.append(hexChars[high]);
        buf.append(hexChars[low]);
    }

}