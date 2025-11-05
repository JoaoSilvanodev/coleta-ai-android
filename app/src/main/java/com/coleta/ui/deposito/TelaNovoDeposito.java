package com.coleta.ui.deposito;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.PackageManagerCompat;

import com.coleta.R;
import com.coleta.model.Depositos;
import com.google.android.gms.internal.location.zzau;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.HashMap;
import java.util.Map;

public class TelaNovoDeposito extends AppCompatActivity {

    private TextInputEditText etMaterial;
    private TextInputEditText etQuantidade;
    private TextInputEditText etDetalhes;
    private Button btnSalvarDeposito;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private FusedLocationProviderClient fusedLocationClient;
    private Location ultimaLocalizacao;

    private ActivityResultLauncher<String[]> locationPermissionRequest;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tela_novo_deposito);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etMaterial = findViewById(R.id.etMaterial);
        etQuantidade = findViewById(R.id.etQuantidade);
        etDetalhes = findViewById(R.id.etDetalhes);
        btnSalvarDeposito = findViewById(R.id.btnSalvarDeposito);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        configurarLauncherPermissao();

        btnSalvarDeposito.setOnClickListener(v -> prepararSalvamento());

    }

    private void configurarLauncherPermissao() {
        locationPermissionRequest = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                    Boolean fineLocactionGranted = result.getOrDefault(
                            Manifest.permission.ACCESS_FINE_LOCATION, false);

                    if (fineLocactionGranted != null && fineLocactionGranted) {
                        oterLocalizacaoAtual();
                    } else {
                        Toast.makeText(this, "Permissão de Localização negada. O depósito será salvo sem GPS.", Toast.LENGTH_LONG).show();
                        salvarDeposito(0.0, 0.0);
                    }
        });
    }

    private void prepararSalvamento() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            oterLocalizacaoAtual();
        } else {
            locationPermissionRequest.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void oterLocalizacaoAtual() {
        try {

            fusedLocationClient.getLastLocation().addOnSuccessListener(this, (Location location) -> {
                if (location != null) {
                    ultimaLocalizacao = location;
                    Toast.makeText(this, "Localização Obtida", Toast.LENGTH_SHORT).show();
                    salvarDeposito(location.getLatitude(), location.getLongitude());
                } else {
                    Toast.makeText(this, "Não foi possível obter o GPS", Toast.LENGTH_SHORT).show();
                    salvarDeposito(0.0,0.0);
                }
            });

        } catch (SecurityException e) {
            Toast.makeText(this, "Erro de segurança ao acessar GPS.", Toast.LENGTH_LONG).show();
            salvarDeposito(0.0, 0.0);
        }
    }


    private void salvarDeposito(double lat, double lng) {

        String material = etMaterial.getText().toString().trim();
        String quantidade = etQuantidade.getText().toString().trim();
        String detalhes = etDetalhes.getText().toString().trim();

        FirebaseUser user = mAuth.getCurrentUser();


        if (user == null) {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show();
            return;
        }
        if (material.isEmpty() || quantidade.isEmpty() ) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        Depositos novoDeposito = new Depositos(material, quantidade, detalhes, user.getUid(), lat, lng);

        salvarDepositoFirebase(novoDeposito);

    }



    private void salvarDepositoFirebase(Depositos novoDeposito) {

        Map<String, Object> depositoData = new HashMap<>();
        depositoData.put("userId", novoDeposito.getUserId());
        depositoData.put("material", novoDeposito.getMaterial());
        depositoData.put("quantidade", novoDeposito.getQuantidade());
        depositoData.put("detalhes", novoDeposito.getDetalhes());
        depositoData.put("timestamp", novoDeposito.getTimestamp());
        depositoData.put("coletado", novoDeposito.isColetado());

        GeoPoint geoPoint = new GeoPoint(novoDeposito.getLatitude(), novoDeposito.getLongitude());
        depositoData.put("localizacao",geoPoint);


        db.collection("depositos")
                .add(depositoData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(TelaNovoDeposito.this, "Depósito salvo com sucesso!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(TelaNovoDeposito.this, "Erro ao salvar depósito" +e.getMessage(), Toast.LENGTH_SHORT).show();
                });

    }

}
