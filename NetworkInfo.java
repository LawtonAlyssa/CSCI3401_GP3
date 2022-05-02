public class NetworkInfo {
    private String name;
    private String ipAddr;
    private int portNum;
    private int num;

    public NetworkInfo(String name, String ipAddr, int portNum) {
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

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public static NetworkInfo parse(String info) {
        return new NetworkInfo(info.split(":")[0], info.split(":")[1], Integer.parseInt(info.split(":")[2]));
    }

    public String displayString() {
        return "Name: " + name + "\tIP Address: " + ipAddr + "\tPort Number: " + portNum ;
    }

    @Override
    public String toString() {
        return name + ":" + ipAddr + ":" + portNum ;
    }
    
}
