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

    public Client(String ipAddr, int portNum) throws UnknownHostException {
        clientNetworkInfo = new NetworkInfo("", ipAddr, portNum);
        new KeyboardThread(queue);
        
    }

    public Client(String ipAddr) throws UnknownHostException {
        clientNetworkInfo = new NetworkInfo(11111);
        new KeyboardThread(queue);
    }

    public Socket getServerSocket() {
        return serverSocket;
    }

    public void setServerSocket(Socket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public PrintWriter getOut() {
        return serverOut;
    }

    public void setOut(PrintWriter serverOut) {
        this.serverOut = serverOut;
    }

    public BufferedReader getIn() {
        return serverIn;
    }

    public void setIn(BufferedReader in) {
        this.serverIn = in;
    }

    public PrintWriter getServerOut() {
        return serverOut;
    }

    public void setServerOut(PrintWriter serverOut) {
        this.serverOut = serverOut;
    }

    public BufferedReader getServerIn() {
        return serverIn;
    }

    public void setServerIn(BufferedReader serverIn) {
        this.serverIn = serverIn;
    }

    public NetworkInfo getClientNetworkInfo() {
        return clientNetworkInfo;
    }

    public void setClientNetworkInfo(NetworkInfo clientNetworkInfo) {
        this.clientNetworkInfo = clientNetworkInfo;
    }

    // public void sendClientInfo() throws Exception {
    // System.out.print("Enter name: ");
    // String name = getKeyboardInput();

    // }

    public void setServerIO(String args[]) {
        String serverHostName = clientNetworkInfo.getIpAddr();
        if (args.length > 0) {
            serverHostName = args[0];
        }
        System.out.println("Attemping to connect to host " + serverHostName + " on port " + 12345 + ".");
        try {
            // client.setServerSocket(new Socket("141.239.208.113", 3125)); 
            serverSocket = new Socket(serverHostName, clientNetworkInfo.getPortNum());
            // client.setOut(new PrintWriter(client.getServerSocket().getOutputStream(), true));
            serverOut = new PrintWriter(serverSocket.getOutputStream(), true);
            // client.setIn(new BufferedReader(new InputStreamReader(client.getServerSocket().getInputStream())));
            serverIn = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: " + serverHostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: " + serverHostName);
            System.exit(1);
        }
    }

    public String getKeyboardInput() throws InterruptedException {
        String keyboardInput;
        do {
            keyboardInput = queue.take();
            Thread.sleep(100);
        } while (keyboardInput == null);
        enableKeyboard = false;
        return keyboardInput;
    }

    public void println(String text) {
        String prompt = "\r> ";
        if(text.length() < prompt.length())
            text = String.format("%-5s", text);
        System.out.println("\r" + text);
        System.out.print(prompt);
    }

    // public void print(String text){
    // System.out.print("\r" + text);
    // }

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



    public String joinTokens(String[] tokens, char split_char) {
        StringBuilder out = new StringBuilder();
        for (int i = 1; i < tokens.length; i++) {
            out.append(tokens[i]);
            if (i != tokens.length - 1)
                out.append(split_char);
        }
        return out.toString();
    }

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
                    System.out.println("USER INPUT" + userInput);
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
            println("ERROR");
            // println("Socket Closed!");
            return false;
        }
        if (outContent != null) sendOut(outLabel, outContent);
        else println("Invalid command...");
        return !exit;
    }

    public String getMessage(String msg) {
        // println("RECIPIENTS:" + clientRecipients);
        if (!clientRecipients.isEmpty()) return Message.build(clientNetworkInfo.getName(), clientRecipients, msg);
        return null;
    }

    public boolean handleServerInput(String serverInput) throws IOException, InterruptedException {
        if (file!=null) receivedIn(serverInput);
        Message msg = Message.parse(serverInput);
        println("ECHO LABEL:" + msg.getLabel());
        println("ECHO MESSAGE:" + msg.getContent());
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
                // println("NUM:" + clientNum);
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
                println("RECEIVED");
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

    public String getClientInfoStr(String name) {
        return name  + ":" + clientNetworkInfo.getIpAddr() + ":" + clientNetworkInfo.getPortNum();
    }

    public void clearFile() throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(file.getPath());
        pw.print("");
        pw.close();
    }

    public void communicate() throws IOException, InterruptedException {
        System.out.print("name: "); // ***CHECK IF EMPTY & UNIQUE

        while (true) {
            if (queue.size() > 0 && !enableKeyboard) {
                String userInput = queue.take();
                if (userInput != null) {
                    // println("Getting input: " + userInput);
                    if (!handleClientInput(userInput)) break;
                }
            }
            if (serverIn.ready()) {
                String serverInput = serverIn.readLine();
                // println("SERVER INPUT:" + serverInput);
                if (serverInput != null) {
                    if (!handleServerInput(serverInput)) break;
                }
            }
            // Thread.sleep(100);
        }
        System.out.println("GOT OUT!");
        close();
        System.exit(0); // Kills KeyboardThread
    }

    public String getCommands() {
        StringBuilder out = new StringBuilder();
        out.append("---Commands---\n");
        String[] commands = {"print_clients", 
                                "request"};
        String[] descriptions = {"server prints all available clients",
                                    "request to speak to client(s)\n\t\t"+
                                    "  type 'request name0 name1 ...'"};
        for (int i = 0; i < descriptions.length; i++) {
            out.append(String.format("%15s - %s\n", commands[i], descriptions[i]));
        }
        return out.toString();
    }

    public void receivedIn(String inputLine) throws IOException {
        if (clientNetworkInfo != null) {
            writeToFile("Received from Server: " + inputLine + "\n");
        }
    }

    public void writeToFile(String line) throws IOException {
        FileWriter fw = new FileWriter(new File(file.getPath()), true); //file.getAbsolutePath(), true);
        fw.write(new Timestamp(System.currentTimeMillis()) + "\n" + line);
        fw.close();
        // System.out.println("Wrote to file.");
    }

    public void sendOut(String label, String content) throws IOException {
        String line = label + "-" + content;
        serverOut.println(line);
        writeToFile("\tClient Sent: " + line + "\n");
    }
    
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

    public KeyboardThread(LinkedBlockingQueue<String> queue) {
        this.queue = queue;
        start();
    }

    public void run() {
        try {
            String userInput;
            
            do {
                // System.out.println("KEYBOARD");
                System.out.print("\r> ");

                userInput = stdIn.readLine();
                if (userInput != null) {
                    queue.put(userInput);
                    // System.out.println("KEYBOARD THREAD: " + userInput);
                }
            } while (userInput != null);
            stdIn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // System.out.println("KEYBOARD CLOSE");
    }

}