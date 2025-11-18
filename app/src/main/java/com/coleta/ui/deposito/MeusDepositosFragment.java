package com.coleta.ui.deposito;

import android.content.Intent;
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
import com.coleta.ui.adapter.OnDepositoClickListener;
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

public class MeusDepositosFragment extends Fragment implements OnDepositoClickListener {

    private RecyclerView recyclerDepositos;
    private DepositosAdapter adapter;
    private List<Depositos> listaDepositos;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ProgressBar progressBar;


    public MeusDepositosFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_meus_depositos, container, false);


        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        recyclerDepositos = view.findViewById(R.id.recycler_meus_depositos);
        progressBar = view.findViewById(R.id.progress_bar_lista);

        listaDepositos = new ArrayList<>();
        adapter = new DepositosAdapter(listaDepositos, this,false);
        recyclerDepositos.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        carregarMeusDepositos();
    }


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
                            Depositos deposito = document.toObject(Depositos.class);

                            deposito.setDocumentId(document.getId());

                            listaDepositos.add(deposito);
                        }

                        if(listaDepositos.isEmpty()){
                            Toast.makeText(getContext(), "Você ainda não tem depósitos criados.", Toast.LENGTH_LONG).show();
                        }

                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "Falha ao carregar seus depósitos: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void onAceiteColetaClick(Depositos deposito) {}

    @Override
    public void onItemClick(Depositos deposito) {
        Intent intent = new Intent(getContext(), TelaDetalhesDeposito.class);
        intent.putExtra("DEPOSITO_OBJ", deposito);
        startActivity(intent);
    }
}