import java.net.UnknownHostException;
/**
 * @author Alyssa
 * @author Truong
 */
public class ServerNetworkInfo extends NetworkInfo{
    private static int counter = 0;
    private int num;

    public ServerNetworkInfo(String name, String ipAddr, int portNum) throws UnknownHostException {
        super(name, ipAddr, portNum);
        num = counter;
        counter++;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public static ServerNetworkInfo parse(String info) throws UnknownHostException {
        String[] tokens = info.split(":");
        if (tokens.length!=3) {
            System.out.println("INVALID SERVER NETWORK INFO:" + info);
        }
        return new ServerNetworkInfo(tokens[0], tokens[1], Integer.parseInt(tokens[2]));
    }
}
