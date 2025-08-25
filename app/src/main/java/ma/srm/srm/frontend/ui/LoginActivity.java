package ma.srm.srm.frontend.ui;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import ma.srm.srm.frontend.R;
import ma.srm.srm.frontend.models.Secteur;
import ma.srm.srm.frontend.models.User;
import ma.srm.srm.frontend.network.ApiClient;
import ma.srm.srm.frontend.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText usernameEditText, passwordEditText;
    private Button loginButton;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.editTextUsername);
        passwordEditText = findViewById(R.id.editTextPassword);
        loginButton = findViewById(R.id.buttonLogin);

        apiService = ApiClient.getClient().create(ApiService.class);

        loginButton.setOnClickListener(v -> login());
    }

    private void login() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Champs vides");
            return;
        }

        Log.d(TAG, "Tentative de login avec username: " + username);

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);

        apiService.login(user).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                Log.d(TAG, "Login response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    User loggedUser = response.body();
                    Log.d(TAG, "Utilisateur récupéré: " + loggedUser.getUsername() + ", ID=" + loggedUser.getId());

                    if (loggedUser.getId() == null) {
                        Toast.makeText(LoginActivity.this, "Erreur : l'API ne renvoie pas d'ID utilisateur", Toast.LENGTH_LONG).show();
                        Log.d(TAG, "ID utilisateur null");
                        return;
                    }

                    // Sauvegarde de l'ID utilisateur
                    SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putLong("USER_ID", loggedUser.getId());
                    editor.apply();

                    Toast.makeText(LoginActivity.this, "Connexion réussie", Toast.LENGTH_SHORT).show();

                    // 🔹 Appel API pour récupérer le secteur du jour
                    Log.d(TAG, "Appel API pour récupérer le secteur du jour");
                    apiService.getSecteurDuJour(loggedUser.getId()).enqueue(new Callback<Secteur>() {
                        @Override
                        public void onResponse(Call<Secteur> call, Response<Secteur> response) {
                            Log.d(TAG, "Secteur response code: " + response.code());

                            if (response.isSuccessful() && response.body() != null) {
                                Secteur secteurDuJour = response.body();
                                Log.d(TAG, "Secteur récupéré: " + secteurDuJour.getNom());
                                Toast.makeText(LoginActivity.this, "Secteur du jour: " + secteurDuJour.getNom(), Toast.LENGTH_LONG).show();
                                afficherNotification(secteurDuJour.getNom());
                            } else {
                                Log.d(TAG, "Aucun secteur récupéré ou réponse non successful");
                                Toast.makeText(LoginActivity.this, "Aucun secteur à vérifier aujourd'hui", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Secteur> call, Throwable t) {
                            Log.e(TAG, "Erreur lors de la récupération du secteur", t);
                            Toast.makeText(LoginActivity.this, "Erreur API secteur: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

                    // Redirection vers la carte
                    Intent intent = new Intent(LoginActivity.this, MapActivity.class);
                    startActivity(intent);
                    finish();

                } else {
                    Toast.makeText(LoginActivity.this, "Identifiants invalides", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Login échoué ou response body null");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Erreur serveur: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Erreur login API", t);
            }
        });
    }

    private void afficherNotification(String nomSecteur) {
        Log.d(TAG, "Tentative d'affichage notification pour secteur: " + nomSecteur);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(notificationManager == null) {
            Log.e(TAG, "NotificationManager null");
            return;
        }

        String channelId = "secteur_du_jour";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Secteur du jour",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Secteur à contrôler aujourd'hui")
                .setContentText(nomSecteur)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify(1, builder.build());
        Log.d(TAG, "Notification affichée");
    }
}
