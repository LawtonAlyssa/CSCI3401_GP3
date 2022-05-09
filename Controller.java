/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.net.*;
import java.io.*;

/**
 *
 * @author Alyssa 
 * @author Truong
 */
public class Controller {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        String ipAddr = InetAddress.getLocalHost().toString().split("/")[1];
        System.out.println("IP Address:" + ipAddr);
        
        int portNum = 12345;
        Server controller = new Server(portNum);

        new Connection(controller);

        controller.communicateAllClients();

        controller.close();
    }
}