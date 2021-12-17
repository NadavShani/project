import java.util.Arrays;
import java.util.Date;

public class HostInstance {

    private String lastTimeEstablished;
    private String lastPacketReceived;
    private String hostName;
    private String hostIP;
    private port [] ports;
    private AES aes;
    private String keySecretAsString;
    private boolean isServerEncrypting;

    @Override
    public String toString() {
        return "Host{" +
                "lastPacketReceived=" + lastPacketReceived +
                ", lastTimeEstablished=" + lastTimeEstablished +
                ", hostName='" + hostName +
                ", hostIP='" + hostIP + '\'' +
                ", ports=" + Arrays.toString(ports) +
                ", aes =" + aes.toString() +
                '}';
    }

    public HostInstance(Date firstTimeEstablised, String lastTimeEstablished, String hostName, String hostIP, AES aes) {
        this.lastTimeEstablished = lastTimeEstablished;
        this.hostName = hostName;
        this.hostIP = hostIP;
        this.aes = aes;
        this.keySecretAsString = aes.getKeyFromAes();
        isServerEncrypting = true;
        //this.sharedsecret = sharedsecret;

    }

    /************** GETTERS **************/

    public String getFirstTimeEstablised() {
        return lastPacketReceived;
    }

    public String getLastTimeEstablished() {
        return lastTimeEstablished;
    }

    public String getHostName() {
        return hostName;
    }

    public String getHostIP() {
        return hostIP;
    }

    public AES getAes() {
        return aes;
    }

    public String getKeySecretAsString() {
        return keySecretAsString;
    }

    public boolean isServerEncrypting() {
        return isServerEncrypting;
    }

    /************** SETTERS **************/

    public void setFirstTimeEstablised(String firstTimeEstablised) {
        this.lastPacketReceived = firstTimeEstablised;
    }

    public void setLastTimeEstablished(String lastTimeEstablished) {
        this.lastTimeEstablished = lastTimeEstablished;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public void setHostIP(String hostIP) {
        this.hostIP = hostIP;
    }

    public void setAes(AES aes) {
        this.aes = aes;
    }
    public void setKeySecretAsString(String keySecretAsString) {
        this.keySecretAsString = keySecretAsString;
    }
    public void setServerEncrypting(boolean isServerEncrypting){
        this.isServerEncrypting = isServerEncrypting;
    }


}
