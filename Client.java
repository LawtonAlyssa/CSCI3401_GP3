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
public class Client {
    private Socket serverSocket = null;
    private PrintWriter serverOut = null;
    private BufferedReader serverIn = null;
    private NetworkInfo clientNetworkInfo;
    private LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private boolean enableKeyboard = false;
    private ArrayList<String> clientRecipients = new ArrayList<>();
    // private int[] ports = { 11111, 22222, 33333, 44444, 55555 };

    public Client(String ipAddr, int portNum) throws UnknownHostException {
        clientNetworkInfo = new NetworkInfo(portNum);
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
        String prompt = "> ";
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
                println(NetworkInfo.parse(client).displayString());
                println("|-----------------------------------------------------|");
            }
            //set checkbox text value
        }
    }

    public String joinTokens(String[] tokens) {
        StringBuilder out = new StringBuilder();
        for (int i = 1; i < tokens.length; i++) {
            out.append(tokens[i]);
            if (i != tokens.length - 1)
                out.append(";");
        }
        return out.toString();
    }

    public boolean handleClientInput(String userInput) throws InterruptedException {
        if (clientNetworkInfo.getName().equals("")) {
            serverOut.println("client_info-" + userInput + ":" + clientNetworkInfo.getIpAddr() + ":"
                    + clientNetworkInfo.getPortNum());
            clientNetworkInfo.setName(userInput);
            serverOut.println("print_clients" + "-");
            println("type 'help' to see commands\n");
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
                    outContent = joinTokens(tokens);
                    break;
                case "close":
                    serverOut.println(outLabel + "-" + outContent);
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
            serverOut.println("error-");
            println("Socket Closed!");
            println("ERROR");
            return false;
        }
        if (outContent != null) serverOut.println(outLabel + "-" + outContent);
        else println("Invalid command...");
        return !exit;
    }

    public String getMessage(String msg) {
        // println("RECIPIENTS:" + clientRecipients);
        if (!clientRecipients.isEmpty()) return Message.build(clientNetworkInfo.getName(), clientRecipients, msg);
        return null;
    }

    public boolean handleServerInput(String serverInput) throws IOException, InterruptedException {
        Message msg = Message.parse(serverInput);
        println("ECHO: " + msg.getContent());
        boolean exit = false;
        println("LABEL:" + msg.getLabel());
        // String outLabel = "", outContent = "";
        switch (msg.getLabel()) {
            case "client_info":
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
            case "request":
                enableKeyboard = true;
                println("Would you like to talk with " + msg.getContent() + "? (Y/N)");
                boolean accept = getKeyboardInput().equalsIgnoreCase("Y");
                String outLabel = "request_resp";
                String outContent = clientNetworkInfo.getName() + (accept ? " accept " : " reject " ) + msg.getContent();
                serverOut.println(outLabel + "-" + outContent);
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
                String[] tokens = msg.getContent().split("-", 3);
                println(tokens[0] + ": " + tokens[2]);
                break;
            case "close":
                println("Sorry, socket will be closed!"); 
                exit = true;
                break;
            case "error":
                println(msg.getContent());
                break;
            default:
                break;
        }
        return !exit;
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
                System.out.println(serverInput);
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
                System.out.print("> ");

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