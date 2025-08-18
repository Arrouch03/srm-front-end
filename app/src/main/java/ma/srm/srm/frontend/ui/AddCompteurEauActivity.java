package ma.srm.srm.frontend.ui;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.Date;

import ma.srm.srm.frontend.R;
import ma.srm.srm.frontend.models.CompteurEau;
import ma.srm.srm.frontend.network.ApiClient;
import ma.srm.srm.frontend.network.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddCompteurEauActivity extends AppCompatActivity {

    private EditText etNumeroEau, etDiametreEau;
    private Button btnSaveEau;

    private FusedLocationProviderClient fusedLocationClient;
    private double latitude = 0.0, longitude = 0.0;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_compteur_eau);

        // Récupération des vues
        etNumeroEau = findViewById(R.id.etNumeroEau);
        etDiametreEau = findViewById(R.id.etDiametreEau);
        btnSaveEau = findViewById(R.id.btnSaveEau);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestLocation();

        btnSaveEau.setOnClickListener(v -> saveCompteurEau());
    }

    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }
        });
    }

    private void saveCompteurEau() {
        String numero = etNumeroEau.getText().toString().trim();
        String diametreStr = etDiametreEau.getText().toString().trim();

        if (numero.isEmpty() || diametreStr.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        double diametre;
        try {
            diametre = Double.parseDouble(diametreStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Diamètre invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Récupérer l’ID utilisateur depuis le même SharedPreferences que LoginActivity
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        long userId = prefs.getLong("USER_ID", -1);

        if (userId == -1) {
            Toast.makeText(this, "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
            return;
        }

        // Construire l’objet CompteurEau
        CompteurEau compteur = new CompteurEau();
        compteur.setNumero(numero);
        compteur.setDiametre(diametre);
        compteur.setDatePose(new Date());
        compteur.setLatitude(latitude);
        compteur.setLongitude(longitude);
        compteur.setTypeId(1L); // 1 = Eau
        compteur.setUserId(userId);

        // Appel API
        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.createCompteurEau(compteur).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddCompteurEauActivity.this,
                            "Compteur Eau ajouté avec succès", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddCompteurEauActivity.this,
                            "Erreur lors de l'ajout (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(AddCompteurEauActivity.this,
                        "Échec : " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestLocation();
        }
    }
}
