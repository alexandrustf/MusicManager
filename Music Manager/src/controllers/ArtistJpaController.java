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
import music.manager.entities.Album;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import music.manager.entities.Artist;

/**
 *
 * @author Alex Stefan
 */
public class ArtistJpaController implements Serializable {

    public ArtistJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Artist artist) throws PreexistingEntityException, Exception {
//        System.out.println(artist.getId());
        if (artist.getAlbumCollection() == null) {
            artist.setAlbumCollection(new ArrayList<Album>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Collection<Album> attachedAlbumCollection = new ArrayList<Album>();
            for (Album albumCollectionAlbumToAttach : artist.getAlbumCollection()) {
                albumCollectionAlbumToAttach = em.getReference(albumCollectionAlbumToAttach.getClass(), albumCollectionAlbumToAttach.getId());
                attachedAlbumCollection.add(albumCollectionAlbumToAttach);
            }
            artist.setAlbumCollection(attachedAlbumCollection);
            em.persist(artist);
            for (Album albumCollectionAlbum : artist.getAlbumCollection()) {
                Artist oldIdArtistOfAlbumCollectionAlbum = albumCollectionAlbum.getIdArtist();
                albumCollectionAlbum.setIdArtist(artist);
                albumCollectionAlbum = em.merge(albumCollectionAlbum);
                if (oldIdArtistOfAlbumCollectionAlbum != null) {
                    oldIdArtistOfAlbumCollectionAlbum.getAlbumCollection().remove(albumCollectionAlbum);
                    oldIdArtistOfAlbumCollectionAlbum = em.merge(oldIdArtistOfAlbumCollectionAlbum);
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findArtist(artist.getId()) != null) {
                throw new PreexistingEntityException("Artist " + artist + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Artist artist) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Artist persistentArtist = em.find(Artist.class, artist.getId());
            Collection<Album> albumCollectionOld = persistentArtist.getAlbumCollection();
            Collection<Album> albumCollectionNew = artist.getAlbumCollection();
            List<String> illegalOrphanMessages = null;
            for (Album albumCollectionOldAlbum : albumCollectionOld) {
                if (!albumCollectionNew.contains(albumCollectionOldAlbum)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Album " + albumCollectionOldAlbum + " since its idArtist field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Collection<Album> attachedAlbumCollectionNew = new ArrayList<Album>();
            for (Album albumCollectionNewAlbumToAttach : albumCollectionNew) {
                albumCollectionNewAlbumToAttach = em.getReference(albumCollectionNewAlbumToAttach.getClass(), albumCollectionNewAlbumToAttach.getId());
                attachedAlbumCollectionNew.add(albumCollectionNewAlbumToAttach);
            }
            albumCollectionNew = attachedAlbumCollectionNew;
            artist.setAlbumCollection(albumCollectionNew);
            artist = em.merge(artist);
            for (Album albumCollectionNewAlbum : albumCollectionNew) {
                if (!albumCollectionOld.contains(albumCollectionNewAlbum)) {
                    Artist oldIdArtistOfAlbumCollectionNewAlbum = albumCollectionNewAlbum.getIdArtist();
                    albumCollectionNewAlbum.setIdArtist(artist);
                    albumCollectionNewAlbum = em.merge(albumCollectionNewAlbum);
                    if (oldIdArtistOfAlbumCollectionNewAlbum != null && !oldIdArtistOfAlbumCollectionNewAlbum.equals(artist)) {
                        oldIdArtistOfAlbumCollectionNewAlbum.getAlbumCollection().remove(albumCollectionNewAlbum);
                        oldIdArtistOfAlbumCollectionNewAlbum = em.merge(oldIdArtistOfAlbumCollectionNewAlbum);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                BigDecimal id = artist.getId();
                if (findArtist(id) == null) {
                    throw new NonexistentEntityException("The artist with id " + id + " no longer exists.");
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
            Artist artist;
            try {
                artist = em.getReference(Artist.class, id);
                artist.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The artist with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<Album> albumCollectionOrphanCheck = artist.getAlbumCollection();
            for (Album albumCollectionOrphanCheckAlbum : albumCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Artist (" + artist + ") cannot be destroyed since the Album " + albumCollectionOrphanCheckAlbum + " in its albumCollection field has a non-nullable idArtist field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(artist);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Artist> findArtistEntities() {
        return findArtistEntities(true, -1, -1);
    }

    public List<Artist> findArtistEntities(int maxResults, int firstResult) {
        return findArtistEntities(false, maxResults, firstResult);
    }

    private List<Artist> findArtistEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Artist.class));
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

    public Artist findArtist(BigDecimal id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Artist.class, id);
        } finally {
            em.close();
        }
    }

    public int getArtistCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Artist> rt = cq.from(Artist.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
