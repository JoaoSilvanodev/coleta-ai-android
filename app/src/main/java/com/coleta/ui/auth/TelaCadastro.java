package com.coleta.ui.auth;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.coleta.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class TelaCadastro extends AppCompatActivity {

    private EditText et_nome;
    private EditText et_email_cadastro;
    private EditText et_senha_cadastro;
    private EditText et_confirmar_senha;
    private RadioButton rbEmpresa;
    private RadioButton rbColetor;
    private RadioButton rbCidadao;
    private MaterialButton btn_cadastrar_final;
    private MaterialButton btn_voltar_login;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tela_cadastro);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        rbEmpresa = findViewById(R.id.rb_empresa);
        rbColetor = findViewById(R.id.rb_coletor);
        rbCidadao = findViewById(R.id.rb_cidadao);
        et_nome = findViewById(R.id.et_nome);
        et_email_cadastro = findViewById(R.id.et_email_cadastro);
        et_senha_cadastro = findViewById(R.id.et_senha_cadastro);
        et_confirmar_senha = findViewById(R.id.et_confirmar_senha);
        btn_cadastrar_final = findViewById(R.id.btn_cadastrar_final);
        btn_voltar_login = findViewById(R.id.btn_voltar_login);

        rbEmpresa.setOnClickListener(v -> handleRadioButtonClick(rbEmpresa));
        rbColetor.setOnClickListener(v -> handleRadioButtonClick(rbColetor));
        rbCidadao.setOnClickListener(v -> handleRadioButtonClick(rbCidadao));
        btn_cadastrar_final.setOnClickListener(v -> fazerCadastro());
        btn_voltar_login.setOnClickListener(v -> finish());
    }

    private void handleRadioButtonClick(RadioButton clickedButton) {
        rbEmpresa.setChecked(clickedButton.getId() == R.id.rb_empresa);
        rbColetor.setChecked(clickedButton.getId() == R.id.rb_coletor);
        rbCidadao.setChecked(clickedButton.getId() == R.id.rb_cidadao);
    }

    private void fazerCadastro() {
        String nome = et_nome.getText().toString().trim();
        String email = et_email_cadastro.getText().toString().trim();
        String senha = et_senha_cadastro.getText().toString().trim();
        String confirmarSenha = et_confirmar_senha.getText().toString().trim();
        String tipoUser;

        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || confirmarSenha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!senha.equals(confirmarSenha)) {
            Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show();
            return;
        }
        if (senha.length() < 6) {
            Toast.makeText(this, "A senha deve ter no mínimo 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        if (rbEmpresa.isChecked()) {
            tipoUser = "Empresa";
        } else if (rbColetor.isChecked()) {
            tipoUser = "Coletor";
        } else if (rbCidadao.isChecked()) {
            tipoUser = "Cidadão";
        } else {
            Toast.makeText(this, "Selecione um tipo de usuário", Toast.LENGTH_SHORT).show();
            return;
        }

        criarUsuarioFirebase(nome, email, senha, tipoUser);
    }


    private void criarUsuarioFirebase(String nome, String email, String senha, String tipoUser) {

        mAuth.createUserWithEmailAndPassword(email, senha).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();


                Map<String, Object> userData = new HashMap<>();
                userData.put("nome", nome);
                userData.put("email", email);
                userData.put("tipoUser", tipoUser);

                db.collection("Users").document(userId).set(userData).addOnSuccessListener(aVoid -> {
                    Toast.makeText(TelaCadastro.this, "Cadastro bem-sucedido!", Toast.LENGTH_LONG).show();
                    finish();
                }).addOnFailureListener(e -> {
                    Toast.makeText(TelaCadastro.this, "Erro ao cadastrar perfil: " + e.getMessage(), Toast.LENGTH_LONG).show();

                });
            } else {
                String erro = Objects.requireNonNull(task.getException()).getMessage();
                Toast.makeText(TelaCadastro.this, "Erro ao cadastrar: " + erro, Toast.LENGTH_LONG).show();
            }
        });
    }
}