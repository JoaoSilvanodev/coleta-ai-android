package com.coleta.ui.coleta;

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
import com.coleta.model.ColetaConcluida;
import com.coleta.model.Depositos;
import com.coleta.ui.adapter.DepositosAdapter;
import com.coleta.ui.adapter.OnDepositoClickListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class ListaColetasFragment extends Fragment implements OnDepositoClickListener {

    private RecyclerView recyclerDepositos;
    private ProgressBar progressBar;
    private DepositosAdapter adapter;
    private List<Depositos> listaDepositos;
    private List<ColetaConcluida> listaColetas;
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
        progressBar = view.findViewById(R.id.barra_progresso_coleta);

        listaDepositos = new ArrayList<>();
        listaColetas = new ArrayList<>();
        adapter = new DepositosAdapter(listaDepositos, this); // 'this' (o Fragment) é o listener
        recyclerDepositos.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        buscarDepositosDisponiveis();
        buscarColetasAceitas();
    }

    // Método da Interface (clique no Card)
    @Override
    public void onAceiteColetaClick(Depositos deposito) {
        Toast.makeText(getContext(), "Aceitando coleta de " + deposito.getMaterial() + " Aguarde...", Toast.LENGTH_SHORT).show();
        marcarColetado(deposito);
    }

    private void marcarColetado(Depositos deposito) {
        // 1. Inicia o carregamento
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        String documentId = deposito.getDocumentId();
        // Obtém o ID do Catador logado
        String catadorId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        if (documentId == null || documentId.isEmpty()) {
            Toast.makeText(getContext(), "ID do depósito não encontrado", Toast.LENGTH_SHORT).show();
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            return;
        }

        // 2. ATUALIZAÇÃO SIMPLES: Atualiza o status e salva o ID do Catador no documento original
        db.collection("depositos").document(documentId)
                .update("coletado", true, "catadorId", catadorId)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Coleta Finalizada e Histórico Salvo!", Toast.LENGTH_LONG).show();

                    // Recarrega a lista de DISPONÍVEIS para remover o item
                    buscarDepositosDisponiveis();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Falha na atualização: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                });
    }

    private void buscarColetasAceitas() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        listaColetas.clear();

        String catadorId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        db.collection("coletas_concluidas")
                .whereEqualTo("catadorId", catadorId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ColetaConcluida coleta = document.toObject(ColetaConcluida.class);
                            listaColetas.add(coleta);
                        }

                        if (listaColetas.isEmpty()) {
                            Toast.makeText(getContext(), "Nenhuma coleta aceita ainda", Toast.LENGTH_SHORT).show();
                        }

                        adapter.notifyDataSetChanged();
                    } else {
                        Log.e("Firebase", "Erro ao buscar coletas aceitas: " + Objects.requireNonNull(task.getException()).getMessage());
                    }
                });
    }



    // Lógica de Busca no Firestore
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