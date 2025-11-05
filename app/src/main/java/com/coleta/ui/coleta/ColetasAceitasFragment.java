package com.coleta.ui.coleta;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ColetasAceitasFragment extends Fragment implements OnDepositoClickListener {

    private RecyclerView recyclerView;
    private DepositosAdapter adapter;
    private List<Depositos> listaDepositos;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public  ColetasAceitasFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_coletas_aceitas, container, false);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.recycler_coletas_aceitas);
        listaDepositos = new ArrayList<>();
        adapter = new DepositosAdapter(listaDepositos, this);
        recyclerView.setAdapter(adapter);
        return view;

    }

    @Override
    public void onResume() {
        super.onResume();
        buscarColetasAceitas();
    }

    private void buscarColetasAceitas() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String meuCatadorId = Objects.requireNonNull(user).getUid();
        listaDepositos.clear();

        // BUSCA SIMPLIFICADA: Na coleção DEPOSITOS
        db.collection("depositos")
                .whereEqualTo("coletado", true) // Filtro 1: Apenas os coletados
                .whereEqualTo("catadorId", meuCatadorId) // Filtro 2: Apenas os coletados por MIM
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Mapeia o documento original
                            Depositos deposito = document.toObject(Depositos.class);
                            deposito.setDocumentId(document.getId());
                            listaDepositos.add(deposito);
                        }

                        if (listaDepositos.isEmpty()) {
                            Toast.makeText(getContext(), "Nenhum histórico de coleta encontrado.", Toast.LENGTH_SHORT).show();
                        }

                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "Erro ao carregar histórico: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

        @Override
        public void onAceiteColetaClick(Depositos deposito) {
            Toast.makeText(getContext(), "Coleta já finalizada. Detalhes: " + deposito.getMaterial(), Toast.LENGTH_SHORT).show();
        }
    }


