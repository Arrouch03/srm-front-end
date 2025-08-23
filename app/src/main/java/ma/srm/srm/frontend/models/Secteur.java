package ma.srm.srm.frontend.models;

import java.io.Serializable;

public class Secteur implements Serializable {
    private Long id;
    private String nom;
    private String ville;

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }
}
