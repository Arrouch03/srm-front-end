package ma.srm.srm.frontend;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import ma.srm.srm.frontend.ui.AddCompteurEauActivity;
import ma.srm.srm.frontend.ui.AddCompteurElectriciteActivity;
import ma.srm.srm.frontend.ui.LoginActivity;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // ✅ On crée le bouton hamburger
        toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.open,
                R.string.close
        ) {
            // 👉 Surcharge du clic sur le hamburger
            @Override
            public void onDrawerOpened(@NonNull android.view.View drawerView) {
                // On bloque l’ouverture du drawer
                drawerLayout.closeDrawers();
                // On affiche directement le choix du compteur
                showCompteurChoiceDialog();
            }
        };

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Gestion menu du drawer (logout, etc.)
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_logout) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }

            drawerLayout.closeDrawers();
            return true;
        });
    }

    // ✅ Dialogue choix compteur
    private void showCompteurChoiceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choisir type de compteur")
                .setItems(new CharSequence[]{"Eau", "Électricité"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent;
                        if (which == 0) {
                            intent = new Intent(MainActivity.this, AddCompteurEauActivity.class);
                            intent.putExtra("TYPE_ID", 1);
                        } else {
                            intent = new Intent(MainActivity.this, AddCompteurElectriciteActivity.class);
                            intent.putExtra("TYPE_ID", 2);
                        }
                        startActivity(intent);
                    }
                });
        builder.show();
    }
}
