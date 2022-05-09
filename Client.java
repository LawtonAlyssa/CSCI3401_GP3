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
public class Client {
    private Socket serverSocket = null;
    private PrintWriter serverOut = null;
    private BufferedReader serverIn = null;
    private NetworkInfo clientNetworkInfo;
    private LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private boolean enableKeyboard = false;
    private ArrayList<String> clientRecipients = new ArrayList<>();
    private File file = null;
    // private int[] ports = { 11111, 22222, 33333, 44444, 55555 };

    /**
     * Constructor for Client class with 2 params
     * @param ipAddr client's IP address
     * @param portNum client's port number
     * @throws UnknownHostException
     */
    public Client(String ipAddr, int portNum) throws UnknownHostException {
        clientNetworkInfo = new NetworkInfo("", ipAddr, portNum);
        new KeyboardThread(queue);
        
    }

    /**
     * Constructor for Client class with 1 param
     * @param ipAddr client's IP address
     * @throws UnknownHostException
     */
    public Client(String ipAddr) throws UnknownHostException {
        clientNetworkInfo = new NetworkInfo(11111);
        new KeyboardThread(queue);
    }

    /**
     * Get Server Socket
     * @return socket
     */
    public Socket getServerSocket() {
        return serverSocket;
    }

    /**
     * Set Server Socket field
     * @param serverSocket socket to be set to Server Socket
     */
    public void setServerSocket(Socket serverSocket) {
        this.serverSocket = serverSocket;
    }

    /**
     * Get object to send message
     * @return PrintWriter object
     */
    public PrintWriter getOut() {
        return serverOut;
    }

    /**
     * Set object to send message
     * @param serverOut
     */
    public void setOut(PrintWriter serverOut) {
        this.serverOut = serverOut;
    }

    /**
     * Get object to read message
     * @return BufferedReader object
     */
    public BufferedReader getIn() {
        return serverIn;
    }
    
    /**
     * Set object to read message
     * @param in
     */
    public void setIn(BufferedReader in) {
        this.serverIn = in;
    }

    /**
     * Get object to send message to server
     * @return PrintWriter object
     */
    public PrintWriter getServerOut() {
        return serverOut;
    }

    /**
     * Set object to send message to server
     * @param serverOut
     */
    public void setServerOut(PrintWriter serverOut) {
        this.serverOut = serverOut;
    }

    /**
     * Get object to read message from server
     * @return BufferedReader object
     */
    public BufferedReader getServerIn() {
        return serverIn;
    }

    /**
     * Set object to read message from server
     * @param serverIn
     */
    public void setServerIn(BufferedReader serverIn) {
        this.serverIn = serverIn;
    }

    /**
     * Get object holding network's information
     * @return NetworkInfo object
     */
    public NetworkInfo getClientNetworkInfo() {
        return clientNetworkInfo;
    }

    /**
     * Set object containing network's info
     * @param clientNetworkInfo
     */
    public void setClientNetworkInfo(NetworkInfo clientNetworkInfo) {
        this.clientNetworkInfo = clientNetworkInfo;
    }

    /**
     * Connect to host and
     * Set input and output channels for server
     * @param args
     */
    public void setServerIO(String args[]) {
        String serverHostName = clientNetworkInfo.getIpAddr();
        if (args.length > 0) {
            serverHostName = args[0];
        }
        System.out.println("Attemping to connect to host " + serverHostName + " on port " + 12345 + ".");
        try {
            serverSocket = new Socket(serverHostName, clientNetworkInfo.getPortNum());
            serverOut = new PrintWriter(serverSocket.getOutputStream(), true);
            serverIn = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: " + serverHostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: " + serverHostName);
            System.exit(1);
        }
    }

    /**
     * Get input from keyboard and put in a thread
     * @return input from the keyboard
     * @throws InterruptedException
     */
    public String getKeyboardInput() throws InterruptedException {
        String keyboardInput;
        do {
            keyboardInput = queue.take();
            Thread.sleep(100);
        } while (keyboardInput == null);
        enableKeyboard = false;
        return keyboardInput;
    }

    /**
     * Printing out text on the screen with format
     * @param text: text to be output
     */
    public void println(String text) {
        String prompt = "\r> ";
        if(text.length() < prompt.length())
            text = String.format("%-5s", text);
        System.out.println("\r" + text);
        System.out.print(prompt);
    }

    /**
     * Method to print out the formatted list of all available clients
     * @param input String all current clients separated by ";"
     * @throws NumberFormatException
     * @throws UnknownHostException
     */
    public void printAllClients(String input) throws NumberFormatException, UnknownHostException {
        String clients[] = input.split(";");
        if (clients.length == 1) {
            println("There are currently no other available clients...");
            return;
        }
        println("|-----------------------------------------------------|");
        println(String.format("%-16s All Clients Available %16s", "|", "|"));
        println("|-----------------------------------------------------|");
        println(String.format("| %-16s| %-16s| %-16s|", "Name", "IP Address", "Port Number"));
        println("|-----------------------------------------------------|");
        for (String client : clients) {
            if(!clientNetworkInfo.getName().equals(NetworkInfo.parse(client).getName())){
                println(ServerNetworkInfo.parse(client).displayString());
                println("|-----------------------------------------------------|");
            }
        }
    }

    /**
     * Method to concatenate string tokens
     * @param tokens: String to be combined
     * @param split_char: desired splitting character
     * @return a newly built String
     */
    public String joinTokens(String[] tokens, char split_char) {
        StringBuilder out = new StringBuilder();
        for (int i = 1; i < tokens.length; i++) {
            out.append(tokens[i]);
            if (i != tokens.length - 1)
                out.append(split_char);
        }
        return out.toString();
    }

    /**
     * Handling client's commands
     * @param userInput
     * @return boolean value (true: exit, invalid), otherwise: false
     * @throws InterruptedException
     * @throws IOException
     */
    public boolean handleClientInput(String userInput) throws InterruptedException, IOException {
        if (clientNetworkInfo.getName().equals("")) {
            serverOut.println("client_info-" + getClientInfoStr(userInput));
            return true;
        }
        // System.out.println("ECHO(CLIENT): " + userInput);
        String[] tokens = userInput.split(" ");
        String inLabel = tokens[0].toLowerCase();
        String outLabel = inLabel, outContent = "";
        boolean exit = false;
        try {
            switch (inLabel) {
                case "help":
                    println(getCommands());
                    break;
                case "leave":
                    outContent = getMessage("");
                    clientRecipients = new ArrayList<>(); // clears client recipent list
                    // println("RECIPIENTS:"+clientRecipients);
                    break;
                case "leave_server":
                    println("Leaving server...");
                    exit = true;
                    break;
                case "print_clients":
                    break;
                case "request":
                    if (!clientRecipients.isEmpty()) {
                        println("Cannot request during conversation.");
                        return true;
                    } 
                    userInput = userInput.replace(" "+clientNetworkInfo.getName(), "");
                    if (userInput.replace(" ", "").equals(outLabel)) {
                        println("Cannot request to speak with yourself.");
                        return true;
                    }
                    outContent = joinTokens(tokens, ';');
                    break;
                case "broadcast":
                    outContent = joinTokens(tokens, ' ');
                    break;
                case "close":
                    sendOut(outLabel, outContent);
                    println("Socket will be closed!");
                    exit = true;
                    break;
                default:
                    outLabel = "msg";
                    outContent = getMessage(userInput);
                    break;
            }
        } catch (Exception e) {
            println(e.getMessage());
            sendOut("error","");
            return false;
        }
        if (outContent != null) sendOut(outLabel, outContent);
        else println("Invalid command...");
        return !exit;
    }

    /**
     * Get message from client
     * @param msg: client's message typed from keyboard
     * @return message, otherwise: null if no other client in the room
     */
    public String getMessage(String msg) {
        if (!clientRecipients.isEmpty()) return Message.build(clientNetworkInfo.getName(), clientRecipients, msg);
        return null;
    }

    /**
     * Server handling input from user
     * @param serverInput
     * @return boolean value (true: exit, invalid), otherwise: false
     * @throws IOException
     * @throws InterruptedException
     */
    public boolean handleServerInput(String serverInput) throws IOException, InterruptedException {
        if (file!=null) receivedIn(serverInput);
        Message msg = Message.parse(serverInput);
        boolean exit = false;
        String outLabel = msg.getLabel(), outContent = "";
        String[] tokens;
        switch (msg.getLabel()) {
            case "client_info":
                // allows client to reenter name
                println("Name is unavailable. Please enter a new name");
                outContent = getClientInfoStr(getKeyboardInput());
                serverOut.println(outLabel + "-" + outContent);
                break;
            case "success":
                clientNetworkInfo.setName(msg.getContent().split(";")[0]);
                int clientNum = Integer.parseInt(msg.getContent().split(";")[1]);
                file = new File(String.format("client_logs/client%d.log", clientNum));
                clearFile();
                sendOut("print_clients", "");
                println("type 'help' to see commands\n");
                break;
            case "join":
                println(msg.getContent() + " has entered the server.");
                break;
            case "print_clients":
                printAllClients(msg.getContent());
                break;
            case "leave":
                println(msg.getContent() + " has left the chat.");
                // println("RECIPIENT REMOVED:"+msg.getContent());
                clientRecipients.remove(msg.getContent());
                // println("RECIPIENTS:"+clientRecipients);
                break;
            case "leave_server":
                println(msg.getContent() + " has left the server.");
                if (!clientRecipients.isEmpty()) clientRecipients.remove(msg.getContent()); // ends chat if necessary
                break;
            case "request":
                enableKeyboard = true;
                println("Would you like to talk with " + msg.getContent() + "? (Y/N)");
                boolean accept = getKeyboardInput().equalsIgnoreCase("Y");
                outLabel = "request_resp";
                outContent = clientNetworkInfo.getName() + (accept ? " accept " : " reject " ) + msg.getContent();
                sendOut(outLabel, outContent);
                if(accept){
                    clientRecipients.add(msg.getContent()); // start communication
                }
                break;
            case "request_resp":
                String name = msg.getContent().split(" ")[0];
                String resp = msg.getContent().split(" ")[1];
                if (resp.equals("reject"))
                    println(name + " rejected your request.");
                else { // accept
                    println(name + " accepted your request.");
                    clientRecipients.add(name); // start communication
                }
                break;
            case "msg":
                tokens = msg.getContent().split("-", 3);
                println(tokens[0] + ": " + tokens[2]);
                break;
            case "broadcast":
                tokens = msg.getContent().split("-", 3);
                println(tokens[0] + ": " + tokens[2]);
                break;
            case "close":
                println("Sorry, socket will be closed!"); 
                exit = true;
                break;
            case "error":
                println("Error: " + msg.getContent());
                break;
            default:
                break;
        }
        return !exit;
    }

    /**
     * Get client info 
     * @param name: name of client
     * @return client's info
     */
    public String getClientInfoStr(String name) {
        return name  + ":" + clientNetworkInfo.getIpAddr() + ":" + clientNetworkInfo.getPortNum();
    }

    /**
     * Clear file
     * @throws FileNotFoundException
     */
    public void clearFile() throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(file.getPath());
        pw.print("");
        pw.close();
    }

    /**
     * Handling communication between client and server
     * @throws IOException
     * @throws InterruptedException
     */
    public void communicate() throws IOException, InterruptedException {
        System.out.print("name: "); // ***CHECK IF EMPTY & UNIQUE

        while (true) {
            if (queue.size() > 0 && !enableKeyboard) {
                String userInput = queue.take();
                if (userInput != null) {
                    if (!handleClientInput(userInput)) break;
                }
            }
            if (serverIn.ready()) {
                String serverInput = serverIn.readLine();
                if (serverInput != null) {
                    if (!handleServerInput(serverInput)) break;
                }
            }
        }
        System.out.println("GOT OUT!");
        close();
        System.exit(0); // Kills KeyboardThread
    }

    /**
     * Get command entered by user
     * @return client's command
     */
    public String getCommands() {
        StringBuilder out = new StringBuilder();
        out.append("---Commands---\n");
        String[] commands = {
            "help",
            "print_clients", 
            "request", 
            "broadcast",
            "leave",
            "leave_server",
            "close"
        };
        String[] descriptions = {
            "shows all commands available",
            "server prints all available clients",
            "request private chat with another client\n\t\t" + 
                "  syntax: 'request {client's name}'",
            "broadcasts a message to all clients\n\t\t" + 
                "  synatx: 'broadcast {message}'", 
            "closes private chat",
            "closes chat and disconnects from server",
            "terminates server and closes chat for all clients"
        };
        for (int i = 0; i < descriptions.length; i++) {
            out.append(String.format("%15s - %s\n", commands[i], descriptions[i]));
        }
        return out.toString();
    }

    /**
     * Writing input received to file
     * @param inputLine: input to be saved in file
     * @throws IOException
     */
    public void receivedIn(String inputLine) throws IOException {
        if (clientNetworkInfo != null) {
            writeToFile("Received from Server: " + inputLine + "\n");
        }
    }

    /**
     * Method to write text to a file
     * @param line
     * @throws IOException
     */
    public void writeToFile(String line) throws IOException {
        FileWriter fw = new FileWriter(new File(file.getPath()), true); //file.getAbsolutePath(), true);
        fw.write(new Timestamp(System.currentTimeMillis()) + "\n" + line);
        fw.close();
    }

    /**
     * Sending out message to server
     * @param label
     * @param content
     * @throws IOException
     */
    public void sendOut(String label, String content) throws IOException {
        String line = label + "-" + content;
        serverOut.println(line);
        writeToFile("\tClient Sent: " + line + "\n");
    }
    
    /**
     * Closing server IO chanels and socket
     * @throws IOException
     */
    public void close() throws IOException {
        serverIn.close();
        serverOut.close();
        serverSocket.close();
    }
}

/**
 * KeyboardThread
 */
class KeyboardThread extends Thread {
    private BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));; // new BufferedReader(new InputStreamReader(System.in));
    private LinkedBlockingQueue<String> queue;

    /**
     * Constructor
     * @param queue
     */
    public KeyboardThread(LinkedBlockingQueue<String> queue) {
        this.queue = queue;
        start();
    }

    /**
     * Reading input from keyboard until empty
     */
    public void run() {
        try {
            String userInput;
            
            do {
                System.out.print("\r> ");

                userInput = stdIn.readLine();
                if (userInput != null) {
                    queue.put(userInput);
                }
            } while (userInput != null);
            stdIn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}