package example_code;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.net.*; 
import java.io.*; 
/**
 *
 * @author yizhu
 */
public class EchoServer {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException{
    ServerSocket serverSocket = null; 

    try { 
         serverSocket = new ServerSocket(12345); 
        } 
    catch (IOException e) 
        { 
         System.err.println("Could not listen on port: 12345."); 
         System.exit(1); 
        } 

    Socket clientSocket = null; 
    System.out.println ("Waiting for connection.....");

    try { 
         clientSocket = serverSocket.accept(); 
        } 
    catch (IOException e) 
        { 
         System.err.println("Accept failed."); 
         System.exit(1); 
        } 

    System.out.println ("Connection successful");
    System.out.println ("Waiting for input.....");

    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), 
                                      true); 
    BufferedReader in = new BufferedReader( 
            new InputStreamReader( clientSocket.getInputStream())); 

    String inputLine; 
    // boolean done = false;
    while ((inputLine = in.readLine()) != null) 
        { 
         System.out.println ("Server: " + inputLine); 
         out.println(inputLine); 

         if (inputLine.equalsIgnoreCase("Bye")) 
             break; 
        } 

    out.close(); 
    in.close(); 
    clientSocket.close(); 
    serverSocket.close(); 
   } 
}

