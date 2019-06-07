/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import controllers.exceptions.NonexistentEntityException;
import controllers.exceptions.PreexistingEntityException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import music.manager.entities.Album;
import music.manager.entities.Song;

/**
 *
 * @author Alex Stefan
 */
public class SongJpaController implements Serializable {

    public SongJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Song song) throws PreexistingEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Album idAlbum = song.getIdAlbum();
            if (idAlbum != null) {
                idAlbum = em.getReference(idAlbum.getClass(), idAlbum.getId());
                song.setIdAlbum(idAlbum);
            }
            em.persist(song);
            if (idAlbum != null) {
                idAlbum.getSongCollection().add(song);
                idAlbum = em.merge(idAlbum);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findSong(song.getId()) != null) {
                throw new PreexistingEntityException("Song " + song + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Song song) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Song persistentSong = em.find(Song.class, song.getId());
            Album idAlbumOld = persistentSong.getIdAlbum();
            Album idAlbumNew = song.getIdAlbum();
            if (idAlbumNew != null) {
                idAlbumNew = em.getReference(idAlbumNew.getClass(), idAlbumNew.getId());
                song.setIdAlbum(idAlbumNew);
            }
            song = em.merge(song);
            if (idAlbumOld != null && !idAlbumOld.equals(idAlbumNew)) {
                idAlbumOld.getSongCollection().remove(song);
                idAlbumOld = em.merge(idAlbumOld);
            }
            if (idAlbumNew != null && !idAlbumNew.equals(idAlbumOld)) {
                idAlbumNew.getSongCollection().add(song);
                idAlbumNew = em.merge(idAlbumNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                BigDecimal id = song.getId();
                if (findSong(id) == null) {
                    throw new NonexistentEntityException("The song with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(BigDecimal id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Song song;
            try {
                song = em.getReference(Song.class, id);
                song.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The song with id " + id + " no longer exists.", enfe);
            }
            Album idAlbum = song.getIdAlbum();
            if (idAlbum != null) {
                idAlbum.getSongCollection().remove(song);
                idAlbum = em.merge(idAlbum);
            }
            em.remove(song);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Song> findSongEntities() {
        return findSongEntities(true, -1, -1);
    }

    public List<Song> findSongEntities(int maxResults, int firstResult) {
        return findSongEntities(false, maxResults, firstResult);
    }

    private List<Song> findSongEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Song.class));
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

    public Song findSong(BigDecimal id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Song.class, id);
        } finally {
            em.close();
        }
    }

    public int getSongCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Song> rt = cq.from(Song.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
