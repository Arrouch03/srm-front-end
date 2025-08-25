package ma.srm.srm.frontend.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.Date;

public class CompteurElectricite implements Serializable {

    private Long id;
    private String numero;
    private Integer nbFils;
    private Integer nbRoues;
    private String calibre;
    private Date datePose;
    private Double latitude;
    private Double longitude;
    private String statut;

    // ✅ Envoyer secteurId directement
    @SerializedName("secteurId")
    private Long secteurId;

    @SerializedName("user")
    private UserRef user;

    @SerializedName("type")
    private TypeRef type;

    // -------------------------
    // Classes internes pour références
    // -------------------------
    public static class UserRef implements Serializable {
        private Long id;
        public UserRef(Long id) { this.id = id; }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
    }

    public static class TypeRef implements Serializable {
        private Long id;
        public TypeRef(Long id) { this.id = id; }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
    }

    // -------------------------
    // Getters & Setters
    // -------------------------
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public Integer getNbFils() { return nbFils; }
    public void setNbFils(Integer nbFils) { this.nbFils = nbFils; }

    public Integer getNbRoues() { return nbRoues; }
    public void setNbRoues(Integer nbRoues) { this.nbRoues = nbRoues; }

    public String getCalibre() { return calibre; }
    public void setCalibre(String calibre) { this.calibre = calibre; }

    public Date getDatePose() { return datePose; }
    public void setDatePose(Date datePose) { this.datePose = datePose; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Long getSecteurId() { return secteurId; }
    public void setSecteurId(Long secteurId) { this.secteurId = secteurId; }

    public UserRef getUser() { return user; }
    public void setUser(UserRef user) { this.user = user; }

    public TypeRef getType() { return type; }
    public void setType(TypeRef type) { this.type = type; }
}
