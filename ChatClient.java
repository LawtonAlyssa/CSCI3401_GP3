/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.*;
import java.net.*;

/**
 *
 * @author Alyssa
 * @author Truong
 */
public class ChatClient {

    // final static int portNum = 22222;

    /**
     * @param args the command line arguments
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        
        int portNum = 12345;
        Client client = new Client("172.20.10.3", portNum); //192.168.200.120

        client.setServerIO(args);
        client.communicate();
        
        // Connection serverCxn = new Connection(client, args, portNum);

        // serverCxn.close();
        client.close();
    }

}
