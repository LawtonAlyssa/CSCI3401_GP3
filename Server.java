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
    private ArrayList<NetworkInfo> clientsNetworkInfo;
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
        clientsNetworkInfo = new ArrayList<>();
        clientSockets = new ArrayList<>();
        // PrintWriter pw = new PrintWriter(file.getName());
        // pw.print("");
        // other operations
        // pw.close();
    }

    public Server() {
        serverSocket = null;
        portNum = 12345;
        out = null;
        in = null;
        clientsNetworkInfo = new ArrayList<>();
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

    public ArrayList<NetworkInfo> getClientsNetworkInfo() {
        return clientsNetworkInfo;
    }

    public void setClientsNetworkInfo(ArrayList<NetworkInfo> clientsNetworkInfo) {
        this.clientsNetworkInfo = clientsNetworkInfo;
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

    public void addClientNetworkInfo(NetworkInfo networkInfo) {
        clientsNetworkInfo.add(networkInfo);
    }
    
    public void addClientSocket(Socket clientSocket, NetworkInfo clientNetworkInfo) {
        if (clientNetworkInfo.getNum()<clientSockets.size()) clientSockets.set(clientNetworkInfo.getNum(), clientSocket);
        else clientSockets.add(clientSocket);
    }

    public int pickClientSocketIndex(Socket clientSocket) {
        for (int i = 0; i < clientSockets.size(); i++) {
            if (clientSockets.get(i)==null) return i;
        }
        return clientSockets.size();
    }

    public void removeClientNetworkInfo(NetworkInfo networkInfo) {
        System.out.println("CLIENT NTWK INFO BEFORE:" + clientsNetworkInfo);
        clientsNetworkInfo.set(clientsNetworkInfo.indexOf(networkInfo), null);
        System.out.println("CLIENT NTWK INFO AFTER:" + clientsNetworkInfo);
    }

    public void removeClientSocket(Socket clientSocket) throws IOException {
        clientSocket.close();
        clientSockets.set(clientSockets.indexOf(clientSocket), null);
    }

    public Socket getClientSocketFromNum(int num) {
        for (int i = 0; i < clientsNetworkInfo.size(); i++) {
            if (clientsNetworkInfo.get(i)==null) continue;
            if (clientsNetworkInfo.get(i).getNum()==num) return clientSockets.get(i);
        }
        return null;
    }
    
    public String getAllClientsNetworkInfo() {
        StringBuilder clientInfo = new StringBuilder();
        for (int i = 0; i < clientsNetworkInfo.size(); i++) {
            clientInfo.append(clientsNetworkInfo.get(i).toString());
            if (i < clientsNetworkInfo.size()-1) clientInfo.append(";");
        }
        return clientInfo.toString();
    }
    
    public NetworkInfo getClientsNetworkInfoFromName(String name) {
        for (int i = 0; i < clientsNetworkInfo.size(); i++) {
            if (clientsNetworkInfo.get(i).getName().equals(name)) return clientsNetworkInfo.get(i);
        }
        // for (NetworkInfo clientNetworkInfo : clientsNetworkInfo) {
            
        //     // System.out.println("MATCH:" + clientNetworkInfo.getName() + "--" + name);
        //     if ((clientNetworkInfo.getName()).equals(name)) return clientNetworkInfo;
        // }
        return null;
    }

    public NetworkInfo getClientsNetworkInfoFromNum(int num) {
        for (NetworkInfo clientNetworkInfo : clientsNetworkInfo) {
            if (clientNetworkInfo.getNum()==num) return clientNetworkInfo;
        }
        return null;
    }

    public void communicateAllClients() throws IOException {
    /*
        for (Client client : clients) {
            out.println("Connected to Controller");
            System.out.println(client);
            communicate();
        }
    */
        while(!clientSockets.isEmpty()){
            for (Socket clientSocket : clientSockets) {
                communicate(clientSocket);
                in.close();
                out.close();
                // break;
            }
        }
        
    }

    public void communicate(Socket clientSocket) throws IOException { //Client client
        String inputLine;
        setOut(new PrintWriter(clientSocket.getOutputStream(), true));
        setIn(new BufferedReader(new InputStreamReader(clientSocket.getInputStream())));
        while((inputLine = in.readLine()) != null) {
            System.out.println(inputLine);
            if(inputLine.equalsIgnoreCase("Bye")) {
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
