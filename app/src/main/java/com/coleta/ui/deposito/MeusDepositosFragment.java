package com.coleta.ui.deposito;

import android.os.Bundle;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MeusDepositosFragment extends Fragment {

    private RecyclerView recyclerDepositos;
    private DepositosAdapter adapter;
    private List<Depositos> listaDepositos;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ProgressBar progressBar;

    // Construtor vazio (obrigatório)
    public MeusDepositosFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // 1. Infla o layout que contém a lista
        View view = inflater.inflate(R.layout.fragment_meus_depositos, container, false);

        // 2. Inicializa Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 3. Inicializa UI
        recyclerDepositos = view.findViewById(R.id.recycler_meus_depositos);
        progressBar = view.findViewById(R.id.progress_bar_lista);

        listaDepositos = new ArrayList<>();
        // Passando null para o listener, pois o Depositante apenas VÊ seus depósitos, não clica (ainda)
        adapter = new DepositosAdapter(listaDepositos, null);
        recyclerDepositos.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 4. Carrega os dados quando o Fragmento fica visível
        carregarMeusDepositos();
    }

    // [A LÓGICA DA LISTA ESTÁ AQUI AGORA]
    private void carregarMeusDepositos() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Erro! Faça login novamente.", Toast.LENGTH_LONG).show();
            return;
        }

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        listaDepositos.clear();

        db.collection("depositos")
                .whereEqualTo("userId", user.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Converte o documento do Firebase para o objeto Depositos
                            Depositos deposito = document.toObject(Depositos.class);

                            // Salva o ID do documento (ESSENCIAL para futuras ações)
                            deposito.setDocumentId(document.getId());

                            listaDepositos.add(deposito);
                        }

                        if(listaDepositos.isEmpty()){
                            Toast.makeText(getContext(), "Você ainda não tem depósitos criados.", Toast.LENGTH_LONG).show();
                        }

                        adapter.notifyDataSetChanged();
                    } else {
                        // O erro do Índice deve estar resolvido, mas mantemos o Toast de falha
                        Toast.makeText(getContext(), "Falha ao carregar seus depósitos: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}