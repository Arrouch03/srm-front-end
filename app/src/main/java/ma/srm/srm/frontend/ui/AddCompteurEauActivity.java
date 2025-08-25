package ma.srm.srm.frontend.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;

import java.io.File;
import java.util.Date;
import java.util.List;

import ma.srm.srm.frontend.R;
import ma.srm.srm.frontend.models.CompteurEau;
import ma.srm.srm.frontend.models.Secteur;
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

    private EditText etNumero, etDiametre;
    private Spinner spinnerStatut, spinnerSecteur;
    private Button btnChoosePhoto, btnSave;
    private Uri selectedPhotoUri;
    private FusedLocationProviderClient fusedLocationClient;
    private List<Secteur> secteurs;
    private double latitude = 0.0, longitude = 0.0;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final int PICK_IMAGE_REQUEST_CODE = 101;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 102;
    private static final String TAG = "AddCompteurEau";

    private final Long userId = 1L;   // Remplacer par ID réel
    private final Long typeId = 1L;   // Type compteur eau

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_compteur_eau);

        // Init views
        etNumero = findViewById(R.id.etNumero);
        etDiametre = findViewById(R.id.etDiametre);
        spinnerStatut = findViewById(R.id.spinnerStatut);
        spinnerSecteur = findViewById(R.id.spinnerSecteur);
        btnChoosePhoto = findViewById(R.id.btnChoosePhoto);
        btnSave = findViewById(R.id.btnSave);

        // Spinner Statut
        ArrayAdapter<String> adapterStatut = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Normal", "À contrôler", "Frauduleux", "Inaccessible"});
        adapterStatut.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatut.setAdapter(adapterStatut);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestLocation();
        loadSecteurs();

        btnChoosePhoto.setOnClickListener(v -> checkStoragePermissionAndOpenGallery());
        btnSave.setOnClickListener(v -> saveCompteurEau());
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
                Log.d(TAG, "Localisation récupérée: lat=" + latitude + ", lng=" + longitude);
            } else {
                Log.w(TAG, "Localisation introuvable !");
            }
        });
    }

    private void loadSecteurs() {
        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.getSecteurs().enqueue(new Callback<List<Secteur>>() {
            @Override
            public void onResponse(Call<List<Secteur>> call, Response<List<Secteur>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    secteurs = response.body();
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(AddCompteurEauActivity.this,
                            android.R.layout.simple_spinner_item,
                            secteurs.stream().map(Secteur::getNom).toArray(String[]::new));
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerSecteur.setAdapter(adapter);
                    Log.d(TAG, "Secteurs chargés: " + secteurs.size());
                } else {
                    Log.e(TAG, "Erreur chargement secteurs HTTP " + response.code());
                    Toast.makeText(AddCompteurEauActivity.this, "Erreur chargement secteurs", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Secteur>> call, Throwable t) {
                Log.e(TAG, "Échec chargement secteurs", t);
                Toast.makeText(AddCompteurEauActivity.this, "Échec: " + t.getMessage(), Toast.LENGTH_LONG).show();
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
            Log.d(TAG, "Photo sélectionnée: " + selectedPhotoUri);
        }
    }
    private void saveCompteurEau() {
        String numero = etNumero.getText().toString().trim();
        String diamStr = etDiametre.getText().toString().trim();

        if (numero.isEmpty() || diamStr.isEmpty()) {
            Toast.makeText(this, "Remplissez tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        double diametre;
        try {
            diametre = Double.parseDouble(diamStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Diamètre invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        int secteurPosition = spinnerSecteur.getSelectedItemPosition();
        if (secteurPosition < 0 || secteurs == null || secteurs.isEmpty()) {
            Toast.makeText(this, "Secteur obligatoire", Toast.LENGTH_SHORT).show();
            return;
        }

        String statut = spinnerStatut.getSelectedItem().toString();

        CompteurEau compteur = new CompteurEau();
        compteur.setNumero(numero);
        compteur.setDiametre(diametre);
        compteur.setDatePose(new Date());
        compteur.setLatitude(latitude);
        compteur.setLongitude(longitude);
        compteur.setStatut(statut);

        // ✅ Utilisation directe des IDs comme pour l'électricité
        compteur.setSecteurId(secteurs.get(secteurPosition).getId());
        compteur.setUserId(userId);
        compteur.setTypeId(typeId);

        // 🔹 Log JSON envoyé
        String json = new Gson().toJson(compteur);
        Log.d("AddCompteurEau", "JSON envoyé au backend: " + json);

        ApiService api = ApiClient.getClient().create(ApiService.class);
        Call<CompteurEau> call = api.createCompteurEau(compteur);

        call.enqueue(new Callback<CompteurEau>() {
            @Override
            public void onResponse(Call<CompteurEau> call, Response<CompteurEau> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.i("AddCompteurEau", "Compteur créé ID=" + response.body().getId());
                    Toast.makeText(AddCompteurEauActivity.this, "Compteur ajouté !", Toast.LENGTH_SHORT).show();

                    if (selectedPhotoUri != null) {
                        uploadPhoto(response.body().getId(), selectedPhotoUri);
                    } else {
                        finish();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "vide";
                        Log.e("AddCompteurEau", "Erreur HTTP " + response.code() + " - Body: " + errorBody);
                    } catch (Exception e) {
                        Log.e("AddCompteurEau", "Erreur parsing errorBody", e);
                    }
                    Toast.makeText(AddCompteurEauActivity.this, "Erreur ajout compteur", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CompteurEau> call, Throwable t) {
                Log.e("AddCompteurEau", "Échec réseau: " + t.getMessage(), t);
                Toast.makeText(AddCompteurEauActivity.this, "Erreur réseau", Toast.LENGTH_SHORT).show();
            }
        });
    }










    private void uploadPhoto(Long compteurId, Uri photoUri) {
        try {
            File file = FileUtils.uriToFile(photoUri, this);
            RequestBody reqFile = RequestBody.create(file, MediaType.parse("image/*"));
            MultipartBody.Part photoPart = MultipartBody.Part.createFormData("file", file.getName(), reqFile);

            Log.d(TAG, "Upload photo: compteurId=" + compteurId + ", fichier=" + file.getName());

            ApiService api = ApiClient.getClient().create(ApiService.class);
            api.uploadCompteurEauPhoto(compteurId, photoPart).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Log.i(TAG, "Photo upload réussie pour compteur " + compteurId);
                        Toast.makeText(AddCompteurEauActivity.this, "Compteur ajouté avec photo", Toast.LENGTH_SHORT).show();
                    } else {
                        try {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "vide";
                            Log.e(TAG, "Erreur upload photo HTTP " + response.code() + " - Body: " + errorBody);
                        } catch (Exception e) {
                            Log.e(TAG, "Erreur parsing errorBody upload photo", e);
                        }
                        Toast.makeText(AddCompteurEauActivity.this, "Compteur ajouté mais photo échouée", Toast.LENGTH_LONG).show();
                    }
                    finish();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e(TAG, "Échec upload photo", t);
                    Toast.makeText(AddCompteurEauActivity.this, "Compteur ajouté mais photo échouée", Toast.LENGTH_LONG).show();
                    finish();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Erreur conversion photo", e);
            Toast.makeText(this, "Erreur photo: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    private void uploadPhoto(ApiService api, Long compteurId, MultipartBody.Part photoPart) {
        api.uploadCompteurEauPhoto(compteurId, photoPart).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddCompteurEauActivity.this, "Compteur ajouté avec photo", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AddCompteurEauActivity.this, "Compteur ajouté mais photo échouée", Toast.LENGTH_LONG).show();
                }
                finish();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(AddCompteurEauActivity.this, "Photo échouée: " + t.getMessage(), Toast.LENGTH_LONG).show();
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
        } else if (requestCode == STORAGE_PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        }
    }
}
