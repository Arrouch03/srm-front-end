package ma.srm.srm.frontend.ui;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.util.Date;

import ma.srm.srm.frontend.R;
import ma.srm.srm.frontend.models.CompteurEau;
import ma.srm.srm.frontend.network.ApiClient;
import ma.srm.srm.frontend.network.ApiService;
import ma.srm.srm.frontend.utils.FileUtils;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddCompteurEauActivity extends AppCompatActivity {

    private EditText etNumeroEau, etDiametreEau;
    private Button btnChoosePhoto, btnSaveEau;
    private Uri selectedPhotoUri;

    private FusedLocationProviderClient fusedLocationClient;
    private double latitude = 0.0, longitude = 0.0;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;
    private static final int PICK_IMAGE_REQUEST_CODE = 102;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 103;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_compteur_eau);

        etNumeroEau = findViewById(R.id.etNumeroEau);
        etDiametreEau = findViewById(R.id.etDiametreEau);
        btnChoosePhoto = findViewById(R.id.btnChoosePhoto);
        btnSaveEau = findViewById(R.id.btnSaveEau);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestLocation();

        btnChoosePhoto.setOnClickListener(v -> checkStoragePermissionAndOpenGallery());
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

    private void checkStoragePermissionAndOpenGallery() {
        String perm = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
                Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{perm}, STORAGE_PERMISSION_REQUEST_CODE);
        } else {
            openGallery();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            selectedPhotoUri = data.getData();
            Toast.makeText(this, "Photo sélectionnée", Toast.LENGTH_SHORT).show();
        }
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

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        long userId = prefs.getLong("USER_ID", -1);
        if (userId == -1) {
            Toast.makeText(this, "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
            return;
        }

        CompteurEau compteur = new CompteurEau();
        compteur.setNumero(numero);
        compteur.setDiametre(diametre);
        compteur.setDatePose(new Date());
        compteur.setLatitude(latitude);
        compteur.setLongitude(longitude);
        compteur.setTypeId(1L); // Type Eau
        compteur.setUserId(userId);

        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.createCompteurEau(compteur).enqueue(new Callback<CompteurEau>() {
            @Override
            public void onResponse(Call<CompteurEau> call, Response<CompteurEau> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CompteurEau savedCompteur = response.body();
                    Toast.makeText(AddCompteurEauActivity.this, "Compteur Eau ajouté", Toast.LENGTH_SHORT).show();
                    if (selectedPhotoUri != null) {
                        uploadPhoto(savedCompteur.getId(), selectedPhotoUri);
                    } else {
                        returnResultOk();
                    }
                } else {
                    Toast.makeText(AddCompteurEauActivity.this, "Erreur ajout: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CompteurEau> call, Throwable t) {
                Toast.makeText(AddCompteurEauActivity.this, "Échec : " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void uploadPhoto(Long compteurId, Uri photoUri) {
        String path = FileUtils.getPath(this, photoUri);
        if (path == null) {
            Toast.makeText(this, "Impossible de récupérer le chemin de la photo", Toast.LENGTH_SHORT).show();
            Log.e("UPLOAD", "Path null pour photoUri: " + photoUri);
            returnResultOk();
            return;
        }

        File file = new File(path);
        RequestBody requestFile = RequestBody.create(file, MediaType.parse("image/*"));
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.uploadCompteurEauPhoto(compteurId, body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddCompteurEauActivity.this, "Photo uploadée avec succès", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AddCompteurEauActivity.this, "Erreur upload photo: " + response.code(), Toast.LENGTH_LONG).show();
                    Log.e("UPLOAD", "Erreur upload: " + response.code());
                }
                returnResultOk();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(AddCompteurEauActivity.this, "Échec upload photo: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("UPLOAD", "Échec upload photo", t);
                returnResultOk();
            }
        });
    }

    private void returnResultOk() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
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
        } else if (requestCode == STORAGE_PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        }
    }
}
