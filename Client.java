/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.*;
import java.net.*;
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
    private int[] ports = { 11111, 22222, 33333, 44444, 55555 };

    public Client(String ipAddr, int portNum) throws UnknownHostException {
        clientNetworkInfo = new NetworkInfo("", ipAddr, portNum);
        new KeyboardThread(queue);
    }

    public Client(String ipAddr) throws UnknownHostException {
        clientNetworkInfo = new NetworkInfo("", ipAddr, 11111);
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
        System.out.println("\r" + text);
        System.out.print("input: ");
    }

    // public void print(String text){
    // System.out.print("\r" + text);
    // }

    public void printAllClients(String input) {
        println("---All Clients Available---");
        String clients[] = input.split(";");
        for (String client : clients) {
            println(NetworkInfo.parse(client).displayString());
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

    public boolean handleUserInput(String userInput) {
        if (clientNetworkInfo.getName().equals("")) {

            serverOut.println("client_info-" + userInput + ":" + clientNetworkInfo.getIpAddr() + ":"
                    + clientNetworkInfo.getPortNum());
            clientNetworkInfo.setName(userInput);
            return true;
        }
        // System.out.println("echo(client): " + userInput);
        String[] tokens = userInput.split(" ");
        String inLabel = tokens[0].toLowerCase();
        String outLabel = inLabel, outContent = "";
        boolean exit = false;
        try {
            switch (inLabel) {
                case "leave":
                    serverOut.println(outLabel + "-" + outContent);
                    println("Socket will be closed!");
                    exit = true;
                    break;
                case "print_clients":
                    break;
                case "request":
                    outContent = joinTokens(tokens);
                    break;
                default:
                    outLabel = "msg";
                    outContent = userInput;
                    break;
            }
        } catch (Exception e) {
            serverOut.println("error-");
            println("Socket Closed!");
            return false;
        }
        serverOut.println(outLabel + "-" + outContent);
        return !exit;
    }

    public boolean handleServerInput(String serverInput) throws IOException, InterruptedException {
        Message msg = Message.parse(serverInput);
        println("echo: " + msg.getContent());
        boolean exit = false;
        // String outLabel = "", outContent = "";
        switch (msg.getLabel()) {
            case "print_clients":
                printAllClients(msg.getContent());
                break;
            case "leave":

                break;
            case "request":
                enableKeyboard = true;
                println("Would you like to talk with " + msg.getContent() + "? (Y/N): ");
                boolean accept = getKeyboardInput().equalsIgnoreCase("Y");
                String outLabel = "request_resp";
                String outContent = clientNetworkInfo.getName() + (accept ? " accept " : " reject " ) + msg.getContent();
                serverOut.println(outLabel + "-" + outContent);
                if(accept){
                    // start communication
                }
                break;
            case "request_resp":
                String name = msg.getContent().split(" ")[0];
                String resp = msg.getContent().split(" ")[1];
                if (resp.equals("reject"))
                    println(name + " rejected your request.");
                else { // accept
                    println(name + " accepted your request.");
                    // start communication
                    
                }
                break;
            case "close":

                exit = true;
                break;
            default:
                break;
        }
        return !exit;
    }

    public void communicate() throws IOException, InterruptedException {
        System.out.print("name: ");

        while (true) {
            if (queue.size() > 0 && !enableKeyboard) {
                String userInput = queue.take();
                if (userInput != null) {
                    // println("Getting input: " + userInput);
                    if (!handleUserInput(userInput))
                        break;
                }
            }
            if (serverIn.ready()) {
                String serverInput = serverIn.readLine();
                if (serverInput != null) {
                    if (!handleServerInput(serverInput))
                        break;
                }
            }
            // Thread.sleep(100);
        }

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
    private BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
    private LinkedBlockingQueue<String> queue;

    public KeyboardThread(LinkedBlockingQueue<String> queue) {
        this.queue = queue;
        start();
    }

    public void run() {
        try {
            String userInput;
            do {
                System.out.print("input: ");

                userInput = stdIn.readLine();
                if (userInput != null) {
                    queue.put(userInput);
                    // System.out.println("Keyboard thread: " + userInput);
                }
            } while (userInput != null);
            stdIn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}