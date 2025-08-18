package ma.srm.srm.frontend.ui;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;

import java.util.Date;

import ma.srm.srm.frontend.R;
import ma.srm.srm.frontend.models.CompteurElectricite;
import ma.srm.srm.frontend.network.ApiClient;
import ma.srm.srm.frontend.network.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddCompteurElectriciteActivity extends AppCompatActivity {

    private EditText etNumero, etNbFils, etNbRoues, etCalibre;
    private Button btnSave;
    private FusedLocationProviderClient fusedLocationClient;

    private double latitude = 0.0;
    private double longitude = 0.0;

    private static final int LOCATION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_compteur_electricite);

        // Récupération des vues
        etNumero = findViewById(R.id.etNumero);
        etNbFils = findViewById(R.id.etNbFils);
        etNbRoues = findViewById(R.id.etNbRoues);
        etCalibre = findViewById(R.id.etCalibre);
        btnSave = findViewById(R.id.btnSave);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestLocation();

        btnSave.setOnClickListener(v -> saveCompteur());
    }

    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }
        });
    }

    private void saveCompteur() {
        String numero = etNumero.getText().toString();
        String nbFilsStr = etNbFils.getText().toString();
        String nbRouesStr = etNbRoues.getText().toString();
        String calibre = etCalibre.getText().toString();

        if (numero.isEmpty() || nbFilsStr.isEmpty() || nbRouesStr.isEmpty() || calibre.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        int nbFils = Integer.parseInt(nbFilsStr);
        int nbRoues = Integer.parseInt(nbRouesStr);

        // Récupérer l’ID utilisateur depuis SharedPreferences
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        long userId = prefs.getLong("USER_ID", -1);

        if (userId == -1) {
            Toast.makeText(this, "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
            return;
        }

        // Construire l’objet compteur
        CompteurElectricite compteur = new CompteurElectricite();
        compteur.setNumero(numero);
        compteur.setNbFils(nbFils);
        compteur.setNbRoues(nbRoues);
        compteur.setCalibre(calibre);
        compteur.setUserId(userId);
        compteur.setTypeId(2L); // 2 = Electricité
        compteur.setLatitude(latitude);
        compteur.setLongitude(longitude);
        compteur.setDatePose(new Date()); // Date actuelle

        // Log JSON envoyé pour debug
        String jsonToSend = new Gson().toJson(compteur);
        Log.d("API_REQUEST", "JSON envoyé : " + jsonToSend);

        // Envoi API
        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.createCompteurElectricite(compteur).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddCompteurElectriciteActivity.this,
                            "Compteur ajouté avec succès", Toast.LENGTH_SHORT).show();
                    finish(); // revenir à la carte
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e("API_ERROR", "Code: " + response.code() + " - " + errorBody);
                        Toast.makeText(AddCompteurElectriciteActivity.this,
                                "Erreur " + response.code() + " : " + errorBody,
                                Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(AddCompteurElectriciteActivity.this,
                                "Erreur serveur " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("API_FAILURE", "Erreur réseau", t);
                Toast.makeText(AddCompteurElectriciteActivity.this,
                        "Échec : " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestLocation();
        }
    }
}
