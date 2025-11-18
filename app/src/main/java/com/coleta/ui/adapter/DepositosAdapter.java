package com.coleta.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.coleta.R;
import com.coleta.model.Depositos;
import com.google.android.material.button.MaterialButton;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DepositosAdapter extends RecyclerView.Adapter<DepositosAdapter.DepositoViewHolder> {

    private final List<Depositos> listaDepositos;
    private final OnDepositoClickListener listener;
    private final boolean mostrarBotaoAcao;


    public DepositosAdapter(List<Depositos> listaDepositos, OnDepositoClickListener listener, boolean mostrarBotaoAcao) {
        this.listaDepositos = listaDepositos;
        this.listener = listener;
        this.mostrarBotaoAcao = mostrarBotaoAcao;
    }

    @NonNull
    @Override
    public DepositoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_deposito_card, parent, false);
        return new DepositoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DepositoViewHolder holder, int position) {
        Depositos deposito = listaDepositos.get(position);
        holder.bind(deposito, listener, mostrarBotaoAcao);


    }

    @Override
    public int getItemCount() {
        return listaDepositos.size();
    }

    public static class DepositoViewHolder extends RecyclerView.ViewHolder {

        TextView tvMaterial;
        TextView tvData;
        TextView tvStatus;
        MaterialButton btnAcao;

        public DepositoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMaterial = itemView.findViewById(R.id.tv_card_material);
            tvData = itemView.findViewById(R.id.tv_card_data);
            tvStatus = itemView.findViewById(R.id.tv_card_status);
            btnAcao = itemView.findViewById(R.id.btn_card_acao);
        }

        public void bind(Depositos deposito, OnDepositoClickListener listener, boolean mostrarBotaoAcao) {

            String titulo = deposito.getMaterial() + " - " + deposito.getQuantidade();
            tvMaterial.setText(titulo);


            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM 'às' HH:mm", Locale.getDefault());
            String dataFormatada = sdf.format(new Date(deposito.getTimestamp()));
            tvData.setText("Criado em: " + dataFormatada);

            if (deposito.isColetado()) {
                tvStatus.setText("COLETADO");
                tvStatus.setTextColor(Color.parseColor("#4CAF50"));
                tvStatus.setBackgroundResource(R.drawable.fundo_status);
                btnAcao.setVisibility(View.GONE);
            } else {

                tvStatus.setText("DISPONÍVEL");
                tvStatus.setTextColor(Color.parseColor("#FF9800"));
                tvStatus.setBackgroundResource(R.drawable.fundo_status);


                if (listener != null && mostrarBotaoAcao) {
                    btnAcao.setText(R.string.aceitar_coleta_btn);
                    btnAcao.setVisibility(View.VISIBLE);

                    btnAcao.setOnClickListener(v -> listener.onAceiteColetaClick(deposito));
                } else {
                    btnAcao.setVisibility(View.GONE);
                }
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(deposito);
                }
            });
        }
    }
}