package ma.srm.srm.frontend.models;

public class PositionUpdate {
    private double latitude;
    private double longitude;

    public PositionUpdate(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
}

