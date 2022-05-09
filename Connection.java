/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author Alyssa
 * @author Truong
 */
public class Connection {
    private Server server;

    private Socket clientSocket;
    private String serverHostname;
    private LinkedBlockingQueue<ThreadMessage> queue = new LinkedBlockingQueue<>();

    /**
     * Constructor set by passing a server
     * @param server
     * @throws UnknownHostException
     * @throws IOException
     */
    public Connection(Server server) throws UnknownHostException, IOException {
        this.server = server;
        setClientSocket();
    }

    /**
     * Get client socket
     * @return client socket
     */
    public Socket getClientSocket() {
        return clientSocket;
    }

    /**
     * Setting connection with client's socket
     */
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

    /**
     * Get server hostname
     * @return server hostname
     */
    public String getServerHostname() {
        return serverHostname;
    }
}

class ClientThread extends Thread {
    private Socket clientSocket;
    private Server server;
    private BufferedReader in;
    private PrintWriter out;
    private ServerNetworkInfo serverNetworkInfo;
    private LinkedBlockingQueue<ThreadMessage> queue;

    /**
     * Constructor
     * @param clientSocket
     * @param server
     * @param queue
     */
    public ClientThread(Socket clientSocket, Server server, LinkedBlockingQueue<ThreadMessage> queue) {
        this.clientSocket = clientSocket;
        this.server = server;
        this.in = null;
        this.out = null;
        this.queue = queue;
        start();
    }

    /**
     * Handling client sender input 
     * @param userInput input from client's keyboard
     * @return true or false (exit)
     * @throws IOException
     * @throws InterruptedException
     */
    public boolean handleClientSenderInput(String userInput) throws IOException, InterruptedException {
        Message msg = Message.parse(userInput);
        boolean exit = false;
        String[] tokens;
        int clientSender, clientReceiver;
        String outLabel = msg.getLabel(), outContent = "";
        switch (msg.getLabel()) {
            case "client_info":
                serverNetworkInfo = ServerNetworkInfo.parse(msg.getContent());
                if (!server.isNameValid(serverNetworkInfo.getName())) {
                    sendOut(outLabel, "");
                    break;
                }
                sendOut("success", serverNetworkInfo.getName() + ";" + serverNetworkInfo.getNum());
                server.addServerNetworkInfo(serverNetworkInfo);
                server.addClientSocket(clientSocket, serverNetworkInfo);
                for (ServerNetworkInfo clientRecipent : server.getServerNetworkInfo()) {
                    if (clientRecipent != null) {
                        if (serverNetworkInfo.getNum() != clientRecipent.getNum()) {
                            queue.put(new ThreadMessage("join", serverNetworkInfo.getName(), serverNetworkInfo.getNum(), clientRecipent.getNum()));
                        }
                    }
                }
                break;
            case "print_clients":
                outContent = server.getAllClientsNetworkInfo();
                sendOut(outLabel, outContent);
                break;
            case "help":
                break;
            case "request":
                int[] clientReceiverNums = getClientNums(msg.getContent());
                if (clientReceiverNums == null) {
                    sendOut("error", "Client name not found...");
                } else {
                    for (int clientReceiverNum : clientReceiverNums) {
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
                tokens = msg.getContent().split(" ");
                clientSender = serverNetworkInfo.getNum(); // respondee
                clientReceiver = server.getServerNetworkInfoFromName(tokens[2]).getNum(); // requester
                outContent = msg.getContent();
                queue.put(new ThreadMessage(outLabel, outContent, clientSender, clientReceiver));
                break;
            case "leave":
                tokens = msg.getContent().split("-", 3);
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
                        queue.put(new ThreadMessage(msg.getLabel(), serverNetworkInfo.getName(), clientSender,
                                clientReceiver));
                    }
                }
                closeClient();
                break;
            case "close":
                receivedIn(userInput);
                clientSender = serverNetworkInfo.getNum(); // client closing socket
                for (ServerNetworkInfo clientReceiverNetworkInfo : server.getServerNetworkInfo()) {
                    if (clientReceiverNetworkInfo != serverNetworkInfo) {
                        clientReceiver = clientReceiverNetworkInfo.getNum();
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

    /**
     * Hanlding client receiver input
     * @param thrdMsg
     * @return true (continue) or false (exit)
     * @throws IOException
     */
    public boolean handleClientReceiverInput(ThreadMessage thrdMsg) throws IOException {
        boolean exit = false;
        String outLabel = thrdMsg.getLabel(), outContent = "", clientSender, clientReceiver;
        switch (thrdMsg.getLabel()) {
            case "join":
                String senderName = server.getServerNetworkInfoFromNum(thrdMsg.getClientSender()).getName();
                outContent = senderName;
                break;
            case "request":
                outContent = server.getServerNetworkInfoFromNum(thrdMsg.getClientSender()).getName();
                break;
            case "request_resp":
            case "leave":
            case "leave_server":
                outContent = thrdMsg.getContent();
                break;
            case "close":
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
        sendOut(outLabel, outContent);
        return !exit;
    }

    /**
     * Getting all clients's numbers by using their names
     * @param input: clients's names
     * @return
     */
    public int[] getClientNums(String input) {
        String[] clientNames = input.split(";");
        int[] clientNums = new int[clientNames.length];
        for (int i = 0; i < clientNums.length; i++) {
            if (server.getServerNetworkInfoFromName(clientNames[i]) != null)
                clientNums[i] = server.getServerNetworkInfoFromName(clientNames[i]).getNum();
            else
                return null;
        }
        return clientNums;
    }

    /**
     * Setting IO and running
     */
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

    /**
     * Close client by removing it and its info from server
     * @throws IOException
     */
    public void closeClient() throws IOException {
        server.removeClientSocket(server.getClientSocketFromNum(serverNetworkInfo.getNum()));
        server.removeServerNetworkInfo(serverNetworkInfo);
    }

    /**
     * Receive message from client and confirmation
     * @param inputLine
     * @throws IOException
     */
    public void receivedIn(String inputLine) throws IOException {
        if (serverNetworkInfo != null) {
            writeToFile("Received from Client " + serverNetworkInfo.getNum() + ": " + inputLine + "\n");
            System.out.println("Client " + serverNetworkInfo.getNum() + ": " + inputLine);
        }
    }

    /**
     * Writing to file
     * @param line
     * @throws IOException
     */
    public void writeToFile(String line) throws IOException {
        FileWriter fw = new FileWriter(server.getFile().getAbsoluteFile(), true);
        fw.write(new Timestamp(System.currentTimeMillis()) + "\n" + line);
        fw.close();
    }

    /**
     * Sending out message from controller
     * @param label
     * @param content
     * @throws IOException
     */
    public void sendOut(String label, String content) throws IOException {
        String line = label + "-" + content;
        out.println(line);
        writeToFile("Controller Sent: " + line + "\n");
    }

    /**
     * Close IO chanels and server socket
     * @throws IOException
     */
    public void close() throws IOException {
        out.close();
        in.close();
        server.getServerSocket().close();
    }
}