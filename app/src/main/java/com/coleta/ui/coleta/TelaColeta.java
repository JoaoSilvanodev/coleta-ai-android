package com.coleta.ui.coleta;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.coleta.R;
import com.coleta.ui.perfil.PerfilFragment;
import com.coleta.ui.coleta.MensagensFragment;
import com.coleta.ui.coleta.NotificacoesFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class TelaColeta extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tela_coleta);

        bottomNavigationView = findViewById(R.id.bottom_navigation_view_coleta);


        if (savedInstanceState == null) {
            loadFragment(new ListaColetasFragment());
        }

        bottomNavigationView.setSelectedItemId(R.id.nav_lista_coletas);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.nav_lista_coletas) {
                    selectedFragment = new ListaColetasFragment();
                } else if (itemId == R.id.nav_coletas_aceitas) {
                    selectedFragment = new ColetasAceitasFragment();
                } else if (itemId == R.id.nav_notifications) {
                    selectedFragment = new NotificacoesFragment();
                } else if (itemId == R.id.nav_perfil_coleta) {
                    selectedFragment = new PerfilFragment();
                }

                if (selectedFragment != null) {
                    loadFragment(selectedFragment);
                }
                return true;
            }
        });
    }


    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container_coleta, fragment);
        transaction.commit();
    }
}