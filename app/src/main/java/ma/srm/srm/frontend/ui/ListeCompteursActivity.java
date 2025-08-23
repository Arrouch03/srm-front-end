package ma.srm.srm.frontend.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ma.srm.srm.frontend.R;
import ma.srm.srm.frontend.models.CompteurEau;
import ma.srm.srm.frontend.models.CompteurElectricite;
import ma.srm.srm.frontend.network.ApiClient;
import ma.srm.srm.frontend.network.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListeCompteursActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CompteursAdapter adapter;
    private EditText etFiltre;

    private List<Object> allCompteurs = new ArrayList<>();
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liste_compteurs);

        recyclerView = findViewById(R.id.recyclerViewCompteurs);
        etFiltre = findViewById(R.id.etFiltre);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CompteursAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        apiService = ApiClient.getClient().create(ApiService.class);

        loadCompteurs();

        // 🔍 Filtrage par texte
        etFiltre.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCompteurs(s.toString().trim().toLowerCase());
            }
        });
    }

    private void loadCompteurs() {
        // Charger compteurs eau
        apiService.getCompteursEau().enqueue(new Callback<List<CompteurEau>>() {
            @Override
            public void onResponse(Call<List<CompteurEau>> call, Response<List<CompteurEau>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allCompteurs.addAll(response.body());
                    adapter.updateData(new ArrayList<>(allCompteurs));
                }
            }

            @Override
            public void onFailure(Call<List<CompteurEau>> call, Throwable t) {
                Toast.makeText(ListeCompteursActivity.this, "Erreur chargement eau", Toast.LENGTH_SHORT).show();
            }
        });

        // Charger compteurs électricité
        apiService.getCompteursElectricite().enqueue(new Callback<List<CompteurElectricite>>() {
            @Override
            public void onResponse(Call<List<CompteurElectricite>> call, Response<List<CompteurElectricite>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allCompteurs.addAll(response.body());
                    adapter.updateData(new ArrayList<>(allCompteurs));
                }
            }

            @Override
            public void onFailure(Call<List<CompteurElectricite>> call, Throwable t) {
                Toast.makeText(ListeCompteursActivity.this, "Erreur chargement électricité", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterCompteurs(String query) {
        List<Object> filtered = new ArrayList<>();
        query = query.toLowerCase();

        for (Object obj : allCompteurs) {
            boolean matchesType = false;
            boolean matchesStatut = false;

            if (obj instanceof CompteurEau) {
                CompteurEau c = (CompteurEau) obj;
                matchesType = query.equals("eau") || query.isEmpty();
                matchesStatut = c.getStatut() != null && c.getStatut().toLowerCase().contains(query);
            } else if (obj instanceof CompteurElectricite) {
                CompteurElectricite c = (CompteurElectricite) obj;
                matchesType = query.equals("electricite") || query.isEmpty();
                matchesStatut = c.getStatut() != null && c.getStatut().toLowerCase().contains(query);
            }

            if (matchesType || matchesStatut) {
                filtered.add(obj);
            }
        }

        adapter.updateData(filtered);
    }
}
