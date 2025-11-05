package com.coleta.model;

import java.io.Serializable;

public class ColetaConcluida implements Serializable {

    private String depositoId;
    private String catadorId;
    private long dataColetadaTimestamp;
    private String materialResumo;

    public ColetaConcluida() {}

    public ColetaConcluida(String depositoId, String catadorId, long dataColetadaTimestamp, String materialResumo) {
        this.depositoId = depositoId;
        this.catadorId = catadorId;
        this.dataColetadaTimestamp = dataColetadaTimestamp;
        this.materialResumo = materialResumo;
    }

    public ColetaConcluida(String depositoId, String catadorId, String materialResumo) {
        this.depositoId = depositoId;
        this.catadorId = catadorId;
        this.materialResumo = materialResumo;
        this.dataColetadaTimestamp = System.currentTimeMillis();
    }



    public String getDepositoId() {
        return depositoId;
    }

    public void setDepositoId(String depositoId) {
        this.depositoId = depositoId;
    }

    public String getCatadorId() {
        return catadorId;
    }

    public void setCatadorId(String catadorId) {
        this.catadorId = catadorId;
    }

    public long getDataColetadaTimestamp() {
        return dataColetadaTimestamp;
    }

    public void setDataColetadaTimestamp(long dataColetadaTimestamp) {
        this.dataColetadaTimestamp = dataColetadaTimestamp;
    }

    public String getMaterialResumo() {
        return materialResumo;
    }

    public void setMaterialResumo(String materialResumo) {
        this.materialResumo = materialResumo;
    }
}
