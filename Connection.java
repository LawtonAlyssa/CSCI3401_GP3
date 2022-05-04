/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author aml05
 */
public class Connection {

    private Client client;

    private Server server;

    private Socket clientSocket;
    private String serverHostname;
    private LinkedBlockingQueue<ThreadMessage> queue = new LinkedBlockingQueue<>();

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public Connection(Client client, String[] args, int portNum) throws Exception {
        this.client = client;
        this.serverHostname = client.getClientNetworkInfo().getIpAddr();
        if (args.length > 0)
            this.serverHostname = args[0];
        setServerIO();
        // client.sendClientInfo();
        client.communicate();
    }

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public Connection(Server server) throws UnknownHostException, IOException {
        this.server = server;
        setClientSocket();
        server.getFile().createNewFile();
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public void setClientSocket() {
        while (true) {

            System.out.println("Waiting for connection.....");

            try {
                clientSocket = server.getServerSocket().accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }
            System.out.println("Connection successful");
            System.out.println("Waiting for input.....");
            new ClientThread(clientSocket, server, queue);
        }
    }

    public String getServerHostname() {
        return serverHostname;
    }

    public void setServerIO() {
        System.out.println("Attemping to connect to host " + serverHostname + " on port " + 12345 + ".");
        try {
            client.setServerSocket(new Socket(serverHostname, 12345));
            client.setOut(new PrintWriter(client.getServerSocket().getOutputStream(), true));
            client.setIn(new BufferedReader(new InputStreamReader(client.getServerSocket().getInputStream())));
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: " + serverHostname);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: " + serverHostname);
            System.exit(1);
        }
    }

    public void close() throws IOException {
        // client.close();
        client.getServerIn().close();
        // System.out.println("IN:" + client.getServerIn());
        client.getServerOut().close();
        // System.out.println("OUT:" + client.getServerOut());
        client.getServerSocket().close();
        // System.out.println("SOCKET:" + client.getServerSocket());
    }

}

class ClientThread extends Thread {
    private Socket clientSocket;
    private Server server;
    private BufferedReader in;
    private PrintWriter out;
    private NetworkInfo clientNetworkInfo;
    private LinkedBlockingQueue<ThreadMessage> queue;

    public ClientThread(Socket clientSocket, Server server, LinkedBlockingQueue<ThreadMessage> queue) {
        this.clientSocket = clientSocket;
        this.server = server;
        this.in = null;
        this.out = null;
        this.queue = queue;
        start();
    }

    public boolean handleClientSenderInput(String userInput) throws IOException, InterruptedException {
        Message msg = Message.parse(userInput);
        boolean exit = false;
        String[] tokens;
        int clientSender, clientReceiver;
        String outLabel = msg.getLabel(), outContent = "";
        switch (msg.getLabel()) {
            case "client_info":
                clientNetworkInfo = NetworkInfo.parse(msg.getContent());
                int clientNum = server.pickClientSocketIndex(clientSocket);
                clientNetworkInfo.setNum(clientNum);
                server.addClientNetworkInfo(clientNetworkInfo);
                server.addClientSocket(clientSocket, clientNetworkInfo);
                for (NetworkInfo clientRecipent : server.getClientsNetworkInfo()) {
                    if(clientNum!=clientRecipent.getNum()) {
                        // System.out.println("SENT: " + clientRecipent.getName());
                        queue.put(new ThreadMessage(msg.getLabel(), clientNetworkInfo.getName(), clientNum, clientRecipent.getNum()));
                    }
                }
                // System.out.println("INITIAL:" + clientSocket);
                break;
            case "print_clients":
                outContent = server.getAllClientsNetworkInfo();
                sendOut(outLabel, outContent);
                break;
            case "help":
                break;
            case "request":
                System.out.println("RECEIVED REQUEST IN SERVER - CLIENT SENDER THREAD");
                int[] clientReceiverNums = getClientNums(msg.getContent());
                if (clientReceiverNums==null) {
                    sendOut("error", "Client name not found...");
                }
                else {
                    for (int clientReceiverNum : clientReceiverNums) {
                        queue.put(new ThreadMessage(outLabel, outContent, clientNetworkInfo.getNum(), clientReceiverNum));
                    }
                }
                break;
            case "request_resp":
                tokens = msg.getContent().split(" ");
                clientSender = clientNetworkInfo.getNum(); // respondee
                clientReceiver = server.getClientsNetworkInfoFromName(tokens[2]).getNum(); // requester
                outContent = msg.getContent();
                queue.put(new ThreadMessage(outLabel, outContent, clientSender, clientReceiver));
                break;
            case "leave":
                tokens = msg.getContent().split("-", 3);
                System.out.println(server.getClientsNetworkInfo());
                clientSender = server.getClientsNetworkInfoFromName(tokens[0]).getNum(); //client leaving
                for (String clientReceiverName : tokens[1].split("-")) {
                    clientReceiver = server.getClientsNetworkInfoFromName(clientReceiverName).getNum();
                    outContent = tokens[2];
                    queue.put(new ThreadMessage(outLabel, outContent, clientSender, clientReceiver));
                }
                break;
            case "leave_server":
                server.removeClientSocket(server.getClientSocketFromNum(clientNetworkInfo.getNum()));
                server.removeClientNetworkInfo(clientNetworkInfo);
                break;
            case "close":
                receivedIn(userInput);
                clientSender = clientNetworkInfo.getNum(); // client closing socket
                System.out.println("CLIENT NTWK INFO:" + server.getClientsNetworkInfo());
                for (NetworkInfo clientReceiverNetworkInfo :server.getClientsNetworkInfo()) {
                    if (clientReceiverNetworkInfo!=clientNetworkInfo) {
                        clientReceiver = clientReceiverNetworkInfo.getNum();
                        System.out.println("INSTRUCT TO REMOVE"+clientReceiver);
                        queue.put(new ThreadMessage(msg.getLabel(), "", clientSender, clientReceiver));
                        // System.out.println("BEFORE REMOVAL:" + server.getClientSockets() + 
                        //     "\n:" + server.getClientsNetworkInfo());
                        // server.removeClientSocket(server.getClientSocketFromNum(clientNetworkInfo.getNum()));
                        // server.removeClientNetworkInfo(clientNetworkInfo);
                        // System.out.println("AFTER REMOVAL:" + server.getClientSockets() + 
                        //     "\n" + server.getClientsNetworkInfo());
                    }  
                }
                // close();
                // System.out.println("SERVER SOCKETS:" + server.getClientSockets());
                // System.out.println("CLIENT SENDER SOCKET:" + clientSocket);
                exit = true;
                break;
            case "msg":
                tokens = msg.getContent().split("-", 3);
                clientSender = server.getClientsNetworkInfoFromName(tokens[0]).getNum();
                for (String clientReceiverName : tokens[1].split("-")) {
                    clientReceiver = server.getClientsNetworkInfoFromName(clientReceiverName).getNum();
                    queue.put(new ThreadMessage(msg.getLabel(), tokens[2], clientSender, clientReceiver));
                }
                break;
            default:
                System.out.println("label not found");
                break;
        }
        receivedIn(userInput);
        return !exit;
    }

    public boolean handleClientReceiverInput(ThreadMessage thrdMsg) throws IOException {
        boolean exit = false;
        String outLabel = thrdMsg.getLabel(), outContent = "";
        System.out.println("LABEL:" + outLabel);
        switch (thrdMsg.getLabel()) {
            case "client_info":
                String senderName = server.getClientsNetworkInfoFromNum(thrdMsg.getClientSender()).getName();
                outContent = senderName; 
                break;
            case "request":
                System.out.println("RECEIVED REQUEST IN SERVER - CLIENT RECEIVER THREAD");
                outContent = server.getClientsNetworkInfoFromNum(thrdMsg.getClientSender()).getName();
                break;
            case "request_resp":
                outContent = thrdMsg.getContent();
                break;
            case "leave":
                outContent = server.getClientsNetworkInfoFromNum(thrdMsg.getClientSender()).getName();
                break;
            case "close":
                System.out.println("RECEIVED CLOSE IN SERVER - CLIENT RECEIVER THREAD");
                // exit = true;
                break;
            default:
                outLabel = "msg";
                String clientSender = server.getClientsNetworkInfoFromNum(thrdMsg.getClientSender()).getName();
                String clientReceiver = server.getClientsNetworkInfoFromNum(thrdMsg.getClientReceiver()).getName();
                outContent = Message.build(clientSender, clientReceiver, thrdMsg.getContent());
                break;
        }
        sendOut(outLabel, outContent);
        return !exit;
    }

    public int[] getClientNums(String input) {
        String[] clientNames = input.split(";");
        int[] clientNums = new int[clientNames.length];
        for (int i = 0; i < clientNums.length; i++) {
            System.out.println("GETTING CLIENT NUM: " + clientNames[i]);
            if (server.getClientsNetworkInfoFromName(clientNames[i])!=null) clientNums[i] = server.getClientsNetworkInfoFromName(clientNames[i]).getNum();
            else return null;
        }
        return clientNums;
    }

    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: ");
            System.exit(1);
        }

        
        try {
            while (true) {
                if(in.ready()){
                    String inputLine = in.readLine();
                    if (inputLine != null) {
                        if (!handleClientSenderInput(inputLine)) break;
                    }
                }
                if(queue.size() > 0) {
                    ThreadMessage thrdMsg = queue.peek();
                    if (clientNetworkInfo != null && thrdMsg != null && thrdMsg.getClientReceiver()==clientNetworkInfo.getNum()) {
                        queue.take(); // removes from queue
                        if (!handleClientReceiverInput(thrdMsg)) break;
                    }
                }
            }
            close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() throws IOException {
        out.close();
        in.close();
        // clientSocket.close();
        System.out.println("REMOVING CLIENT#" + clientNetworkInfo.getNum());
        // server.removeClientNetworkInfo(clientNetworkInfo);
        server.removeClientSocket(clientSocket);
        server.getServerSocket().close();
    }

    public void receivedIn(String inputLine) throws IOException {
        if (clientNetworkInfo != null) {
            writeToFile("Received from Client " + clientNetworkInfo.getNum() + ": " + inputLine + "\n");
            System.out.println("Client " + clientNetworkInfo.getNum() + ": " + inputLine);
        }
    }

    public void writeToFile(String line) throws IOException {
        FileWriter fw = new FileWriter(server.getFile().getAbsoluteFile(), true);
        fw.write(line);
        fw.close();
        // System.out.println("Wrote to file.");
    }

    public void sendOut(String label, String content) throws IOException {
        String line = label + "-" + content;
        // System.out.println("LINE:"+line);
        out.println(line);
        writeToFile("Controller Sent: " + line + "\n");
    }
}

/**
 * ThreadMessage
 */
class ThreadMessage extends Message {
    private int clientSender;
    private int clientReceiver;

    public ThreadMessage(String label, String content, int clientSender, int clientReceiver) {
        super(label, content);
        this.clientSender = clientSender;
        this.clientReceiver = clientReceiver;
    }

    public ThreadMessage(int clientSender, int clientReceiver) {
        super();
        this.clientSender = clientSender;
        this.clientReceiver = clientReceiver;
    }

    public int getClientSender() {
        return clientSender;
    }

    public void setClientSender(int clientSender) {
        this.clientSender = clientSender;
    }

    public int getClientReceiver() {
        return clientReceiver;
    }

    public void setClientReceiver(int clientReceiver) {
        this.clientReceiver = clientReceiver;
    }

}