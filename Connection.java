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
        clientSocket.close();
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

    public boolean handleUserInput(String userInput) throws IOException, InterruptedException {
        Message msg = Message.parse(userInput);
        boolean exit = false;
        String[] tokens;
        int clientSender, clientReceiver;
        switch (msg.getLabel()) {
            case "client_info":
                clientNetworkInfo = NetworkInfo.parse(msg.getContent());
                int clientNum = server.pickClientSocketIndex(clientSocket);
                clientNetworkInfo.setNum(clientNum);
                server.addClientNetworkInfo(clientNetworkInfo);
                server.addClientSocket(clientSocket, clientNetworkInfo);
                break;
            case "print_clients":
                sendOut(msg.getLabel() + "-" + server.getClientNetworkInfo());
                break;
            case "request":
                System.out.println("RECEIVED REQUEST IN SERVER - ORIGINAL THREAD");
                int[] clientReceiverNums = getClientNums(msg.getContent());
                for (int clientReceiverNum : clientReceiverNums) {
                    queue.put(new ThreadMessage(msg.getLabel(), "", clientNetworkInfo.getNum(), clientReceiverNum));
                }
                break;
            case "request_resp":
                tokens = msg.getContent().split(" ");
                // System.out.println("Len" + tokens.length);
                clientSender = clientNetworkInfo.getNum();
                clientReceiver = server.getClientNetworkInfoFromName(tokens[2]).getNum();
                // System.out.println("Sender" + clientSender);
                queue.put(new ThreadMessage(msg.getLabel(), msg.getContent(), clientSender, clientReceiver));
                break;
            case "leave":
                break;
            case "close":
                exit = true;
                break;
            case "msg":
                tokens = msg.getContent().split("-", 3);
                clientSender = server.getClientNetworkInfoFromName(tokens[0]).getNum();
                for (String clientReceiverName : tokens[1].split("-")) {
                    clientReceiver = server.getClientNetworkInfoFromName(clientReceiverName).getNum();
                    queue.put(new ThreadMessage(msg.getLabel(), tokens[2], clientSender, clientReceiver));
                }
                break;
            default:
                break;
        }
        receivedIn(userInput);
        return !exit;
    }


    public boolean handleOtherUserInput(ThreadMessage thrdMsg) {
        boolean exit = false;
        String outLabel = thrdMsg.getLabel(), outContent = "";
        switch (thrdMsg.getLabel()) {
            case "request":
                System.out.println("RECEIVED REQUEST IN SERVER - OTHER THREAD");
                outContent = server.getClientNetworkInfoFromNum(thrdMsg.getClientSender()).getName();
                break;
            case "request_resp":
                outContent = thrdMsg.getContent();
                break;
            default:
                outLabel = "msg";
                String clientSender = server.getClientNetworkInfoFromNum(thrdMsg.getClientSender()).getName();
                String clientReceiver = server.getClientNetworkInfoFromNum(thrdMsg.getClientReceiver()).getName();
                outContent = Message.build(clientSender, clientReceiver, thrdMsg.getContent());
                break;
        }
        out.println(outLabel + "-" + outContent);
        return !exit;
    }

    public int[] getClientNums(String input) {
        String[] clientNames = input.split(";");
        int[] clientNums = new int[clientNames.length];
        for (int i = 0; i < clientNums.length; i++) {
            System.out.println("Getting Client Num: " + clientNames[i]);
            clientNums[i] = server.getClientNetworkInfoFromName(clientNames[i]).getNum();
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
                        if (!handleUserInput(inputLine)) break;
                    }
                }
                if(queue.size() > 0) {
                    ThreadMessage thrdMsg = queue.peek();
                    if (clientNetworkInfo != null && thrdMsg != null && thrdMsg.getClientReceiver()==clientNetworkInfo.getNum()) {
                        queue.take(); // removes from queue
                        if (!handleOtherUserInput(thrdMsg)) break;
                        
                    }
                }
            }
            server.removeClientNetworkInfo(clientNetworkInfo);
            server.removeClientSocket(clientSocket);
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        System.out.println("Wrote to file.");
    }

    public void sendOut(String outputLine) throws IOException {
        out.println(outputLine);
        writeToFile("Controller Sent: " + outputLine + "\n");
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