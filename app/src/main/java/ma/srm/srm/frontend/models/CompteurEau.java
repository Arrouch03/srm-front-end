package ma.srm.srm.frontend.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.Date;

public class CompteurEau implements Serializable {

    private Long id;
    private String numero;
    private double diametre;

    @SerializedName("datePose")
    private Date datePose;

    private Double latitude;
    private Double longitude;
    private String statut;

    @SerializedName("secteurId")
    private Long secteurId;

    @SerializedName("userId")
    private Long userId;

    @SerializedName("typeId")
    private Long typeId;

    // -------------------------
    // Getters & Setters
    // -------------------------
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public double getDiametre() { return diametre; }
    public void setDiametre(double diametre) { this.diametre = diametre; }

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

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getTypeId() { return typeId; }
    public void setTypeId(Long typeId) { this.typeId = typeId; }
}
