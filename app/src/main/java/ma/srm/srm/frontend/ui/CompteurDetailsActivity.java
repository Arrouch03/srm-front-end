package ma.srm.srm.frontend.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import ma.srm.srm.frontend.R;
import ma.srm.srm.frontend.models.CompteurEau;
import ma.srm.srm.frontend.models.CompteurElectricite;

public class CompteurDetailsActivity extends AppCompatActivity {

    private TextView tvNumero, tvDatePose, tvLatitude, tvLongitude;
    private TextView tvDiametre, tvNbFils, tvNbRoues, tvCalibre;
    private LinearLayout sectionEau, sectionElectricite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compteur_details);

        tvNumero = findViewById(R.id.tvNumero);
        tvDatePose = findViewById(R.id.tvDatePose);
        tvLatitude = findViewById(R.id.tvLatitude);
        tvLongitude = findViewById(R.id.tvLongitude);

        // Champs spécifiques
        tvDiametre = findViewById(R.id.tvDiametre);
        tvNbFils = findViewById(R.id.tvNbFils);
        tvNbRoues = findViewById(R.id.tvNbRoues);
        tvCalibre = findViewById(R.id.tvCalibre);

        sectionEau = findViewById(R.id.sectionEau);
        sectionElectricite = findViewById(R.id.sectionElectricite);

        // Vérifier quel type de compteur est reçu
        Object obj = getIntent().getSerializableExtra("compteur");

        if (obj instanceof CompteurEau) {
            CompteurEau compteur = (CompteurEau) obj;
            afficherCompteurEau(compteur);
        } else if (obj instanceof CompteurElectricite) {
            CompteurElectricite compteur = (CompteurElectricite) obj;
            afficherCompteurElectricite(compteur);
        }
    }

    private void afficherCompteurEau(CompteurEau compteur) {
        sectionEau.setVisibility(View.VISIBLE);
        tvNumero.setText("Numéro : " + compteur.getNumero());
        tvDatePose.setText("Date pose : " + compteur.getDatePose());
        tvLatitude.setText("Latitude : " + compteur.getLatitude());
        tvLongitude.setText("Longitude : " + compteur.getLongitude());
        tvDiametre.setText("Diamètre : " + compteur.getDiametre());
    }

    private void afficherCompteurElectricite(CompteurElectricite compteur) {
        sectionElectricite.setVisibility(View.VISIBLE);
        tvNumero.setText("Numéro : " + compteur.getNumero());
        tvDatePose.setText("Date pose : " + compteur.getDatePose());
        tvLatitude.setText("Latitude : " + compteur.getLatitude());
        tvLongitude.setText("Longitude : " + compteur.getLongitude());
        tvNbFils.setText("Nb fils : " + compteur.getNbFils());
        tvNbRoues.setText("Nb roues : " + compteur.getNbRoues());
        tvCalibre.setText("Calibre : " + compteur.getCalibre());
    }
}
