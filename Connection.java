/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author Alyssa
 * @author Truong
 */
public class Connection {

    // private Client client;

    private Server server;

    private Socket clientSocket;
    private String serverHostname;
    private LinkedBlockingQueue<ThreadMessage> queue = new LinkedBlockingQueue<>();

    // @SuppressWarnings("OverridableMethodCallInConstructor")
    // public Connection(Client client, String[] args, int portNum) throws Exception
    // {
    // this.client = client;
    // this.serverHostname = client.getClientNetworkInfo().getIpAddr();
    // if (args.length > 0)
    // this.serverHostname = args[0];
    // setServerIO();
    // // client.sendClientInfo();
    // client.communicate();
    // }

    // @SuppressWarnings("OverridableMethodCallInConstructor")
    public Connection(Server server) throws UnknownHostException, IOException {
        this.server = server;
        setClientSocket();
        // (server.getFile()).createNewFile();
        // server.setFile(new File("controller.txt"));

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
    /*
     * public void setServerIO() {
     * System.out.println("Attemping to connect to host " + serverHostname +
     * " on port " + 12345 + ".");
     * try {
     * // client.setServerSocket(new Socket("141.239.208.113", 3125));
     * client.setServerSocket(new Socket(serverHostname, 12345));
     * client.setOut(new PrintWriter(client.getServerSocket().getOutputStream(),
     * true));
     * client.setIn(new BufferedReader(new
     * InputStreamReader(client.getServerSocket().getInputStream())));
     * } catch (UnknownHostException e) {
     * System.err.println("Don't know about host: " + serverHostname);
     * System.exit(1);
     * } catch (IOException e) {
     * System.err.println("Couldn't get I/O for the connection to: " +
     * serverHostname);
     * System.exit(1);
     * }
     * }
     */
    /*
     * public void close() throws IOException {
     * // client.close();
     * client.getServerIn().close();
     * // System.out.println("IN:" + client.getServerIn());
     * client.getServerOut().close();
     * // System.out.println("OUT:" + client.getServerOut());
     * client.getServerSocket().close();
     * // System.out.println("SOCKET:" + client.getServerSocket());
     * }
     */

}

class ClientThread extends Thread {
    private Socket clientSocket;
    private Server server;
    private BufferedReader in;
    private PrintWriter out;
    private ServerNetworkInfo serverNetworkInfo;
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
        System.out.println("SERVER SENDER LABEL:" + msg.getLabel());
        System.out.println("SERVER SENDER MSG:" + msg.getContent());
        switch (msg.getLabel()) {
            case "client_info":
                serverNetworkInfo = ServerNetworkInfo.parse(msg.getContent());
                if (!server.isNameValid(serverNetworkInfo.getName())) {
                    sendOut(outLabel, "");
                    break;
                }
                // System.out.println("CONTINUE");
                // int clientNum = server.pickClientSocketIndex(clientSocket);
                // serverNetworkInfo.setNum(clientNum);
                // System.out.println("CLIENT NUM:" + serverNetworkInfo.getNum());
                sendOut("success", serverNetworkInfo.getName() + ";" + serverNetworkInfo.getNum());
                server.addServerNetworkInfo(serverNetworkInfo);
                server.addClientSocket(clientSocket, serverNetworkInfo);
                // System.out.println("GET CLIENT INFO:" + server.getServerNetworkInfo() +
                // "\nGET CLIENT SOCKETS:" + server.getClientSockets());
                for (ServerNetworkInfo clientRecipent : server.getServerNetworkInfo()) {
                    if (clientRecipent != null) {
                        if (serverNetworkInfo.getNum() != clientRecipent.getNum()) {
                            // System.out.println("SENT: " + clientRecipent.getName());
                            // System.out.println(serverNetworkInfo.getName());
                            queue.put(new ThreadMessage("join", serverNetworkInfo.getName(), serverNetworkInfo.getNum(), clientRecipent.getNum()));
                        }
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
                // System.out.println("RECEIVED REQUEST IN SERVER - CLIENT SENDER THREAD");
                int[] clientReceiverNums = getClientNums(msg.getContent());
                if (clientReceiverNums == null) {
                    sendOut("error", "Client name not found...");
                } else {
                    for (int clientReceiverNum : clientReceiverNums) {
                        // System.out.println("SENT TO:");
                        queue.put(
                                new ThreadMessage(outLabel, outContent, serverNetworkInfo.getNum(), clientReceiverNum));
                    }
                }
                break;
            case "broadcast":
                clientSender = serverNetworkInfo.getNum(); // respondee
                outContent = msg.getContent();
                for (ServerNetworkInfo clientRecipent : server.getServerNetworkInfo()) {
                    if (clientRecipent != null) {
                        if (serverNetworkInfo.getNum() != clientRecipent.getNum()) {
                            queue.put(new ThreadMessage(outLabel, outContent, clientSender, clientRecipent.getNum()));
                        }
                    }
                }
                break;
            case "request_resp":
                // System.out.println("RECEIVED REQUEST RESPONSE IN SERVER - CLIENT SENDER
                // THREAD");
                tokens = msg.getContent().split(" ");
                clientSender = serverNetworkInfo.getNum(); // respondee
                clientReceiver = server.getServerNetworkInfoFromName(tokens[2]).getNum(); // requester
                outContent = msg.getContent();
                // System.out.println("CLIENT S:" + clientSender + " R:"+clientReceiver);
                queue.put(new ThreadMessage(outLabel, outContent, clientSender, clientReceiver));
                break;
            case "leave":
                tokens = msg.getContent().split("-", 3);
                // System.out.println(server.getServerNetworkInfo());
                String clientSenderName = tokens[0];
                clientSender = server.getServerNetworkInfoFromName(clientSenderName).getNum(); // client leaving
                for (String clientReceiverName : tokens[1].split("-")) {
                    clientReceiver = server.getServerNetworkInfoFromName(clientReceiverName).getNum();
                    outContent = clientSenderName;
                    queue.put(new ThreadMessage(outLabel, outContent, clientSender, clientReceiver));
                }
                break;
            case "leave_server":
                clientSender = serverNetworkInfo.getNum(); // client leaving server
                for (ServerNetworkInfo clientReceiverNetworkInfo : server.getServerNetworkInfo()) {
                    if (clientReceiverNetworkInfo != serverNetworkInfo) {
                        clientReceiver = clientReceiverNetworkInfo.getNum();
                        System.out.println("INFORM:" + clientReceiver);
                        queue.put(new ThreadMessage(msg.getLabel(), serverNetworkInfo.getName(), clientSender,
                                clientReceiver));
                    }
                }
                closeClient();
                break;
            case "close":
                receivedIn(userInput);
                clientSender = serverNetworkInfo.getNum(); // client closing socket
                System.out.println("CLIENT NTWK INFO:" + server.getServerNetworkInfo());
                for (ServerNetworkInfo clientReceiverNetworkInfo : server.getServerNetworkInfo()) {
                    if (clientReceiverNetworkInfo != serverNetworkInfo) {
                        clientReceiver = clientReceiverNetworkInfo.getNum();
                        System.out.println("INSTRUCT TO REMOVE CLIENT:" + clientReceiver);
                        queue.put(new ThreadMessage(msg.getLabel(), "", clientSender, clientReceiver));
                    }
                }
                exit = true;
                break;
            case "msg":
                tokens = msg.getContent().split("-", 3);
                clientSender = server.getServerNetworkInfoFromName(tokens[0]).getNum();
                for (String clientReceiverName : tokens[1].split("-")) {
                    clientReceiver = server.getServerNetworkInfoFromName(clientReceiverName).getNum();
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
        String outLabel = thrdMsg.getLabel(), outContent = "", clientSender, clientReceiver;
        System.out.println("SEVER RECEIVER LABEL:" + outLabel);
        System.out.println("SERVER RECEIVER MSG:" + thrdMsg.getContent());
        switch (thrdMsg.getLabel()) {
            // case "client_info":
            // String senderName =
            // server.getServerNetworkInfoFromNum(thrdMsg.getClientSender()).getName();
            // outContent = senderName;
            // break;
            case "join":
                String senderName = server.getServerNetworkInfoFromNum(thrdMsg.getClientSender()).getName();
                outContent = senderName;
                break;
            case "request":
                // System.out.println("RECEIVED REQUEST IN SERVER - CLIENT RECEIVER THREAD");
                outContent = server.getServerNetworkInfoFromNum(thrdMsg.getClientSender()).getName();
                // System.out.println("NAME SENDING TO:" + outContent);
                break;
            case "request_resp":
            case "leave":
            case "leave_server":
                outContent = thrdMsg.getContent();
                break;
            case "close":
                // System.out.println("RECEIVED CLOSE IN SERVER - CLIENT RECEIVER THREAD");
                exit = true;
                break;
            case "broadcast":
                clientSender = server.getServerNetworkInfoFromNum(thrdMsg.getClientSender()).getName();
                clientReceiver = server.getServerNetworkInfoFromNum(thrdMsg.getClientReceiver()).getName();
                outContent = Message.build(clientSender, clientReceiver, thrdMsg.getContent());
                break;
            case "msg":
                clientSender = server.getServerNetworkInfoFromNum(thrdMsg.getClientSender()).getName();
                clientReceiver = server.getServerNetworkInfoFromNum(thrdMsg.getClientReceiver()).getName();
                outContent = Message.build(clientSender, clientReceiver, thrdMsg.getContent());
                break;
            default:
                System.out.println("Label not found...");
                break;
        }
        System.out.println("SENDING OUT:" + outLabel + "-" + outContent);
        sendOut(outLabel, outContent);
        return !exit;
    }

    public int[] getClientNums(String input) {
        String[] clientNames = input.split(";");
        int[] clientNums = new int[clientNames.length];
        for (int i = 0; i < clientNums.length; i++) {
            // System.out.println("GETTING CLIENT NUM FOR: " + clientNames[i]);
            if (server.getServerNetworkInfoFromName(clientNames[i]) != null)
                clientNums[i] = server.getServerNetworkInfoFromName(clientNames[i]).getNum();
            else
                return null;
        }
        // System.out.println("NUMS:" + clientNums);
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
                if (in.ready()) {
                    String inputLine = in.readLine();
                    if (inputLine != null) {
                        if (!handleClientSenderInput(inputLine))
                            break;
                    }
                }
                if (queue.size() > 0) {
                    ThreadMessage thrdMsg = queue.peek();
                    if (serverNetworkInfo != null && thrdMsg != null
                            && thrdMsg.getClientReceiver() == serverNetworkInfo.getNum()) {
                        // System.out.println("QUEUE TAKES");
                        queue.take(); // removes from queue
                        if (!handleClientReceiverInput(thrdMsg))
                            break;
                    }
                }
            }
            System.out.println("");
            close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeClient() throws IOException {
        // System.out.println("BEFORE LEAVE:" + server.getServerNetworkInfo() +
        // "\n" + server.getClientSockets());
        System.out.println("CLOSING CLIENT:" + serverNetworkInfo.getNum() + ":" + serverNetworkInfo.getName());
        server.removeClientSocket(server.getClientSocketFromNum(serverNetworkInfo.getNum()));
        server.removeServerNetworkInfo(serverNetworkInfo);
        // System.out.println("AFTER LEAVE:" + server.getServerNetworkInfo() +
        // "\n" + server.getClientSockets());
    }

    public void receivedIn(String inputLine) throws IOException {
        if (serverNetworkInfo != null) {
            writeToFile("Received from Client " + serverNetworkInfo.getNum() + ": " + inputLine + "\n");
            System.out.println("Client " + serverNetworkInfo.getNum() + ": " + inputLine);
        }
    }

    public void writeToFile(String line) throws IOException {
        FileWriter fw = new FileWriter(server.getFile().getAbsoluteFile(), true);
        fw.write(new Timestamp(System.currentTimeMillis()) + "\n" + line);
        fw.close();
        // System.out.println("Wrote to file.");
    }

    public void sendOut(String label, String content) throws IOException {
        String line = label + "-" + content;
        // System.out.println("LINE:"+line);
        out.println(line);
        writeToFile("Controller Sent: " + line + "\n");
    }

    public void close() throws IOException {
        out.close();
        in.close();
        // clientSocket.close();
        System.out.println("REMOVING CLIENT#" + serverNetworkInfo.getNum());
        // closeClient();
        server.getServerSocket().close();
    }
}