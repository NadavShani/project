import com.github.ffalcinelli.jdivert.*;
import com.github.ffalcinelli.jdivert.exceptions.*;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


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
    private String localAddress = null;
    private ArrayList<String> machineLocalAddresses;

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
        machineLocalAddresses = new ArrayList<String>();

        Enumeration e = null;
        try {
            e = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        while(e.hasMoreElements())
        {
            NetworkInterface n = (NetworkInterface) e.nextElement();
            Enumeration ee = n.getInetAddresses();
            while (ee.hasMoreElements())
            {
                InetAddress i = (InetAddress) ee.nextElement();
                machineLocalAddresses.add(i.getHostAddress());
            }
        }
    }


    public static void main(String[] args) throws WinDivertException, FileNotFoundException, UnknownHostException {

        /* Production class */
        production prod = new production();
        try {
            prod.setLocalAddress(InetAddress.getLocalHost().getHostAddress());
        }
        catch (UnknownHostException u){
            u.printStackTrace();
            System.exit(1);
        }

        /* read unsecured protocols file*/
        File file = new File("unsecured.config");
        Scanner myReader = new Scanner(file);
        ArrayList<Integer> unsecuredProtocols = new ArrayList<Integer>();
        while (myReader.hasNextLine())
            unsecuredProtocols.add(Integer.parseInt(myReader.nextLine()));

        String filter = new String();
        for(int i=0;i<unsecuredProtocols.size();i++) {
            if(i < unsecuredProtocols.size() -1)
                filter += "tcp.DstPort = " + unsecuredProtocols.get(i) + " or tcp.SrcPort = " + unsecuredProtocols.get(i) + " or ";
            else
                filter += "tcp.DstPort = " + unsecuredProtocols.get(i) + " or tcp.SrcPort = " + unsecuredProtocols.get(i);
        }

        System.out.println(filter);
        /* Open Windivert Handle */
        prod.w = new WinDivert(filter);
        prod.w.open(); // packets will be captured from now on

        /** Main Loop **/
        while (true) {
            try {
                /**** inbound packet ***/
                Packet packet = prod.w.recv();
                String hostAddress = packet.getIpv4().getDstAddrStr();
               // if (hostAddress.equals(prod.getLocalAddress())) {
                    if(prod.isAddressIsLocal(hostAddress)) {
                    if(prod.IsHostExists(packet.getSrcAddr())) {
                        class RunMe implements Runnable {
                            private production prod;
                            private String hostAddress;

                            public RunMe(production prodInstance, String hostAddress) {
                                this.prod = prodInstance;
                                this.hostAddress = hostAddress;
                            }
                            @Override
                            public void run() {
                               try {
                                   HostInstance hi = prod.getHostInstance(packet.getSrcAddr());
                                   byte[] payload = hi.getAes().decrypt(packet.getPayload());
                                   Packet p = prod.generateNewPacketWithPaylod(packet, payload); //create new packet with encrypted payload
                                   p.recalculateChecksum(); //checksum
                                   prod.w.send(p, false); //send to server
                               }
                               catch (Exception e){
                                   e.printStackTrace();
                               }

                            }
                        }
                        new RunMe(prod, hostAddress).run();
                    }
                }
                else {
                    /**** Outbound packet ***/
                    boolean isHostExists = prod.IsHostExists(hostAddress);
                    if (!isHostExists && !prod.sourcesTryingToDiffie.contains(hostAddress)) {
                        prod.sourcesTryingToDiffie.add(hostAddress);
                        /* Send Notification To Server To Start Diffie Hellman - Until Success */
                        LogPanel.logEvent("Unsecured Protocol Detected (" + packet.getTcp().getDstPort() + ")  ...");
                        LogPanel.logEvent("Intializing diffie-hellman with target " + packet.getDstAddr() + " ...");
                        while (!prod.sendNotificationToHost(packet)) ;
                        /* Connect Client */
                        diffieThread df = new diffieThread(hostAddress, diffiePort, prod);

                    } else if (isHostExists) {

                        class RunMe implements Runnable {
                            private production prod;
                            private String hostAddress;

                            public RunMe(production prodInstance, String hostAddress) {
                                this.prod = prodInstance;
                                this.hostAddress = hostAddress;
                            }

                            @Override
                            public void run() {
                                HostInstance hi = this.prod.getHostInstance(hostAddress);
                                try {
                                    byte[] payload;
                                    payload = hi.getAes().encrypt(packet.getPayload());
                                    Packet p = this.prod.generateNewPacketWithPaylod(packet, payload);
                                    p.recalculateChecksum(); //checksum
                                    prod.w.send(p, false); //send to server
                                    if(LogPanel.getShowLogsEnable()) /* Show Packet Payload on it's way out */
                                        LogPanel.logEvent(new String("[ENC]: " + new String(packet.getPayload())));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        new RunMe(prod, hostAddress).run();
                    }
                }
            }
            catch(Exception e){
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
            System.out.println(intializePacket.getRaw());
            success = true;

        }
        catch (WinDivertException w){
            w.printStackTrace();
            LogPanel.logEvent("Notify Again: " + old.getDstAddr() + " to start diffie session");
        }

        return success;
    }

    public static void sendCommandToServer(String server,String command){
        LogPanel.logEvent("Sending Command: +" + command + " To:" + server);
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

    private boolean isAddressIsLocal(String address){
        return this.machineLocalAddresses.contains(address);
    }


    /************** SETTERS **************/
    /* Set production local ip for incoming */
    private void setLocalAddress(String localAddress){
        this.localAddress = localAddress;
    }

    /************** GETTERS **************/

    private String getLocalAddress(){
        return this.localAddress;
    }

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