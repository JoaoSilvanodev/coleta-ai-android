package com.coleta.ui.deposito;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.coleta.R;

public class TelaFoto extends AppCompatActivity {

    private ImageView imgFullScreen;
    private ImageButton btnFechar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foto_full_screen);

        imgFullScreen = findViewById(R.id.img_full_screen);
        btnFechar = findViewById(R.id.btn_fechar_foto);


        String fotoUrl = getIntent().getStringExtra("FOTO_URL");

        if (fotoUrl != null && !fotoUrl.isEmpty()) {

            Glide.with(this)
                    .load(fotoUrl)
                    .placeholder(R.color.gray_medium) // Um placeholder
                    .into(imgFullScreen);
        } else {
            Toast.makeText(this, "Erro: URL da imagem não encontrada.", Toast.LENGTH_LONG).show();
            finish();
        }

        btnFechar.setOnClickListener(v -> finish());
    }
}