package ma.srm.srm.frontend.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class CompteurElectricite {
    private Long id;
    private String numero;

    @SerializedName("calibre")
    private String calibre;   // correction : String car VARCHAR2 en base

    @SerializedName("datePose")
    private Date datePose;    // correction : Date car DATE en base

    private Double longitude;
    private Double latitude;

    @SerializedName("nbFils")
    private Integer nbFils;

    @SerializedName("nbRoues")
    private Integer nbRoues;

    @SerializedName("userId")
    private Long userId;

    @SerializedName("typeId")
    private Long typeId;

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getCalibre() { return calibre; }
    public void setCalibre(String calibre) { this.calibre = calibre; }

    public Date getDatePose() { return datePose; }
    public void setDatePose(Date datePose) { this.datePose = datePose; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Integer getNbFils() { return nbFils; }
    public void setNbFils(Integer nbFils) { this.nbFils = nbFils; }

    public Integer getNbRoues() { return nbRoues; }
    public void setNbRoues(Integer nbRoues) { this.nbRoues = nbRoues; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getTypeId() { return typeId; }
    public void setTypeId(Long typeId) { this.typeId = typeId; }
}
