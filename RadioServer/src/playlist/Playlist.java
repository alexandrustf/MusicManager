/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package playlist;

import java.io.Serializable;
import java.math.BigInteger;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Alex Stefan
 */
@Entity
@Table(name = "PLAYLIST")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Playlist.findAll", query = "SELECT p FROM Playlist p")
    , @NamedQuery(name = "Playlist.findByNume", query = "SELECT p FROM Playlist p WHERE p.nume = :nume")
    , @NamedQuery(name = "Playlist.findByPlayed", query = "SELECT p FROM Playlist p WHERE p.played = :played")})
public class Playlist implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "NUME")
    private String nume;
    @Basic(optional = false)
    @Column(name = "PLAYED")
    private BigInteger played;

    public Playlist() {
    }

    public Playlist(String nume) {
        this.nume = nume;
    }

    public Playlist(String nume, BigInteger played) {
        this.nume = nume;
        this.played = played;
    }

    public String getNume() {
        return nume;
    }

    public void setNume(String nume) {
        this.nume = nume;
    }

    public BigInteger getPlayed() {
        return played;
    }

    public void setPlayed(BigInteger played) {
        this.played = played;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (nume != null ? nume.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Playlist)) {
            return false;
        }
        Playlist other = (Playlist) object;
        if ((this.nume == null && other.nume != null) || (this.nume != null && !this.nume.equals(other.nume))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "playlist.Playlist[ nume=" + nume + " ]";
    }
    
}
