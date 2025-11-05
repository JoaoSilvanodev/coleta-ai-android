package com.coleta.model;

import com.google.firebase.firestore.Exclude;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Depositos implements Serializable {
    private String material;
    private String quantidade;
    private String detalhes;
    private String userId;
    private long timestamp;
    private boolean coletado;
    private double latitude;
    private double longitude;
    private String documentId;
    private List<String> fotosUrls;
    private String catadorId;

    public Depositos() {
        this.fotosUrls = new ArrayList<>();
    }

    public Depositos(String material, String quantidade, String detalhes, String userId, double latitude, double longitude) {
        this.material = material;
        this.quantidade = quantidade;
        this.detalhes = detalhes;
        this.userId = userId;
        this.timestamp = System.currentTimeMillis();
        this.coletado = false;
        this.latitude = latitude;
        this.longitude = longitude;
        this.fotosUrls = new ArrayList<>();
        this.catadorId = null;
    }

    public Depositos(String material, String quantidade, String detalhes, String userId, long timestamp, double latitude, double longitude, boolean coletado) {
        this.material = material;
        this.quantidade = quantidade;
        this.detalhes = detalhes;
        this.userId = userId;
        this.timestamp = timestamp;
        this.coletado = coletado;
        this.latitude = latitude;
        this.longitude = longitude;
        this.fotosUrls = new ArrayList<>();
    }

    public Depositos(String material, String quantidade, String detalhes, String userId) {
        this.material = material;
        this.quantidade = quantidade;
        this.detalhes = detalhes;
        this.userId = userId;
        this.timestamp = 0;
        this.fotosUrls = new ArrayList<>();
    }

    @Exclude
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getMaterial() {
        return material;
    }
    public void setMaterial(String material) {
        this.material = material;
    }

    public String getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(String quantidade) {
        this.quantidade = quantidade;
    }

    public String getDetalhes() {
        return detalhes;
    }

    public void setDetalhes(String detalhes) {
        this.detalhes = detalhes;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isColetado() {
        return coletado;
    }

    public void setColetado(boolean coletado) {
        this.coletado = coletado;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public List<String> getFotosUrls() {
        return fotosUrls;
    }

    public void setFotosUrls(List<String> fotosUrls) {
        this.fotosUrls = fotosUrls;
    }

    public String getCatadorId() {
        return catadorId;
    }

    public void setCatadorId(String catadorId) {
        this.catadorId = catadorId;
    }
}
