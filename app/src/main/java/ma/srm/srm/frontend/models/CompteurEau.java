package ma.srm.srm.frontend.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.Date;

public class CompteurEau implements java.io.Serializable {
    private Long id;
    private String numero;
    private Double diametre;

    @SerializedName("datePose")
    private Date datePose;

    private Double longitude;
    private Double latitude;

    @SerializedName("userId")
    private Long userId;

    @SerializedName("typeId")
    private Long typeId;

    // Nouvelle propriété pour la photo en Base64
    private String photo;

    // ✅ Nouveau champ statut
    private String statut;

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public Double getDiametre() { return diametre; }
    public void setDiametre(Double diametre) { this.diametre = diametre; }

    public Date getDatePose() { return datePose; }
    public void setDatePose(Date datePose) { this.datePose = datePose; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getTypeId() { return typeId; }
    public void setTypeId(Long typeId) { this.typeId = typeId; }

    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
}
