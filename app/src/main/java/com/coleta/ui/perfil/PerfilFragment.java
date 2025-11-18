package com.coleta.ui.perfil;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.coleta.R;
import com.coleta.ui.auth.TelaLogin;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class PerfilFragment extends Fragment {

    private TextView tvNome;
    private TextView tvEmail;
    private Button btnLogout;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;


    public PerfilFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        tvNome = view.findViewById(R.id.tv_perfil_nome);
        tvEmail = view.findViewById(R.id.tv_perfil_email);
        btnLogout = view.findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> realizarLogout());
    }

    @Override
    public void onStart() {
        super.onStart();
        carregarDadosUsuario();
    }

    private void carregarDadosUsuario() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            tvEmail.setText(user.getEmail());
            String userId = user.getUid();
            db.collection("Users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String nome = documentSnapshot.getString("nome");
                            tvNome.setText(nome);
                        } else {
                            tvNome.setText("Nome não encontrado");
                        }
                    })
                    .addOnFailureListener(e -> tvNome.setText("Erro ao buscar nome"));
        } else {
            realizarLogout();
        }
    }

    private void realizarLogout() {
        mAuth.signOut();
        Toast.makeText(getContext(), "Logout realizado.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getActivity(), TelaLogin.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}