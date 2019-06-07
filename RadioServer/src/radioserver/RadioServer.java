/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package radioserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 *
 * @author Alex Stefan
 */
public class RadioServer {
    // Define the port on which the server is listening
    public static final int PORT = 8100;
    
    public RadioServer() throws IOException {
        
        ServerSocket serverSocket = null ;
        try {
            serverSocket = new ServerSocket(PORT);
        while (true) {
            System.out.println ("Waiting for a client ...");
            Socket socket = serverSocket.accept();
            // Execute the client's request in a new thread
            new ClientThread(socket).start();
        }
        } catch (IOException e) {
            System.err. println ("Ooops... " + e);
        } finally {
            serverSocket.close();
        }
    }
    public static void main ( String [] args ) throws IOException {
        RadioServer server = new RadioServer ();
    }
}