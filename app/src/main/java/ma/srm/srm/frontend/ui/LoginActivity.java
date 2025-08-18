package ma.srm.srm.frontend.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ma.srm.srm.frontend.R;
import ma.srm.srm.frontend.models.User;
import ma.srm.srm.frontend.network.ApiClient;
import ma.srm.srm.frontend.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

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
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);

        apiService.login(user).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User loggedUser = response.body();

                    if (loggedUser.getId() == null) {
                        Toast.makeText(LoginActivity.this, "Erreur : l'API ne renvoie pas d'ID utilisateur", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // ✅ Sauvegarde de l'ID utilisateur dans "user_prefs"
                    SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putLong("USER_ID", loggedUser.getId());
                    editor.apply();

                    Toast.makeText(LoginActivity.this, "Connexion réussie", Toast.LENGTH_SHORT).show();

                    // ✅ Redirection vers la carte
                    Intent intent = new Intent(LoginActivity.this, MapActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Identifiants invalides", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Erreur serveur: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
