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

    // Construtor vazio (obrigatório)
    public PerfilFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Infla o layout
        return inflater.inflate(R.layout.fragment_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicialização do Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Inicialização da UI (usando 'view' do fragmento)
        tvNome = view.findViewById(R.id.tv_perfil_nome);
        tvEmail = view.findViewById(R.id.tv_perfil_email);
        btnLogout = view.findViewById(R.id.btn_logout);

        // Configurar o botão de Logout
        btnLogout.setOnClickListener(v -> realizarLogout());
    }

    @Override
    public void onStart() {
        super.onStart();
        // Carregar os dados do usuário
        carregarDadosUsuario();
    }

    private void carregarDadosUsuario() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // E-mail (do Auth)
            tvEmail.setText(user.getEmail());

            // Nome (do Firestore)
            String userId = user.getUid();
            // Usa a coleção "Users" (com 'U' maiúsculo) como definido no cadastro
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
        // Usa getContext() para o Toast no Fragment
        Toast.makeText(getContext(), "Logout realizado.", Toast.LENGTH_SHORT).show();

        // Usa getActivity() para iniciar a intent
        Intent intent = new Intent(getActivity(), TelaLogin.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // Fecha a Activity hospedeira (TelaDeposito)
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}