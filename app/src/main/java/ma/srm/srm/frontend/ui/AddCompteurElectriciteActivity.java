package ma.srm.srm.frontend.ui;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.util.Date;

import ma.srm.srm.frontend.R;
import ma.srm.srm.frontend.models.CompteurElectricite;
import ma.srm.srm.frontend.network.ApiClient;
import ma.srm.srm.frontend.network.ApiService;
import ma.srm.srm.frontend.utils.FileUtils;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddCompteurElectriciteActivity extends AppCompatActivity {

    private EditText etNumero, etNbFils, etNbRoues, etCalibre;
    private Button btnChoosePhoto, btnSave;
    private Uri selectedPhotoUri;
    private FusedLocationProviderClient fusedLocationClient;

    private double latitude = 0.0;
    private double longitude = 0.0;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final int PICK_IMAGE_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_compteur_electricite);

        etNumero = findViewById(R.id.etNumero);
        etNbFils = findViewById(R.id.etNbFils);
        etNbRoues = findViewById(R.id.etNbRoues);
        etCalibre = findViewById(R.id.etCalibre);
        btnChoosePhoto = findViewById(R.id.btnChoosePhoto);
        btnSave = findViewById(R.id.btnSave);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestLocation();

        btnChoosePhoto.setOnClickListener(v -> openGallery());
        btnSave.setOnClickListener(v -> saveCompteur());
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

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            selectedPhotoUri = data.getData();
            Toast.makeText(this, "Photo sélectionnée", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveCompteur() {
        String numero = etNumero.getText().toString().trim();
        String nbFilsStr = etNbFils.getText().toString().trim();
        String nbRouesStr = etNbRoues.getText().toString().trim();
        String calibre = etCalibre.getText().toString().trim();

        if (numero.isEmpty() || nbFilsStr.isEmpty() || nbRouesStr.isEmpty() || calibre.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        int nbFils, nbRoues;
        try {
            nbFils = Integer.parseInt(nbFilsStr);
            nbRoues = Integer.parseInt(nbRouesStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Nombre de fils ou de roues invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        long userId = prefs.getLong("USER_ID", -1);
        if (userId == -1) {
            Toast.makeText(this, "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
            return;
        }

        CompteurElectricite compteur = new CompteurElectricite();
        compteur.setNumero(numero);
        compteur.setNbFils(nbFils);
        compteur.setNbRoues(nbRoues);
        compteur.setCalibre(calibre);
        compteur.setUserId(userId);
        compteur.setTypeId(2L); // 2 = Electricité
        compteur.setLatitude(latitude);
        compteur.setLongitude(longitude);
        compteur.setDatePose(new Date());

        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.createCompteurElectricite(compteur).enqueue(new Callback<CompteurElectricite>() {
            @Override
            public void onResponse(Call<CompteurElectricite> call, Response<CompteurElectricite> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Long compteurId = response.body().getId(); // récupère l'ID renvoyé par le backend
                    Toast.makeText(AddCompteurElectriciteActivity.this,
                            "Compteur ajouté avec succès", Toast.LENGTH_SHORT).show();

                    if (selectedPhotoUri != null) {
                        uploadPhoto(compteurId, selectedPhotoUri);
                    } else {
                        finish();
                    }
                } else {
                    Toast.makeText(AddCompteurElectriciteActivity.this,
                            "Erreur ajout compteur: " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<CompteurElectricite> call, Throwable t) {
                Toast.makeText(AddCompteurElectriciteActivity.this,
                        "Échec : " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void uploadPhoto(Long compteurId, Uri photoUri) {
        String path = FileUtils.getPath(this, photoUri);
        if (path == null) {
            Toast.makeText(this, "Impossible de récupérer le chemin de la photo", Toast.LENGTH_SHORT).show();
            return;
        }

        File file = new File(path);
        RequestBody requestFile = RequestBody.create(file, MediaType.parse("image/*"));
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.uploadCompteurElectricitePhoto(compteurId, body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddCompteurElectriciteActivity.this,
                            "Photo uploadée avec succès", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AddCompteurElectriciteActivity.this,
                            "Erreur upload photo: " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
                finish();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(AddCompteurElectriciteActivity.this,
                        "Échec upload photo: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestLocation();
        }
    }
}
