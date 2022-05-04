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
 * @author aml05
 */
public class Server {

    private ServerSocket serverSocket;
    private int portNum;
    private PrintWriter out;
    private BufferedReader in;
    private ArrayList<ServerNetworkInfo> serversNetworkInfo;
    private File file = new File("controller.txt");
    private ArrayList<Socket> clientSockets;

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
    }

    public void clearFile() throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(file.getName());
        pw.print("");
        pw.close();
    }

    public Server() {
        serverSocket = null;
        portNum = 12345;
        out = null;
        in = null;
        serversNetworkInfo = new ArrayList<>();
    }

    public int getPortNum() {
        return portNum;
    }

    public void setPortNum(int portNum) {
        this.portNum = portNum;
        setServerSocket();
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setServerSocket() {
        try {
            serverSocket = new ServerSocket(portNum);
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + portNum + ".");
            System.exit(1);
        }
    }

    public PrintWriter getOut() {
        return out;
    }

    public void setOut(PrintWriter out) {
        this.out = out;
    }

    public ArrayList<ServerNetworkInfo> getServerNetworkInfo() {
        return serversNetworkInfo;
    }

    public void setServerNetworkInfo(ArrayList<ServerNetworkInfo> serverNetworkInfo) {
        this.serversNetworkInfo = serverNetworkInfo;
    }

    public BufferedReader getIn() {
        return in;
    }

    public void setIn(BufferedReader in) {
        this.in = in;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public void addServerNetworkInfo(ServerNetworkInfo serverNetworkInfo) {
        // for (int i = 0; i < serversNetworkInfo.size(); i++) {
        //     if (serversNetworkInfo.get(i) == null) { // replace null
        //         serversNetworkInfo.set(i, serverNetworkInfo);
        //         return;
        //     }
        // }
        serversNetworkInfo.add(serverNetworkInfo); // append if length=0 or no null elements
        
    }

    public void addClientSocket(Socket clientSocket, NetworkInfo clientNetworkInfo) {
        // for (int i = 0; i < clientSockets.size(); i++) {
        //     if (clientSockets.get(i) == null) { // replace null
        //         clientSockets.set(i, clientSocket);
        //         return;
        //     }
        // }
        clientSockets.add(clientSocket); // append if length=0 or no null elements
        // if (clientNetworkInfo.getNum()<clientSockets.size())
        // clientSockets.set(clientNetworkInfo.getNum(), clientSocket);
        // else clientSockets.add(clientSocket);
    }

    // public int pickClientSocketIndex(Socket clientSocket) {
    //     for (int i = 0; i < clientSockets.size(); i++) {
    //         if (clientSockets.get(i) == null)
    //             return i;
    //     }
    //     return clientSockets.size();
    // }

    public void removeServerNetworkInfo(ServerNetworkInfo serverNetworkInfo) {
        // System.out.println("CLIENT NTWK INFO BEFORE:" + clientsNetworkInfo);
        // serversNetworkInfo.set(serversNetworkInfo.indexOf(serverNetworkInfo), null);
        // System.out.println("CLIENT NTWK INFO AFTER:" + clientsNetworkInfo);
        serversNetworkInfo.remove(serverNetworkInfo);
    }

    public void removeClientSocket(Socket clientSocket) throws IOException {
        clientSocket.close();
        // clientSockets.set(clientSockets.indexOf(clientSocket), null);
        clientSockets.remove(clientSocket);
    }

    public Socket getClientSocketFromNum(int num) {
        for (int i = 0; i < serversNetworkInfo.size(); i++) {
            if (serversNetworkInfo.get(i) == null)
                continue;
            if (serversNetworkInfo.get(i).getNum() == num)
                return clientSockets.get(i);
        }
        return null;
    }

    private int getNotNullElements(ArrayList<ServerNetworkInfo> arrList) {
        int count = 0;
        for (ServerNetworkInfo element : arrList) {
            if (element != null) {
                count++;
            }
        }
        return count;
    }

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

    public ServerNetworkInfo getServerNetworkInfoFromName(String name) {
        for (int i = 0; i < serversNetworkInfo.size(); i++) {
            if (serversNetworkInfo.get(i).getName().equals(name))
                return serversNetworkInfo.get(i);
        }
        // for (NetworkInfo clientNetworkInfo : clientsNetworkInfo) {

        // // System.out.println("MATCH:" + clientNetworkInfo.getName() + "--" + name);
        // if ((clientNetworkInfo.getName()).equals(name)) return clientNetworkInfo;
        // }
        return null;
    }

    public ServerNetworkInfo getServerNetworkInfoFromNum(int num) {
        // System.out.println("CLIENT NTWK INFO:" + getClientsNetworkInfo());
        for (ServerNetworkInfo clientNetworkInfo : serversNetworkInfo) {
            // System.out.println("MATCH:" + clientNetworkInfo.getNum() + ":" + num);
            if (clientNetworkInfo!=null && clientNetworkInfo.getNum() == num)
                return clientNetworkInfo;
        }
        return null;
    }

    public void communicateAllClients() throws IOException {
        /*
         * for (Client client : clients) {
         * out.println("Connected to Controller");
         * System.out.println(client);
         * communicate();
         * }
         */
        while (!clientSockets.isEmpty()) {
            for (Socket clientSocket : clientSockets) {
                communicate(clientSocket);
                in.close();
                out.close();
                // break;
            }
        }

    }

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

    public void close() throws IOException {
        in.close();
        out.close();
        serverSocket.close();
    }

    public ArrayList<Socket> getClientSockets() {
        return clientSockets;
    }

    public void setClientSockets(ArrayList<Socket> clientSockets) {
        this.clientSockets = clientSockets;
    }
}
