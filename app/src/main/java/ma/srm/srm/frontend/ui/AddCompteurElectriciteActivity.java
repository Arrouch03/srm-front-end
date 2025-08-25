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
import ma.srm.srm.frontend.models.CompteurElectricite;
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

public class AddCompteurElectriciteActivity extends AppCompatActivity {

    private EditText etNumero, etNbFils, etNbRoues, etCalibre;
    private Spinner spinnerStatut, spinnerSecteur;
    private Button btnChoosePhoto, btnSave;
    private Uri selectedPhotoUri;
    private FusedLocationProviderClient fusedLocationClient;

    private double latitude = 0.0;
    private double longitude = 0.0;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final int PICK_IMAGE_REQUEST_CODE = 101;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 102;

    private List<Secteur> secteurs;

    private final Long userId = 1L;  // ID utilisateur connecté
    private final Long typeId = 1L;  // ID type compteur électricité

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_compteur_electricite);

        // Init views
        etNumero = findViewById(R.id.etNumero);
        etNbFils = findViewById(R.id.etNbFils);
        etNbRoues = findViewById(R.id.etNbRoues);
        etCalibre = findViewById(R.id.etCalibre);
        spinnerStatut = findViewById(R.id.spinnerStatut);
        spinnerSecteur = findViewById(R.id.spinnerSecteur);
        btnChoosePhoto = findViewById(R.id.btnChoosePhoto);
        btnSave = findViewById(R.id.btnSave);

        // Remplir spinner Statut
        ArrayAdapter<String> adapterStatut = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Normal", "À contrôler", "Frauduleux", "Inaccessible"});
        adapterStatut.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatut.setAdapter(adapterStatut);

        // Localisation
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestLocation();

        // Charger secteurs
        loadSecteurs();

        btnChoosePhoto.setOnClickListener(v -> checkStoragePermissionAndOpenGallery());
        btnSave.setOnClickListener(v -> saveCompteurElectricite());
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
                Log.d("AddCompteurElec", "Localisation détectée: lat=" + latitude + ", long=" + longitude);
            } else {
                Log.w("AddCompteurElec", "Aucune localisation trouvée");
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
                    ArrayAdapter<Secteur> adapter = new ArrayAdapter<>(AddCompteurElectriciteActivity.this,
                            android.R.layout.simple_spinner_item,
                            secteurs);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerSecteur.setAdapter(adapter);
                    Log.d("AddCompteurElec", "Secteurs chargés: " + secteurs.size());
                } else {
                    Toast.makeText(AddCompteurElectriciteActivity.this,
                            "Erreur chargement secteurs", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Secteur>> call, Throwable t) {
                Toast.makeText(AddCompteurElectriciteActivity.this,
                        "Échec chargement secteurs: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
            Log.d("AddCompteurElec", "Photo sélectionnée: " + selectedPhotoUri);
        }
    }

    private void saveCompteurElectricite() {
        String numero = etNumero.getText().toString().trim();
        String calibre = etCalibre.getText().toString().trim();
        String nbFilsStr = etNbFils.getText().toString().trim();
        String nbRouesStr = etNbRoues.getText().toString().trim();

        if (numero.isEmpty() || calibre.isEmpty() || nbFilsStr.isEmpty() || nbRouesStr.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        int nbFils, nbRoues;
        try {
            nbFils = Integer.parseInt(nbFilsStr);
            nbRoues = Integer.parseInt(nbRouesStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Nombre de fils / roues invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        Secteur secteur = (Secteur) spinnerSecteur.getSelectedItem();
        if (secteur == null) {
            Toast.makeText(this, "Veuillez choisir un secteur", Toast.LENGTH_SHORT).show();
            return;
        }

        String statut = spinnerStatut.getSelectedItem().toString();

        CompteurElectricite compteur = new CompteurElectricite();
        compteur.setNumero(numero);
        compteur.setCalibre(calibre);
        compteur.setNbFils(nbFils);
        compteur.setNbRoues(nbRoues);
        compteur.setDatePose(new Date());
        compteur.setLatitude(latitude);
        compteur.setLongitude(longitude);
        compteur.setStatut(statut);

        // ✅ Utiliser secteurId directement
        compteur.setSecteurId(secteur.getId());
        compteur.setUser(new CompteurElectricite.UserRef(userId));
        compteur.setType(new CompteurElectricite.TypeRef(typeId));

        // 🔹 Log JSON envoyé
        String json = new Gson().toJson(compteur);
        Log.d("AddCompteurElec", "JSON envoyé au backend: " + json);

        ApiService api = ApiClient.getClient().create(ApiService.class);
        Call<CompteurElectricite> call = api.createCompteurElectricite(compteur);

        call.enqueue(new Callback<CompteurElectricite>() {
            @Override
            public void onResponse(Call<CompteurElectricite> call, Response<CompteurElectricite> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.i("AddCompteurElec", "Succès: compteur créé avec ID=" + response.body().getId());
                    Toast.makeText(AddCompteurElectriciteActivity.this,
                            "Compteur électricité ajouté !", Toast.LENGTH_SHORT).show();

                    if (selectedPhotoUri != null) {
                        uploadPhoto(response.body().getId(), selectedPhotoUri);
                    } else {
                        finish();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "vide";
                        Log.e("AddCompteurElec", "Erreur HTTP " + response.code() + " - Body: " + errorBody);
                    } catch (Exception e) {
                        Log.e("AddCompteurElec", "Erreur parsing errorBody", e);
                    }
                    Toast.makeText(AddCompteurElectriciteActivity.this, "Erreur d'ajout", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CompteurElectricite> call, Throwable t) {
                Log.e("AddCompteurElec", "Échec réseau: " + t.getMessage(), t);
                Toast.makeText(AddCompteurElectriciteActivity.this, "Erreur réseau", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadPhoto(Long compteurId, Uri photoUri) {
        try {
            File file = FileUtils.uriToFile(photoUri, this);
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

            Log.d("AddCompteurElec", "Upload photo: compteurId=" + compteurId + ", fichier=" + file.getName());

            ApiService api = ApiClient.getClient().create(ApiService.class);
            api.uploadCompteurElectricitePhoto(compteurId, body).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    Log.i("AddCompteurElec", "Photo upload OK, réponse=" + response.code());
                    finish();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("AddCompteurElec", "Échec upload photo: " + t.getMessage(), t);
                    finish();
                }
            });
        } catch (Exception e) {
            Log.e("AddCompteurElec", "Erreur upload photo", e);
            finish();
        }
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
