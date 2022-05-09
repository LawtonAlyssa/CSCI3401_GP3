/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.*;
import java.net.*;
import java.util.*;

/**
 *
 * @author Alyssa
 * @author Truong
 */
public class Server {

    private ServerSocket serverSocket;
    private int portNum;
    private PrintWriter out;
    private BufferedReader in;
    private ArrayList<ServerNetworkInfo> serversNetworkInfo;
    private File file = new File("controller.txt");
    private ArrayList<Socket> clientSockets;

    /**
     * Constructor to create a server
     * @param portNum: server's port number
     * @throws FileNotFoundException
     */
    public Server(int portNum) throws FileNotFoundException {
        try {
            serverSocket = new ServerSocket(portNum);
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + portNum + ".");
            System.exit(1);
        }
        this.portNum = portNum;
        out = null;
        in = null;
        serversNetworkInfo = new ArrayList<>();
        clientSockets = new ArrayList<>();
        clearFile();

        File folder = new File("client_logs");

        if (folder.exists()) {
            for (File file : new File("client_logs/").listFiles())
                if (!file.isDirectory())
                    file.delete();
        } else {
            folder.mkdirs();
        }

    }

    /**
     * Clear file
     * @throws FileNotFoundException
     */
    public void clearFile() throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(file.getName());
        pw.print("");
        pw.close();
    }

    /**
     * Constructor setting server, its IO channels and port number to default
     */
    public Server() {
        serverSocket = null;
        portNum = 12345;
        out = null;
        in = null;
        serversNetworkInfo = new ArrayList<>();
    }

    /**
     * Get port number of the server
     * @return port number
     */
    public int getPortNum() {
        return portNum;
    }

    /**
     * Set server's port number
     * @param portNum
     */
    public void setPortNum(int portNum) {
        this.portNum = portNum;
        setServerSocket();
    }

    /**
     * Get file
     * @return file
     */
    public File getFile() {
        return file;
    }

    /**
     * Set file
     * @param file
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * Set a new server socket
     */
    public void setServerSocket() {
        try {
            serverSocket = new ServerSocket(portNum);
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + portNum + ".");
            System.exit(1);
        }
    }

    /**
     * Getting server's writing chanel
     * @return PrintWriter object
     */
    public PrintWriter getOut() {
        return out;
    }

    /**
     * Setting server's writing chanel
     * @param out
     */
    public void setOut(PrintWriter out) {
        this.out = out;
    }

    /**
     * Getting Server Network Info
     * @return Server info as an array list
     */
    public ArrayList<ServerNetworkInfo> getServerNetworkInfo() {
        return serversNetworkInfo;
    }

    /**
     * Setting server network info
     * @param serverNetworkInfo
     */
    public void setServerNetworkInfo(ArrayList<ServerNetworkInfo> serverNetworkInfo) {
        this.serversNetworkInfo = serverNetworkInfo;
    }

    /**
     * Getting server's reading chanel
     * @return BufferedReader object
     */
    public BufferedReader getIn() {
        return in;
    }

    /**
     * Setting server's reading chanel
     * @param in
     */
    public void setIn(BufferedReader in) {
        this.in = in;
    }

    /**
     * Getting Server Socket
     * @return ServerSocket object
     */
    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    /**
     * Adding server network info to serversNetworkInfo field
     * @param serverNetworkInfo
     */
    public void addServerNetworkInfo(ServerNetworkInfo serverNetworkInfo) {
        serversNetworkInfo.add(serverNetworkInfo); 

    }

    /**
     * Add new client socket to current list
     * @param clientSocket: client to be added
     * @param clientNetworkInfo: client's info
     */
    public void addClientSocket(Socket clientSocket, NetworkInfo clientNetworkInfo) {
        clientSockets.add(clientSocket); 
    }

    /**
     * Remove server network info
     * @param serverNetworkInfo
     */
    public void removeServerNetworkInfo(ServerNetworkInfo serverNetworkInfo) {
        serversNetworkInfo.remove(serverNetworkInfo);
    }

    /**
     * Remove client socket from the server
     * @param clientSocket
     * @throws IOException
     */
    public void removeClientSocket(Socket clientSocket) throws IOException {
        clientSocket.close();
        // clientSockets.set(clientSockets.indexOf(clientSocket), null);
        clientSockets.remove(clientSocket);
    }

    /**
     * Get client socket in server based on number
     * @param num identity number of client to be retreived
     * @return desired client socket
     */
    public Socket getClientSocketFromNum(int num) {
        for (int i = 0; i < serversNetworkInfo.size(); i++) {
            if (serversNetworkInfo.get(i) == null)
                continue;
            if (serversNetworkInfo.get(i).getNum() == num)
                return clientSockets.get(i);
        }
        return null;
    }

    /**
     * Get elements except null in server network info list
     * @param arrList: list containing server network info
     * @return non-null elements
     */
    private int getNotNullElements(ArrayList<ServerNetworkInfo> arrList) {
        int count = 0;
        for (ServerNetworkInfo element : arrList) {
            if (element != null) {
                count++;
            }
        }
        return count;
    }

    /**
     * Check if the new client has a valid name
     * @param name
     * @return true or false
     */
    public boolean isNameValid(String name) {
        for (ServerNetworkInfo clientNetworkInfo : serversNetworkInfo) {
            if (clientNetworkInfo != null) {
                if (clientNetworkInfo.getName().equals(name)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Get all clients network info as a String
     * @return built String containing clients network info
     */
    public String getAllClientsNetworkInfo() {
        StringBuilder clientInfo = new StringBuilder();
        int c = 0;
        for (int i = 0; i < serversNetworkInfo.size(); i++) {
            if (serversNetworkInfo.get(i) != null) {
                clientInfo.append(serversNetworkInfo.get(i).toString());
                if (c < getNotNullElements(serversNetworkInfo) - 1)
                    clientInfo.append(";");
                c++;
            }
        }
        return clientInfo.toString();
    }

    /**
     * Get server network info based on their name
     * @param name
     * @return info of the server network
     */
    public ServerNetworkInfo getServerNetworkInfoFromName(String name) {
        for (int i = 0; i < serversNetworkInfo.size(); i++) {
            if (serversNetworkInfo.get(i).getName().equals(name))
                return serversNetworkInfo.get(i);
        }
        return null;
    }

    /**
     * Get server network info based on their identity number
     * @param num
     * @return info of the server network
     */
    public ServerNetworkInfo getServerNetworkInfoFromNum(int num) {
        for (ServerNetworkInfo clientNetworkInfo : serversNetworkInfo) {
            if (clientNetworkInfo != null && clientNetworkInfo.getNum() == num)
                return clientNetworkInfo;
        }
        return null;
    }

    /**
     * Mainting communication between clients
     * @throws IOException
     */
    public void communicateAllClients() throws IOException {
        while (!clientSockets.isEmpty()) {
            for (Socket clientSocket : clientSockets) {
                communicate(clientSocket);
                in.close();
                out.close();
                // break;
            }
        }

    }

    /**
     * Create communication chanel for each client
     * @param clientSocket: client to start communicating
     * @throws IOException
     */
    public void communicate(Socket clientSocket) throws IOException { // Client client
        String inputLine;
        setOut(new PrintWriter(clientSocket.getOutputStream(), true));
        setIn(new BufferedReader(new InputStreamReader(clientSocket.getInputStream())));
        while ((inputLine = in.readLine()) != null) {
            System.out.println(inputLine);
            if (inputLine.equalsIgnoreCase("Bye")) {
                removeClientSocket(clientSocket);
                break;
            }

        }

    }

    /**
     * Close server
     * @throws IOException
     */
    public void close() throws IOException {
        in.close();
        out.close();
        serverSocket.close();
    }

    /**
     * Get client sockets in the list
     * @return list of sockets
     */
    public ArrayList<Socket> getClientSockets() {
        return clientSockets;
    }

    /**
     * Setting list of client sockets
     * @param clientSockets
     */
    public void setClientSockets(ArrayList<Socket> clientSockets) {
        this.clientSockets = clientSockets;
    }
}
