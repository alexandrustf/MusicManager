/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import controllers.exceptions.IllegalOrphanException;
import controllers.exceptions.NonexistentEntityException;
import controllers.exceptions.PreexistingEntityException;
import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import music.manager.entities.Artist;
import music.manager.entities.Song;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import music.manager.entities.Album;

/**
 *
 * @author Alex Stefan
 */
public class AlbumJpaController implements Serializable {

    public AlbumJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Album album) throws PreexistingEntityException, Exception {
        if (album.getSongCollection() == null) {
            album.setSongCollection(new ArrayList<Song>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Artist idArtist = album.getIdArtist();
            if (idArtist != null) {
                idArtist = em.getReference(idArtist.getClass(), idArtist.getId());
                album.setIdArtist(idArtist);
            }
            Collection<Song> attachedSongCollection = new ArrayList<Song>();
            for (Song songCollectionSongToAttach : album.getSongCollection()) {
                songCollectionSongToAttach = em.getReference(songCollectionSongToAttach.getClass(), songCollectionSongToAttach.getId());
                attachedSongCollection.add(songCollectionSongToAttach);
            }
            album.setSongCollection(attachedSongCollection);
            em.persist(album);
            if (idArtist != null) {
                idArtist.getAlbumCollection().add(album);
                idArtist = em.merge(idArtist);
            }
            for (Song songCollectionSong : album.getSongCollection()) {
                Album oldIdAlbumOfSongCollectionSong = songCollectionSong.getIdAlbum();
                songCollectionSong.setIdAlbum(album);
                songCollectionSong = em.merge(songCollectionSong);
                if (oldIdAlbumOfSongCollectionSong != null) {
                    oldIdAlbumOfSongCollectionSong.getSongCollection().remove(songCollectionSong);
                    oldIdAlbumOfSongCollectionSong = em.merge(oldIdAlbumOfSongCollectionSong);
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findAlbum(album.getId()) != null) {
                throw new PreexistingEntityException("Album " + album + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Album album) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Album persistentAlbum = em.find(Album.class, album.getId());
            Artist idArtistOld = persistentAlbum.getIdArtist();
            Artist idArtistNew = album.getIdArtist();
            Collection<Song> songCollectionOld = persistentAlbum.getSongCollection();
            Collection<Song> songCollectionNew = album.getSongCollection();
            List<String> illegalOrphanMessages = null;
            for (Song songCollectionOldSong : songCollectionOld) {
                if (!songCollectionNew.contains(songCollectionOldSong)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Song " + songCollectionOldSong + " since its idAlbum field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (idArtistNew != null) {
                idArtistNew = em.getReference(idArtistNew.getClass(), idArtistNew.getId());
                album.setIdArtist(idArtistNew);
            }
            Collection<Song> attachedSongCollectionNew = new ArrayList<Song>();
            for (Song songCollectionNewSongToAttach : songCollectionNew) {
                songCollectionNewSongToAttach = em.getReference(songCollectionNewSongToAttach.getClass(), songCollectionNewSongToAttach.getId());
                attachedSongCollectionNew.add(songCollectionNewSongToAttach);
            }
            songCollectionNew = attachedSongCollectionNew;
            album.setSongCollection(songCollectionNew);
            album = em.merge(album);
            if (idArtistOld != null && !idArtistOld.equals(idArtistNew)) {
                idArtistOld.getAlbumCollection().remove(album);
                idArtistOld = em.merge(idArtistOld);
            }
            if (idArtistNew != null && !idArtistNew.equals(idArtistOld)) {
                idArtistNew.getAlbumCollection().add(album);
                idArtistNew = em.merge(idArtistNew);
            }
            for (Song songCollectionNewSong : songCollectionNew) {
                if (!songCollectionOld.contains(songCollectionNewSong)) {
                    Album oldIdAlbumOfSongCollectionNewSong = songCollectionNewSong.getIdAlbum();
                    songCollectionNewSong.setIdAlbum(album);
                    songCollectionNewSong = em.merge(songCollectionNewSong);
                    if (oldIdAlbumOfSongCollectionNewSong != null && !oldIdAlbumOfSongCollectionNewSong.equals(album)) {
                        oldIdAlbumOfSongCollectionNewSong.getSongCollection().remove(songCollectionNewSong);
                        oldIdAlbumOfSongCollectionNewSong = em.merge(oldIdAlbumOfSongCollectionNewSong);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                BigDecimal id = album.getId();
                if (findAlbum(id) == null) {
                    throw new NonexistentEntityException("The album with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(BigDecimal id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Album album;
            try {
                album = em.getReference(Album.class, id);
                album.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The album with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<Song> songCollectionOrphanCheck = album.getSongCollection();
            for (Song songCollectionOrphanCheckSong : songCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Album (" + album + ") cannot be destroyed since the Song " + songCollectionOrphanCheckSong + " in its songCollection field has a non-nullable idAlbum field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Artist idArtist = album.getIdArtist();
            if (idArtist != null) {
                idArtist.getAlbumCollection().remove(album);
                idArtist = em.merge(idArtist);
            }
            em.remove(album);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Album> findAlbumEntities() {
        return findAlbumEntities(true, -1, -1);
    }

    public List<Album> findAlbumEntities(int maxResults, int firstResult) {
        return findAlbumEntities(false, maxResults, firstResult);
    }

    private List<Album> findAlbumEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Album.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Album findAlbum(BigDecimal id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Album.class, id);
        } finally {
            em.close();
        }
    }

    public int getAlbumCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Album> rt = cq.from(Album.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
