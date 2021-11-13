import com.github.ffalcinelli.jdivert.*;
import com.github.ffalcinelli.jdivert.exceptions.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class main {

    public static void main(String[] args) throws WinDivertException {
      //  WinDivert w = new WinDivert("(tcp.DstPort == 80 and outbound) or (inbound)
        WinDivert w = new WinDivert("(outbound and tcp.DstPort = 21 or tcp.DstPort = 20) or (inbound and ip.SrcAddr = 51.116.128.233)");
        w.open(); // packets will be captured from now on
        int prevIpTotalLength = 0;
        int nextpayloadlength = 0;
        int prevpayloadlength = 0;
        while (true) {
            Packet packet = w.recv();  // read a single packet
            String payloadFromServer = new String(packet.getPayload());
            if (packet.getIpv4().getSrcAddrStr().equals("51.116.128.233") && payloadFromServer.indexOf("Password") >= 0 ) {
                //client is expect ack of his packet and not my divert packet - let's fix it:
               // packet.getIpv4().setTotalLength(prevIpTotalLength); //fix ipv4 total length
                packet.getTcp().setAckNumber(packet.getTcp().getAckNumber() - (nextpayloadlength - prevpayloadlength)); //fix ack
                packet.recalculateChecksum();
                System.out.println("yes");
                w.send(packet); //back to client
            }
            else {
                try {


                    String pay = new String(packet.getPayload());
                    if (pay.indexOf("USER") >= 0) {
                       // System.out.println("old packet: " + packet);
                        // System.out.println(packet.getHeadersLength() + " " + packet.getPayload().length);
                        //    System.out.println(packet.getRaw().length);
                        // System.out.println(packet.getWinDivertAddress());
                        //   System.out.println(Util.getBytesAtOffset(ByteBuffer.wrap(packet.getRaw()), 0, packet.getRaw().length));
                        // System.out.println("header: " + Util.getBytesAtOffset(ByteBuffer.wrap(packet.getRaw()), 0, packet.getRaw().length - packet.getPayload().length));
                        /* new payload */
                        String msg = new String("USER nadavs\r\n");
                        byte[] payload = new byte[msg.length()];
                        payload = msg.getBytes();

                        /* clone header */
                        byte[] header = Util.getBytesAtOffset(ByteBuffer.wrap(packet.getRaw()), 0, packet.getRaw().length - packet.getPayload().length); //clone header

                        /* new  raw */
                        byte[] myraw = new byte[packet.getHeadersLength() + payload.length];

                        Util.setBytesAtOffset(ByteBuffer.wrap(myraw), 0, header.length, header); //write header to new raw
                        Util.setBytesAtOffset(ByteBuffer.wrap(myraw), header.length, payload.length, payload); //write new payload to new raw
                        Packet p = new Packet(myraw, packet.getWinDivertAddress()); //create packet
                        p.recalculateChecksum();
                        prevIpTotalLength = packet.getIpv4().getTotalLength();
                        prevpayloadlength = packet.getPayload().length;
                        nextpayloadlength = p.getPayload().length;

                        p.getIpv4().setTotalLength(myraw.length);
                       // System.out.println("new packet: " + p);
                        String old = new String(packet.getPayload());
                        String new2 = new String(p.getPayload());
                       // System.out.println("old payload  : " + old + " length: " + old.length());
                        //System.out.println("new payload  : " + new2 + " length: " + new2.length());

                /*String oldheader = new String(Util.getBytesAtOffset(ByteBuffer.wrap(packet.getRaw()), 0, packet.getRaw().length - packet.getPayload().length));
                String myheader = new String(Util.getBytesAtOffset(ByteBuffer.wrap(p.getRaw()), 0, p.getRaw().length - p.getPayload().length));
                System.out.println("old header : " + oldheader);
                System.out.println("new header : " + myheader);*/


                        // packet.setDstPort(443);
                /*
                String msg = new String("hello world");
                byte[] new_payload = new byte[msg.length() + packet.getPayload().length]; //new array should be msg length + payload length
                ByteBuffer [] payload = new ByteBuffer[20];
                payload.
                payload = msg.getBytes();
                payload = ByteBuffer.wrap(b);
                 */
                        w.send(p, true);
                    }
                    else {
                        w.send(packet);
                       // System.out.println(packet);
                    }
                } catch (Exception e) {
                    System.out.println("********** EXCEPTION ********** " + e.toString());
                }

            }
        }
    }
}
