package com.coleta.ui.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.coleta.R;
import com.coleta.model.Depositos;
import com.coleta.ui.deposito.TelaDetalhesDeposito;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class DepositosAdapter extends RecyclerView.Adapter<DepositosAdapter.DepositosViewHolder> {

    private List<Depositos> listaDepositos;
    private final OnDepositoClickListener listener;


    public DepositosAdapter(List<Depositos> listaDepositos, OnDepositoClickListener listener) {
        this.listaDepositos = listaDepositos;
        this.listener = listener;
    }


    @NonNull
    @Override
    public DepositosViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_deposito_card, parent, false);
        return new DepositosViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull DepositosViewHolder holder, int position) {
        Depositos deposito = listaDepositos.get(position);

        String titulo = deposito.getMaterial()+ " (" + deposito.getQuantidade() + ")";

        long timestamp = deposito.getTimestamp();
        String dataFormatada;

        if (timestamp>0) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
            dataFormatada = simpleDateFormat.format(new Date(timestamp));
        } else {
            dataFormatada = "Data indisponivel";
        }

        holder.tvMaterialTitulo.setText(titulo);

        holder.tvData.setText("Data Deposito: " + dataFormatada);
        String localizaccaoStr = String.format("GPS: %.5f, %.5f", deposito.getLatitude(), deposito.getLongitude());
        holder.tvEndereco.setText(localizaccaoStr);

        holder.cardView.setOnClickListener(null);

        holder.btn_opc.setOnClickListener(v -> {
            if (deposito instanceof Serializable) {
                Intent intent = new Intent(v.getContext(), TelaDetalhesDeposito.class);
                intent.putExtra("DEPOSITO_OBJ", (Serializable) deposito);
                v.getContext().startActivity(intent);

            } else {
                Toast.makeText(v.getContext(), "Erro: Objeto de Depósito não é serializável.", Toast.LENGTH_SHORT).show();
            }

        });
    }



    @Override
    public int getItemCount() {
        return listaDepositos.size();
    }

    public static class DepositosViewHolder extends RecyclerView.ViewHolder {
        TextView tvMaterialTitulo;
        TextView tvEndereco;
        TextView tvData;
        ImageView imgMaterialIcon;
        public final View cardView;
        public final ImageView btn_opc;


        public DepositosViewHolder(@NonNull View itemView) {

            super(itemView);

            tvMaterialTitulo = itemView.findViewById(R.id.materrial_tt);
            tvEndereco = itemView.findViewById(R.id.tv_endereco);
            tvData = itemView.findViewById(R.id.tv_data_hora);
            imgMaterialIcon = itemView.findViewById(R.id.img_material);
            cardView = itemView.findViewById(R.id.card_deposito);
            btn_opc = itemView.findViewById(R.id.btn_opc);

        }
    }




}
