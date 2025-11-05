package com.coleta.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.coleta.R;
import com.coleta.ui.coleta.TelaColeta;
import com.coleta.ui.auth.TelaCadastro; // Necessário para a navegação
import com.coleta.ui.deposito.TelaDeposito;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Objects;


public class TelaLogin extends AppCompatActivity {
    private Button btn_cadastrar;
    private EditText et_email;
    private EditText et_senha;
    private Button btn_login;
    private TextView tvEsqueciSenha;


    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tela_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        btn_cadastrar = findViewById(R.id.btn_cadastrar);
        et_email = findViewById(R.id.et_email);
        et_senha = findViewById(R.id.et_senha);
        btn_login = findViewById(R.id.btn_login);
        tvEsqueciSenha = findViewById(R.id.tv_esqueci_senha);

        btn_login.setOnClickListener(v -> fazerLogin());
        btn_cadastrar.setOnClickListener(v -> irParaTelaCadastro());

        if (mAuth.getCurrentUser() != null) {
            redirecionarUser(mAuth.getCurrentUser());
        }

    }

    private void irParaTelaCadastro() {
        Intent intent = new Intent(this, TelaCadastro.class);
        startActivity(intent);

    }

    private void fazerLogin() {
        String email = et_email.getText().toString().trim();
        String senha = et_senha.getText().toString().trim();


        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha o e-mail e a senha", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, senha).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                Toast.makeText(TelaLogin.this, "Login bem-sucedido", Toast.LENGTH_SHORT).show();
                redirecionarUser(user);
            } else {
                try {
                    throw Objects.requireNonNull(task.getException());
                } catch (FirebaseAuthInvalidCredentialsException e) {
                    Toast.makeText(TelaLogin.this, "E-mail ou senha incorretos", Toast.LENGTH_LONG).show();

                } catch (Exception e) {
                    String erro = e.getMessage();
                    Toast.makeText(TelaLogin.this, "Erro ao fazer login: " + erro, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void redirecionarUser(FirebaseUser user) {
        String userId = user.getUid();

        db.collection("Users").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();

                if (document != null && document.exists()) {

                    String tipoUsuario = document.getString("tipoUser");
                    Intent intent;


                    if ("Coletor".equalsIgnoreCase(tipoUsuario)) {
                        intent = new Intent(TelaLogin.this, TelaColeta.class);
                    } else if ("Empresa".equalsIgnoreCase(tipoUsuario)) {
                        intent = new Intent(TelaLogin.this, TelaDeposito.class);
                    }else if ("Cidadão".equalsIgnoreCase(tipoUsuario)) {
                        intent = new Intent(TelaLogin.this, TelaDeposito.class);
                    } else {
                        mAuth.signOut();
                        Toast.makeText(TelaLogin.this, "Tipo de usuário não suportado", Toast.LENGTH_SHORT).show();
                        return;
                    }


                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();

                } else {
                    Toast.makeText(TelaLogin.this, "Erro ao obter o tipo de usuário", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(TelaLogin.this, "Erro ao buscar dados do usuário", Toast.LENGTH_SHORT).show();
            }

        });
    }
}