package ma.srm.srm.frontend.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import ma.srm.srm.frontend.R;
import ma.srm.srm.frontend.models.CompteurEau;
import ma.srm.srm.frontend.models.CompteurElectricite;

public class CompteursAdapter extends RecyclerView.Adapter<CompteursAdapter.ViewHolder> {

    private List<Object> compteurs;

    // ✅ Le constructeur doit avoir le même nom que la classe
    public CompteursAdapter(List<Object> compteurs) {
        this.compteurs = compteurs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_compteur, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Object obj = compteurs.get(position);

        if (obj instanceof CompteurEau) {
            CompteurEau c = (CompteurEau) obj;
            holder.tvNumero.setText("Eau - " + c.getNumero());
            holder.tvInfos.setText("Diamètre: " + c.getDiametre() + " | Posé le: " + formatDate(c.getDatePose()));
        } else if (obj instanceof CompteurElectricite) {
            CompteurElectricite c = (CompteurElectricite) obj;
            holder.tvNumero.setText("Électricité - " + c.getNumero());
            holder.tvInfos.setText("Calibre: " + c.getCalibre() + " | Posé le: " + formatDate(c.getDatePose()));
        }
    }

    @Override
    public int getItemCount() {
        return compteurs.size();
    }

    public void updateData(List<Object> newList) {
        this.compteurs = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumero, tvInfos;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumero = itemView.findViewById(R.id.tvNumero);
            tvInfos = itemView.findViewById(R.id.tvInfos);
        }
    }

    private String formatDate(java.util.Date date) {
        if (date == null) return "";
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date);
    }
}
