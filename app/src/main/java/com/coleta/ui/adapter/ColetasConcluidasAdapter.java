package com.coleta.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.coleta.R;
import com.coleta.model.ColetaConcluida;

import java.util.List;

public class ColetasConcluidasAdapter extends RecyclerView.Adapter<ColetasConcluidasAdapter.MyViewHolder> {

    private final List<ColetaConcluida> listaColetas;

    public ColetasConcluidasAdapter(List<ColetaConcluida> lista) {
        this.listaColetas = lista;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_detalhes_deposito, parent, false);
        return new MyViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ColetaConcluida coleta = listaColetas.get(position);
        holder.txtMaterial.setText(coleta.getMaterialResumo());
        holder.txtData.setText((int) coleta.getDataColetadaTimestamp());
    }

    @Override
    public int getItemCount() {
        return listaColetas.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView txtMaterial, txtData;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMaterial = itemView.findViewById(R.id.tv_detalhes_titulo);
            txtData = itemView.findViewById(R.id.tv_detalhes_data_hora);
        }
    }
}
