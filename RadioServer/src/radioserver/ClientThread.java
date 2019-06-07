/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package radioserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import playlist.Playlist;
import playlist.PlaylistJpaController;

/**
 *
 * @author Alex Stefan
 */
class ClientThread extends Thread {
    private Socket socket = null ;
    static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("RadioServerPU");
    public ClientThread (Socket socket) { this.socket = socket ; }
    public void run () {
       try {
           // Get the request from the input stream: client → server
           BufferedReader in = new BufferedReader(
           new InputStreamReader(socket.getInputStream()));
           String request = in.readLine();

           String raspuns = "The song chosen by a client is " + request + "!";
           System.out.println(raspuns);
           addSongInPlaylist(request);
// Send the response to the oputput stream: server → client
           PrintWriter out = new PrintWriter(socket.getOutputStream());
           
           out.println(getAndSetSongPlayingNow()); // send the song Name playing now the client
           out.flush();
       } catch (IOException e) {
           System.err.println("Communication error... " + e);
       } finally {
       try {
           socket.close(); 
       }   catch (IOException e) { 
               System.err.println (e); 
           }
       }
    }
    private void addSongInPlaylist(String songName){
        PlaylistJpaController playlistController = new PlaylistJpaController(emf);
        Playlist playlist = new Playlist(songName, BigInteger.ZERO);
        try {  
            playlistController.create(playlist);
        } catch (Exception ex) {
            System.out.println("Nu s-a creeat playlist-ul..");
        }
    }
    private String getAndSetSongPlayingNow(){ // it sets the playing song and returns the name of it
        PlaylistJpaController playlistController = new PlaylistJpaController(emf);
        List<Playlist> playlist = playlistController.findPlaylistEntities();
        System.out.println(playlist);
        for(Playlist song : playlist){
            if(song.getPlayed() == BigInteger.ZERO){
                try {
                    song.setPlayed(BigInteger.ONE);
                    playlistController.edit(song);
                    return song.getNume();
                } catch (Exception ex) {
                    System.out.println("The song edit didn't work..");
                }
            }
        }
        return null;
    }
}
