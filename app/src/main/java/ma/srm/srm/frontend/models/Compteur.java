package ma.srm.srm.frontend.models;


public class Compteur {
    private Long id;
    private double latitude;
    private double longitude;
    private String numero;
    private String type; // eau ou electricite

    // getters + setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}

