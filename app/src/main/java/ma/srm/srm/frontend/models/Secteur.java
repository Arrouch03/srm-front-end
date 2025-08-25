package ma.srm.srm.frontend.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Secteur implements Serializable {
    private Long id;
    private String nom;

    @SerializedName("ville")
    private String ville;

    // -------------------------
    // Getters & Setters
    // -------------------------
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }

    @Override
    public String toString() {
        return nom; // Affiche uniquement le nom du secteur dans le spinner
    }
}
