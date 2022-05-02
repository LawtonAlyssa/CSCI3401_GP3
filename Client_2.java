/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.*;
import java.net.*;

/**
 *
 *
 */
public class Client_2 {

    final static int port_num = 11111;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        
        String ipAddr = InetAddress.getLocalHost().toString().split("/")[1];

        String serverHostname = connectToServer(args, ipAddr);

        Socket controllerSocket = null;
        PrintWriter out = null;
        BufferedReader in = null;

        try {
            controllerSocket = new Socket(serverHostname, 12345);
            out = new PrintWriter(controllerSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(controllerSocket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: " + serverHostname);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for "
                    + "the connection to: " + serverHostname);
            System.exit(1);
        }
        
        System.out.println("Successfully connected to Controller. ");
        
        ServerSocket serverSocket = createServerSocket();
        
        Socket clientSocket = createClientSocket(serverSocket);
        
        out.println("IP Address: " + ipAddr);
        out.println("Port Number: " + port_num);
        System.out.println(clientSocket);
        //        out.println()
        
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

        communicate(out, in, stdIn);

        out.close();
        in.close();
        stdIn.close();
        controllerSocket.close();
        clientSocket.close();
        serverSocket.close();
        
    }

    public static String connectToServer(String[] args, String ipAddr) throws UnknownHostException {
        String serverHostname = new String(ipAddr);

        if (args.length > 0) {
            serverHostname = args[0];
        }
        System.out.println("Attemping to connect to host "
                + serverHostname + " on port 12345.");
        return serverHostname;
    }
    
    public static ServerSocket createServerSocket(){
        ServerSocket serverSocket = null;
        
        try {
            serverSocket = new ServerSocket(port_num);
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + port_num + ".");
            System.exit(1);
        }
        return serverSocket;
    }
    
    public static Socket createClientSocket(ServerSocket serverSocket){
        Socket clientSocket = null;
        System.out.println("Waiting for connection.....");

        try {
            clientSocket = serverSocket.accept();
        } catch (IOException e) {
            System.err.println("Accept failed.");
            System.exit(1);
        }

        System.out.println("Connection successful");        
        System.out.println("Waiting for input.....");
        
        return clientSocket;
    }

    public static void communicate(PrintWriter out, BufferedReader in, BufferedReader stdIn) throws IOException{
        String userInput = null;

        System.out.print("input: ");

        while ((userInput = stdIn.readLine()) != null) {
            out.println(userInput);
            try {
                System.out.println("echo: " + in.readLine());
                if (userInput.equalsIgnoreCase("Bye")) {
                    System.out.println("Socket will be closed!");
                    break;
                }
            } catch (Exception e) {
                System.out.println("Socket Closed!");
                break;
            }
            System.out.print("input: ");
        }
    }
}
