package ma.srm.srm.frontend.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import ma.srm.srm.frontend.R;
import ma.srm.srm.frontend.models.CompteurEau;
import ma.srm.srm.frontend.models.CompteurElectricite;

public class CompteurDetailsActivity extends AppCompatActivity {

    private TextView tvNumero, tvDatePose, tvLatitude, tvLongitude, tvStatut;
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
        tvStatut = findViewById(R.id.tvStatut);

        tvDiametre = findViewById(R.id.tvDiametre);
        tvNbFils = findViewById(R.id.tvNbFils);
        tvNbRoues = findViewById(R.id.tvNbRoues);
        tvCalibre = findViewById(R.id.tvCalibre);

        sectionEau = findViewById(R.id.sectionEau);
        sectionElectricite = findViewById(R.id.sectionElectricite);

        Object obj = getIntent().getSerializableExtra("compteur");

        if (obj instanceof CompteurEau) {
            afficherCompteurEau((CompteurEau) obj);
        } else if (obj instanceof CompteurElectricite) {
            afficherCompteurElectricite((CompteurElectricite) obj);
        }
    }

    private void afficherCompteurEau(CompteurEau compteur) {
        sectionEau.setVisibility(View.VISIBLE);
        tvNumero.setText("Numéro : " + compteur.getNumero());
        tvDatePose.setText("Date pose : " + compteur.getDatePose());
        tvLatitude.setText("Latitude : " + compteur.getLatitude());
        tvLongitude.setText("Longitude : " + compteur.getLongitude());
        tvDiametre.setText("Diamètre : " + compteur.getDiametre());
        afficherStatut(compteur.getStatut());
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
        afficherStatut(compteur.getStatut());
    }

    private void afficherStatut(String statut) {
        if (statut == null) {
            tvStatut.setText("Statut : N/A");
            tvStatut.setTextColor(Color.GRAY);
            return;
        }

        tvStatut.setText("Statut : " + statut);

        switch (statut) {
            case "À contrôler":
                tvStatut.setTextColor(Color.parseColor("#FFA500")); // Orange
                break;
            case "Frauduleux":
                tvStatut.setTextColor(Color.RED);
                break;
            case "Inaccessible":
                tvStatut.setTextColor(Color.GRAY);
                break;
            default:
                tvStatut.setTextColor(Color.BLACK);
        }
    }
}
