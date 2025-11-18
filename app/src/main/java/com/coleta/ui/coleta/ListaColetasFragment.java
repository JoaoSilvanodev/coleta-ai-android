package com.coleta.ui.coleta;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.coleta.R;
import com.coleta.model.Depositos;
import com.coleta.ui.adapter.DepositosAdapter;
import com.coleta.ui.adapter.OnDepositoClickListener;
import com.coleta.ui.deposito.TelaDetalhesDeposito;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;



public class ListaColetasFragment extends Fragment implements OnDepositoClickListener {

    private RecyclerView recyclerDepositos;
    private ProgressBar progressBar;
    private DepositosAdapter adapter;
    private List<Depositos> listaDepositos;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;


    public ListaColetasFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lista_coletas, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        recyclerDepositos = view.findViewById(R.id.recycler_depositos_disponiveis);


        listaDepositos = new ArrayList<>();
        adapter = new DepositosAdapter(listaDepositos, this,true);
        recyclerDepositos.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        buscarDepositosDisponiveis();

    }

    @Override
    public void onAceiteColetaClick(Depositos deposito) {
        Toast.makeText(getContext(), "Aceitando coleta de " + deposito.getMaterial() + " Aguarde...", Toast.LENGTH_SHORT).show();
        marcarColetado(deposito);
    }

    @Override
    public void onItemClick(Depositos deposito) {
        Intent intent = new Intent(getContext(), TelaDetalhesDeposito.class);
        intent.putExtra("DEPOSITO_OBJ", deposito);
        startActivity(intent);
    }

    private void marcarColetado(Depositos deposito) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        String documentId = deposito.getDocumentId();
        String catadorId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        if (documentId == null || documentId.isEmpty()) {
            Toast.makeText(getContext(), "ID do depósito não encontrado", Toast.LENGTH_SHORT).show();
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            return;
        }

        DocumentReference depositoRef = db.collection("depositos").document(documentId);
        DocumentReference novaColetaRef = db.collection("coletas").document();

        WriteBatch batch = db.batch();
        batch.update(depositoRef, "coletado", true, "catadorId", catadorId);

        Map<String, Object> novaColetaData = new HashMap<>();
        novaColetaData.put("catadorId", catadorId);
        novaColetaData.put("depositoId", documentId);
        novaColetaData.put("material", deposito.getMaterial());
        novaColetaData.put("quantidade", deposito.getQuantidade());
        novaColetaData.put("detalhes", deposito.getDetalhes());
        novaColetaData.put("latitude", deposito.getLatitude());
        novaColetaData.put("longitude", deposito.getLongitude());
        novaColetaData.put("userId", deposito.getUserId());
        novaColetaData.put("coletado", true);
        novaColetaData.put("timestamp", deposito.getTimestamp());
        novaColetaData.put("timestampColeta", System.currentTimeMillis());
        batch.set(novaColetaRef, novaColetaData);

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                   Toast.makeText(getContext(), "Coleta aceita com sucesso", Toast.LENGTH_SHORT).show();
                   buscarDepositosDisponiveis();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Falha ao aceitar coleta: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
        });


    }

    private void buscarDepositosDisponiveis() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        listaDepositos.clear();

        db.collection("depositos")
                .whereEqualTo("coletado", false)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Depositos deposito = document.toObject(Depositos.class);
                            deposito.setDocumentId(document.getId());
                            listaDepositos.add(deposito);
                        }

                        if (listaDepositos.isEmpty()) {
                            Toast.makeText(getContext(), "Nenhuma coleta disponível", Toast.LENGTH_SHORT).show();
                        }

                        adapter.notifyDataSetChanged();

                    } else {
                        Log.e("Firebase", "Erro ao buscar coletas: " + Objects.requireNonNull(task.getException()).getMessage());
                        Toast.makeText(getContext(), "Erro ao carregar depositos.", Toast.LENGTH_LONG).show();
                    }
                });
    }
}