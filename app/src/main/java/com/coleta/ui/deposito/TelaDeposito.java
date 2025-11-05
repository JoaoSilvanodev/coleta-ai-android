package com.coleta.ui.deposito;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.coleta.R;
// Remova a importação da TelaPerfil (Activity), pois agora usamos o PerfilFragment
// import com.coleta.ui.perfil.TelaPerfil;
import com.coleta.ui.perfil.PerfilFragment; // Importa o Fragmento de Perfil
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;

// Importe o MeusDepositosFragment que você já criou
import com.coleta.ui.deposito.MeusDepositosFragment;

public class TelaDeposito extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabAddDeposito;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tela_deposito);


        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        fabAddDeposito = findViewById(R.id.fab_add_deposito);



        if (savedInstanceState == null) {
            loadFragment(new MeusDepositosFragment());
        }

        fabAddDeposito.setOnClickListener(v -> {
            Intent intent = new Intent(TelaDeposito.this, TelaNovoDeposito.class);
            startActivity(intent);
        });

        // Configurar a navegação da Bottom Bar
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();


                if (itemId == R.id.nav_meus_depositos) {
                    selectedFragment = new MeusDepositosFragment();
                } else if (itemId == R.id.nav_perfil) {
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
        transaction.replace(R.id.frame_container_deposito, fragment);
        transaction.commit();
    }
}