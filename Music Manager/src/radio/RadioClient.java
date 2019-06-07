/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package radio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import music.manager.entities.Song;

/**
 *
 * @author Alex Stefan
 */
public class RadioClient extends Thread{
    private String serverAddress = "127.0.0.1"; // The server's IP address
    private int PORT = 8100; // The server's port
    private Socket socket;   
    private PrintWriter out;
    private BufferedReader in;
    private String songName;
    public String playingSong;
    public RadioClient(String song){
        try {
            socket = new Socket(serverAddress, PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader (new InputStreamReader(socket.getInputStream())); 
        } catch (IOException ex) {
            System.err.println("No server listening... " + ex);
        }
        songName = song;
    }
    public void run() {
        try {
            out.println(songName);
            out.flush();
            // Wait the response from the server ("Hello World!")
            String response = in.readLine ();
            System.out.println(response);
            playingSong = response;
            try {
                socket.close(); //... usse try-catch-finally to handle the exceptions!
            } catch (IOException ex) {
                System.out.println("Watch out at closing for sockets!");
            }
        } catch (IOException ex) {
            System.out.println("Exception at IO to/from sockets..");
        }
    }  
//     public static void main (String[] args) throws IOException {
//        RadioClient radioClient = new RadioClient();
//        radioClient.sendSong();
//     }
}