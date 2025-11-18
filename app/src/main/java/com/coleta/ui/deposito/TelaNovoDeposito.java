package com.coleta.ui.deposito;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.coleta.R;
import com.coleta.model.Depositos;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TelaNovoDeposito extends AppCompatActivity {

    private TextInputEditText etMaterial;
    private TextInputEditText etQuantidade;
    private TextInputEditText etDetalhes;
    private MaterialButton btnSalvarDeposito;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<String[]> locationPermissionRequest;
    private MaterialButton btnAdicionarFoto;
    private Uri imagemUri;
    private ActivityResultLauncher<String> galleryLauncher;
    private FirebaseStorage storage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tela_novo_deposito);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        etMaterial = findViewById(R.id.etMaterial);
        etQuantidade = findViewById(R.id.etQuantidade);
        etDetalhes = findViewById(R.id.etDetalhes);
        btnSalvarDeposito = findViewById(R.id.btnSalvarDeposito);
        btnAdicionarFoto = findViewById(R.id.btn_adicionar_foto);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        configurarLauncherPermissao();
        configurarGaleriaLauncher();

        btnSalvarDeposito.setOnClickListener(v -> prepararSalvamento());
        btnAdicionarFoto.setOnClickListener(v -> abrirGaleria());
    }

    private void configurarLauncherPermissao() {
        locationPermissionRequest = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                    Boolean fineLocactionGranted = result.getOrDefault(
                            Manifest.permission.ACCESS_FINE_LOCATION, false);

                    if (fineLocactionGranted != null && fineLocactionGranted) {
                        obterLocalizacaoAtual();
                    } else {
                        Toast.makeText(this, "Permissão de Localização negada. Não é possível criar o depósito.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void configurarGaleriaLauncher() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imagemUri = uri;
                        Toast.makeText(this, "Foto selecionada!", Toast.LENGTH_SHORT).show();
                        btnAdicionarFoto.setText("FOTO SELECIONADA (1)");
                    }
                });
    }

    private void abrirGaleria() {
        galleryLauncher.launch("image/*");
    }


    private void prepararSalvamento() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            obterLocalizacaoAtual();
        } else {
            locationPermissionRequest.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void obterLocalizacaoAtual() {
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, (Location location) -> {
                if (location != null) {
                    Toast.makeText(this, "Localização Obtida: OK", Toast.LENGTH_SHORT).show();
                    salvarDeposito(location.getLatitude(), location.getLongitude());
                } else {
                    Toast.makeText(this, "ERRO: Localização não obtida. Tente novamente ou verifique o GPS.", Toast.LENGTH_LONG).show();
                }
            });
        } catch (SecurityException e) {
            Toast.makeText(this, "Erro de segurança ao acessar GPS.", Toast.LENGTH_LONG).show();
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
            Toast.makeText(this, "Preencha todos os campos obrigatórios", Toast.LENGTH_SHORT).show();
            return;
        }

        Depositos novoDeposito = new Depositos(material, quantidade, detalhes, user.getUid(), lat, lng);

        if (imagemUri != null) {
            Toast.makeText(this, "Iniciando upload da foto...", Toast.LENGTH_SHORT).show();
            uploadImagem(novoDeposito);
        } else {
            salvarDepositoFirebase(novoDeposito);
        }
    }


    private void uploadImagem(Depositos novoDeposito) {
        String nomeArquivo = UUID.randomUUID().toString() + ".jpg";
        StorageReference storageRef = storage.getReference()
                .child("depositos_fotos/" + novoDeposito.getUserId() + "/" + nomeArquivo);

        storageRef.putFile(imagemUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String urlFoto = uri.toString();
                        List<String> fotosUrls = new ArrayList<>();
                        fotosUrls.add(urlFoto);
                        novoDeposito.setFotosUrls(fotosUrls);

                        salvarDepositoFirebase(novoDeposito);
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(TelaNovoDeposito.this, "Falha no upload da foto: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }


    private void salvarDepositoFirebase(Depositos novoDeposito) {
        Map<String, Object> depositoData = new HashMap<>();
        depositoData.put("userId", novoDeposito.getUserId());
        depositoData.put("material", novoDeposito.getMaterial());
        depositoData.put("quantidade", novoDeposito.getQuantidade());
        depositoData.put("detalhes", novoDeposito.getDetalhes());
        depositoData.put("timestamp", novoDeposito.getTimestamp());
        depositoData.put("coletado", novoDeposito.isColetado());
        depositoData.put("latitude", novoDeposito.getLatitude());
        depositoData.put("longitude", novoDeposito.getLongitude());
        depositoData.put("fotosUrls", novoDeposito.getFotosUrls());

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