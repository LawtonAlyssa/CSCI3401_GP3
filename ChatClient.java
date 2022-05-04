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
public class ChatClient {

    final static int portNum = 22222;

    /**
     * @param args the command line arguments
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        
        Client client = new Client("192.168.200.120", 0);
        int portNum = 12345;
        Connection serverCxn = new Connection(client, args, portNum);

        /*
         * Client otherClient = new Client(serverConnection.getClientInfo()[0],
         * Integer.parseInt(serverConnection.getClientInfo()[1]));
         * Server clientServer = new Server(client.getPortNum());
         * Connection clientServerConn = new Connection(clientServer);
         */

        serverCxn.close();
        client.close();
    }

}
