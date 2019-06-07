/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package music.manager.create;

import controllers.AlbumJpaController;
import controllers.ArtistJpaController;
import controllers.SongJpaController;
import java.util.List;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import music.manager.entities.Album;
import music.manager.entities.Artist;
import music.manager.entities.Song;

/**
 *
 * @author Alex Stefan
 */
public class Create extends Thread{
    static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("Music_ManagerPU");

    public void createArtist(String name) throws Exception {
        ArtistJpaController pc = new ArtistJpaController(emf);
        Artist artist = new Artist();
        artist.setName(name);
        pc.create(artist);    
    }
    public Artist existsArtist(String nameArtist){
        ArtistJpaController artistController = new ArtistJpaController(emf);
        List<Artist> artists = artistController.findArtistEntities();
        Artist artistSong = null;
        for(Artist artist : artists){
            if(artist.getName().equals(nameArtist)){
                artistSong = artist;
            }
        }
        if(artistSong != null){
            return artistSong;
        }
        else{
            System.out.println("Cred ca aici este problema la artist");
            return null;
        }
    }
    public Album existsAlbum(String nameAlbum){
        AlbumJpaController albumController = new AlbumJpaController(emf);
        List<Album> albums = albumController.findAlbumEntities();
        Album albumSong = null;
        for(Album album : albums){
            if(album.getName().equals(nameAlbum)){
                albumSong = album;
            }
        }
        if(albumSong != null){
            return albumSong;
        }
        else{
            return null;
        }
    }
    public void createAlbum(String nameAlbum, String nameArtist) throws Exception {
        AlbumJpaController albumController = new AlbumJpaController(emf);
        ArtistJpaController artistController = new ArtistJpaController(emf);
        List<Artist> artists = artistController.findArtistEntities();
        Album album = new Album();
        album.setName(nameAlbum);
        Artist artistSong = null;
        for(Artist artist : artists){
            if(artist.getName().equals(nameArtist)){
                artistSong = artist;
            }
        }
        if(artistSong != null){
            album.setIdArtist(artistSong);
        }
        else{
            System.out.println("Cred ca aici este problema la album ");
            //throw custom exception NoArtistFoundForAlbum OR CREATE NEW ARTIST
        }
        albumController.create(album);    
    }
    public void createSong(String nameSong, String nameAlbum) throws Exception {
        SongJpaController pc = new SongJpaController(emf);
        AlbumJpaController albumController = new AlbumJpaController(emf);
        List<Album> albums = albumController.findAlbumEntities();
        Song song = new Song();
        song.setName(nameSong);
        Album albumSong = null;
        for(Album album : albums){
            if(album.getName().equals(nameAlbum)){
                albumSong = album;
            }
        }
        if(albumSong != null){
            System.out.println("Am setat numele albumului: " + albumSong.getName());
            song.setIdAlbum(albumSong);
        }
        else{
            System.out.println("Cred ca aici este problema la song");
            //throw custom exception NoArtistFoundForAlbum OR CREATE NEW ARTIST
        }
        pc.create(song);    
    }
}
