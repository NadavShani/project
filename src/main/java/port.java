public class port {

    private String portName;
    private int portNumber;
    private boolean status;

    public port(String portName,int portNumber, boolean status) {
        this.portName = portName;
        this.portNumber = portNumber;
        this.status = status;
    }

    /************** GETTERS **************/
    protected String getPortName(){
        return this.portName;
    }

    protected int getPortNumber(){
        return this.portNumber;
    }

    protected boolean getStatus(){
        return this.status;
    }

    /************** SETTERS **************/

    protected void setPortName(String portName){
        this.portName = portName;
    }

    protected void setPortNumber(int portNumber){
        this.portNumber = portNumber;
    }

    protected void setStatus(boolean status){
        this.status = status;
    }

}
