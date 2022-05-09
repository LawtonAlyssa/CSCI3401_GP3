import java.net.*;

/**
 * 
 * @author Alyssa
 * @author Truong
 */
public class NetworkInfo {
    private String name;
    private String ipAddr;
    private int portNum;
    

    public NetworkInfo(int portNum) throws UnknownHostException {
        name = "";
        ipAddr = InetAddress.getLocalHost().toString().split("/")[1];
        this.portNum = portNum;  
    }

    public NetworkInfo(String name, String ipAddr, int portNum) throws UnknownHostException {
        this.name = name;
        this.ipAddr = ipAddr;
        this.portNum = portNum;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getIpAddr() {
        return ipAddr;
    }

    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }

    public int getPortNum() {
        return portNum;
    }

    public void setPortNum(int portNum) {
        this.portNum = portNum;
    }

    public static NetworkInfo parse(String info) throws NumberFormatException, UnknownHostException {
        String[] tokens = info.split(":");
        return new NetworkInfo(tokens[0], tokens[1], Integer.parseInt(tokens[2]));
    } 

    public String displayString() {
        return String.format("|%16s |%16s |%16s |", name, ipAddr, portNum);
    }

    @Override
    public String toString() {
        return name + ":" + ipAddr + ":" + portNum ;
    }
    
}
