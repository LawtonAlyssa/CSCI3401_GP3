import java.net.UnknownHostException;
/**
 * @author Alyssa
 * @author Truong
 */
public class ServerNetworkInfo extends NetworkInfo{
    private static int counter = 0;
    private int num;

    /**
     * Constructor
     * @param name
     * @param ipAddr
     * @param portNum
     * @throws UnknownHostException
     */
    public ServerNetworkInfo(String name, String ipAddr, int portNum) throws UnknownHostException {
        super(name, ipAddr, portNum);
        num = counter;
        counter++;
    }

    /**
     * Getting assigned number for network
     * @return network's number
     */
    public int getNum() {
        return num;
    }

    /**
     * Assigning a number for network 
     * @param num
     */
    public void setNum(int num) {
        this.num = num;
    }

    /**
     * Parsing info and create a new Server Network Info instance
     * @param info
     * @return ServerNetworkInfo object
     * @throws UnknownHostException
     */
    public static ServerNetworkInfo parse(String info) throws UnknownHostException {
        String[] tokens = info.split(":");
        return new ServerNetworkInfo(tokens[0], tokens[1], Integer.parseInt(tokens[2]));
    }
}
