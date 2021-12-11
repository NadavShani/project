import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class testnumber {

    private int [][] mat;

    public boolean function(int [][] mat) {
        int col=0,current=0,value=0;

        for(int row=0;row<4;row++){
            col=0;
            for(current=row,value=mat[current][col];current<4;current++){
                if(mat[current][col++] != value)
                    return false;
            }
        }

        return true;
    }

    public static void main(String [] args) throws UnknownHostException, SocketException {
        System.out.println(InetAddress.getLocalHost().getHostAddress());


    }
}