package com.coleta.ui.deposito;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.coleta.R;
import com.coleta.model.Depositos;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TelaDetalhesDeposito extends AppCompatActivity implements OnMapReadyCallback {
    private Depositos depositoAtual;
    private MaterialButton btnAceite;
    private double depositoLat;
    private double depositoLng;
    private String depositoMaterial;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private LinearLayout layoutGaleriaFotos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_deposito);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        depositoAtual = (Depositos) getIntent().getSerializableExtra("DEPOSITO_OBJ");
        btnAceite = findViewById(R.id.btn_acao_padrao);
        layoutGaleriaFotos = findViewById(R.id.layout_galeria_fotos);

        if (depositoAtual == null) {
            Toast.makeText(this, "Não foi possível carregar os detalhes", Toast.LENGTH_SHORT).show(); 
            finish();
            return;
        }

        depositoLat = depositoAtual.getLatitude();
        depositoLng = depositoAtual.getLongitude();
        depositoMaterial = depositoAtual.getMaterial() + " (" + depositoAtual.getQuantidade() + ")";
        preencherDetalhesUI();
        verificarTipoUsuarioECarregarAcao();
        carregarFotosGaleria();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.maps_container_detalhes);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void verificarTipoUsuarioECarregarAcao() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        db.collection("Users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String tipoUsuario = documentSnapshot.getString("tipoUser");
                    if (tipoUsuario != null) {
                        configurarBotaoAcao(tipoUsuario);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao carregar perfil para ação.", Toast.LENGTH_SHORT).show();
                });
    }

    private void configurarBotaoAcao(String tipoUsuario) {
        if (depositoAtual == null || depositoAtual.isColetado()) {
            btnAceite.setVisibility(View.GONE);
            return;
        }
        if ("coletor".equalsIgnoreCase(tipoUsuario)) {
            btnAceite.setText("ACEITAR COLETA");
            btnAceite.setVisibility(View.VISIBLE);
            btnAceite.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.my_primary));
            btnAceite.setOnClickListener(v -> marcarColetado());
        } else {
            btnAceite.setText("CANCELAR DEPOSITO");
            btnAceite.setVisibility(View.VISIBLE);
            btnAceite.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.holo_red_dark));
            btnAceite.setOnClickListener(v -> iniciarExclusao());
        }
     }


    private void marcarColetado() {
        String documentId = depositoAtual.getDocumentId();

        String catadorId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        if (documentId == null || documentId.isEmpty() || catadorId == null) {
            Toast.makeText(this, "ID do depósito ou usuário não encontrado para atualização.", Toast.LENGTH_LONG).show();
            return;
        }
        DocumentReference depositoRef = db.collection("depositos").document(documentId);
        DocumentReference novaColetaRef = db.collection("coletas").document();
        WriteBatch batch = db.batch();
        batch.update(depositoRef, "coletado", true, "catadorId", catadorId);

        Map<String, Object> novaColetaData = new HashMap<>();
        novaColetaData.put("catadorId", catadorId);
        novaColetaData.put("depositoId", documentId);
        novaColetaData.put("material", depositoAtual.getMaterial());
        novaColetaData.put("quantidade", depositoAtual.getQuantidade());
        novaColetaData.put("detalhes", depositoAtual.getDetalhes());
        novaColetaData.put("latitude", depositoAtual.getLatitude());
        novaColetaData.put("longitude", depositoAtual.getLongitude());
        novaColetaData.put("userId", depositoAtual.getUserId());
        novaColetaData.put("coletado", true);
        novaColetaData.put("timestamp", depositoAtual.getTimestamp());
        novaColetaData.put("timestampColeta", System.currentTimeMillis());

        batch.set(novaColetaRef, novaColetaData);

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(TelaDetalhesDeposito.this, "Coleta Aceita", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(TelaDetalhesDeposito.this, "Falha ao marcar como coletado: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });

    }

    private void iniciarExclusao() {
        String documentId = depositoAtual.getDocumentId();
        if (documentId==null || documentId.isEmpty()) {
            Toast.makeText(this, "Erro: ID do depósito não encontrado para exclusão.", Toast.LENGTH_LONG).show();
            return;
        }
        db.collection("depositos").document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(TelaDetalhesDeposito.this, "Depósito cancelado com sucesso!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(TelaDetalhesDeposito.this, "Falha ao cancelar depósito: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void preencherDetalhesUI() {
        TextView tvTitulo = findViewById(R.id.tv_detalhes_titulo);
        TextView tvData = findViewById(R.id.tv_detalhes_data_hora);
        TextView tvObs = findViewById(R.id.tv_detalhes_obs);

        String tituloCompleto = depositoAtual.getMaterial() + " - " + depositoAtual.getQuantidade();
        tvTitulo.setText(tituloCompleto);
        java.text.SimpleDateFormat data = new java.text.SimpleDateFormat("dd/MM 'àS' HH:mm", java.util.Locale.getDefault());

        String dataFomatada = data.format(new java.util.Date(depositoAtual.getTimestamp()));
        tvData.setText("Criado em: " + dataFomatada);
        tvObs.setText(depositoAtual.getDetalhes());
    }

    private void carregarFotosGaleria() {
        List<String> fotosUrls = depositoAtual.getFotosUrls();

        if (fotosUrls != null && !fotosUrls.isEmpty()) {
            layoutGaleriaFotos.removeAllViews();

            for (String url: fotosUrls) {
                ImageView imageView = new ImageView(this);
                int altura =  (int) (150*getResources().getDisplayMetrics().density);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, altura);
                params.setMargins(0,0,(int) (0 *getResources().getDisplayMetrics().density),0);
                imageView.setLayoutParams(params);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setAdjustViewBounds(true);

                Glide.with(this).load(url).placeholder(R.color.gray_light).into(imageView);


                imageView.setOnClickListener(v ->{
                    Intent intent = new Intent(TelaDetalhesDeposito.this, TelaFoto.class);
                    intent.putExtra("FOTO_URL", url);
                    startActivity(intent);
                });

                layoutGaleriaFotos.addView(imageView);
            }
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        LatLng localizacaoDeposito = new LatLng(depositoLat, depositoLng);
        googleMap.addMarker(new MarkerOptions()
                .position(localizacaoDeposito)
                .title(depositoMaterial));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(localizacaoDeposito, 15));
    }
}