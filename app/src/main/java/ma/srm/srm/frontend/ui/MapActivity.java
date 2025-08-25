package ma.srm.srm.frontend.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

import ma.srm.srm.frontend.R;
import ma.srm.srm.frontend.models.CompteurEau;
import ma.srm.srm.frontend.models.CompteurElectricite;
import ma.srm.srm.frontend.models.PositionUpdate;
import ma.srm.srm.frontend.network.ApiClient;
import ma.srm.srm.frontend.network.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQ_LOCATION = 1001;

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private ApiService apiService;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FloatingActionButton fabAdd;

    private final List<CompteurEau> compteursEau = new ArrayList<>();
    private final List<CompteurElectricite> compteursElectricite = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        apiService = ApiClient.getClient().create(ApiService.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        fabAdd = findViewById(R.id.fab_add);

        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this::onNavItemSelected);
        }

        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> showAddCompteurDialog());
        }

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCompteursFromAPI();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        if (hasLocationPermission()) {
            enableMyLocationAndCenter();
        } else {
            requestLocationPermission();
        }

        mMap.setOnMarkerClickListener(this::onMarkerClick);

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) { }

            @Override
            public void onMarkerDrag(Marker marker) { }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                LatLng newPos = marker.getPosition();
                Object compteur = marker.getTag();
                Log.d("MapActivity", "Marker drag ended: " + newPos.latitude + ", " + newPos.longitude);

                if (compteur != null) {
                    updateCompteurPosition(compteur, newPos.latitude, newPos.longitude);
                }
            }
        });

        loadCompteursFromAPI();
    }

    private boolean onMarkerClick(Marker marker) {
        LatLng pos = marker.getPosition();
        List<Object> nearby = getCompteursNear(pos);

        if (nearby.size() == 1) {
            openCompteurDetails(nearby.get(0));
        } else if (nearby.size() > 1) {
            openListeCompteursDialog(nearby);
        }
        return true;
    }

    private List<Object> getCompteursNear(LatLng pos) {
        List<Object> result = new ArrayList<>();
        double rayon = 20; // distance en mètres

        for (CompteurEau c : compteursEau) {
            if (c.getLatitude() != null && c.getLongitude() != null &&
                    distanceEnMetres(pos.latitude, pos.longitude, c.getLatitude(), c.getLongitude()) <= rayon) {
                result.add(c);
            }
        }

        for (CompteurElectricite c : compteursElectricite) {
            if (c.getLatitude() != null && c.getLongitude() != null &&
                    distanceEnMetres(pos.latitude, pos.longitude, c.getLatitude(), c.getLongitude()) <= rayon) {
                result.add(c);
            }
        }

        return result;
    }

    private double distanceEnMetres(double lat1, double lon1, Double lat2, Double lon2) {
        if (lat2 == null || lon2 == null) return Double.MAX_VALUE;

        final int R = 6371000; // rayon Terre en mètres
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private void openCompteurDetails(Object compteur) {
        Intent intent = new Intent(this, CompteurDetailsActivity.class);
        intent.putExtra("compteur", (java.io.Serializable) compteur);
        startActivity(intent);
    }

    private void openListeCompteursDialog(List<Object> compteurs) {
        String[] items = new String[compteurs.size()];
        for (int i = 0; i < compteurs.size(); i++) {
            Object c = compteurs.get(i);
            if (c instanceof CompteurEau) items[i] = "Eau - " + ((CompteurEau) c).getNumero();
            else if (c instanceof CompteurElectricite) items[i] = "Élec - " + ((CompteurElectricite) c).getNumero();
        }

        new AlertDialog.Builder(this)
                .setTitle("Compteurs disponibles")
                .setItems(items, (dialog, which) -> openCompteurDetails(compteurs.get(which)))
                .show();
    }

    private boolean onNavItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_refresh) {
            loadCompteursFromAPI();
            closeDrawer();
            return true;
        } else if (id == R.id.nav_add_compteur) {
            showAddCompteurDialog();
            closeDrawer();
            return true;
        } else if (id == R.id.nav_liste_compteurs) {
            startActivity(new Intent(this, ListeCompteursActivity.class));
            closeDrawer();
            return true;
        } else if (id == R.id.nav_logout) {
            Toast.makeText(this, "Déconnexion…", Toast.LENGTH_SHORT).show();
            finish();
            return true;
        }
        return false;
    }

    private void closeDrawer() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    private void showAddCompteurDialog() {
        String[] types = {"Eau", "Électricité"};
        new AlertDialog.Builder(this)
                .setTitle("Ajouter un compteur")
                .setItems(types, (d, which) -> {
                    if (which == 0) startActivity(new Intent(this, AddCompteurEauActivity.class));
                    else startActivity(new Intent(this, AddCompteurElectriciteActivity.class));
                })
                .show();
    }

    private void addMarker(double lat, double lng, String title, String snippet, float color, Object compteur) {
        LatLng pos = new LatLng(lat, lng);
        Marker marker = mMap.addMarker(new com.google.android.gms.maps.model.MarkerOptions()
                .position(pos)
                .title(title)
                .snippet(snippet)
                .icon(BitmapDescriptorFactory.defaultMarker(color))
                .draggable(true));
        if (marker != null) marker.setTag(compteur);
    }

    private void loadCompteursFromAPI() {
        if (mMap == null) return;

        mMap.clear();
        compteursEau.clear();
        compteursElectricite.clear();

        apiService.getCompteursEau().enqueue(new Callback<List<CompteurEau>>() {
            @Override
            public void onResponse(Call<List<CompteurEau>> call, Response<List<CompteurEau>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    compteursEau.addAll(response.body());
                    for (CompteurEau c : compteursEau) {
                        if (c.getLatitude() == null || c.getLongitude() == null) continue;
                        addMarker(c.getLatitude(), c.getLongitude(),
                                "Eau - " + safe(c.getNumero()), "ID: " + c.getId(),
                                BitmapDescriptorFactory.HUE_BLUE, c);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<CompteurEau>> call, Throwable t) {
                Log.e("MapActivity", "Erreur compteurs eau: " + t.getMessage());
            }
        });

        apiService.getCompteursElectricite().enqueue(new Callback<List<CompteurElectricite>>() {
            @Override
            public void onResponse(Call<List<CompteurElectricite>> call, Response<List<CompteurElectricite>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    compteursElectricite.addAll(response.body());
                    for (CompteurElectricite c : compteursElectricite) {
                        if (c.getLatitude() == null || c.getLongitude() == null) continue;
                        addMarker(c.getLatitude(), c.getLongitude(),
                                "Élec - " + safe(c.getNumero()), "ID: " + c.getId(),
                                BitmapDescriptorFactory.HUE_YELLOW, c);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<CompteurElectricite>> call, Throwable t) {
                Log.e("MapActivity", "Erreur compteurs électricité: " + t.getMessage());
            }
        });
    }

    private void updateCompteurPosition(Object compteur, double lat, double lng) {
        PositionUpdate pos = new PositionUpdate(lat, lng);

        if (compteur instanceof CompteurEau) {
            CompteurEau c = (CompteurEau) compteur;
            c.setLatitude(lat);
            c.setLongitude(lng);

            Call<Void> call = apiService.updateCompteurEauPosition(c.getId(), pos);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(MapActivity.this, "Position Eau mise à jour", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MapActivity.this, "Erreur API Eau: " + response.code(), Toast.LENGTH_LONG).show();
                        Log.e("MapActivity", "Erreur réponse API Eau: " + response.code() + " - " + response.message());
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(MapActivity.this, "Erreur mise à jour Eau: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("MapActivity", "Erreur réseau Eau", t);
                }
            });

        } else if (compteur instanceof CompteurElectricite) {
            CompteurElectricite c = (CompteurElectricite) compteur;
            c.setLatitude(lat);
            c.setLongitude(lng);

            Call<Void> call = apiService.updateCompteurElectricitePosition(c.getId(), pos);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(MapActivity.this, "Position Électricité mise à jour", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MapActivity.this, "Erreur API Élec: " + response.code(), Toast.LENGTH_LONG).show();
                        Log.e("MapActivity", "Erreur réponse API Élec: " + response.code() + " - " + response.message());
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(MapActivity.this, "Erreur mise à jour Élec: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("MapActivity", "Erreur réseau Élec", t);
                }
            });
        }
    }

    private void enableMyLocationAndCenter() {
        try { if (mMap != null) mMap.setMyLocationEnabled(true); } catch (SecurityException ignore) {}

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null && mMap != null) {
                LatLng myPos = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPos, 15));
            }
        });
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQ_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOCATION &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableMyLocationAndCenter();
            loadCompteursFromAPI();
        } else {
            Toast.makeText(this, "Permission localisation refusée", Toast.LENGTH_SHORT).show();
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
