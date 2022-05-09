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
    
    /**
     * Constructor set by port number
     * @param portNum
     * @throws UnknownHostException
     */
    public NetworkInfo(int portNum) throws UnknownHostException {
        name = "";
        ipAddr = InetAddress.getLocalHost().toString().split("/")[1];
        this.portNum = portNum;  
    }

    /**
     * Constructor set by name, ip address and port number
     */
    public NetworkInfo(String name, String ipAddr, int portNum) throws UnknownHostException {
        this.name = name;
        this.ipAddr = ipAddr;
        this.portNum = portNum;
    }
    
    /**
     * Get name of the network
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Set name of the network
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Get ip address of the network
     * @return ip address
     */
    public String getIpAddr() {
        return ipAddr;
    }

    /**
     * Set ip address of the network
     * @param ipAddr
     */
    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }

    /**
     * Get port number
     * @return port number
     */
    public int getPortNum() {
        return portNum;
    }

    /**
     * Set port number
     * @param portNum
     */
    public void setPortNum(int portNum) {
        this.portNum = portNum;
    }

    /**
     * Created new network info object with provided info
     * @param info
     * @return
     * @throws NumberFormatException
     * @throws UnknownHostException
     */
    public static NetworkInfo parse(String info) throws NumberFormatException, UnknownHostException {
        String[] tokens = info.split(":");
        return new NetworkInfo(tokens[0], tokens[1], Integer.parseInt(tokens[2]));
    } 

    /**
     * Display a formatted String
     * @return
     */
    public String displayString() {
        return String.format("|%16s |%16s |%16s |", name, ipAddr, portNum);
    }

    /**
     * Constructed string containing name, ip and port number of network
     */
    @Override
    public String toString() {
        return name + ":" + ipAddr + ":" + portNum ;
    }
    
}
