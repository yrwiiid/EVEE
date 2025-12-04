package com.example.evee;

import java.io.Serializable;

public class ArtikelModel implements Serializable {
    private String id;
    private String judul;
    private String tanggal;
    private String gambarUrl;
    private String link;

    public ArtikelModel(String id, String judul, String tanggal, String gambarUrl, String link) {
        this.id = id;
        this.judul = judul;
        this.tanggal = tanggal;
        this.gambarUrl = gambarUrl;
        this.link = link;
    }

    public String getId() { return id; }
    public String getJudul() { return judul; }
    public String getTanggal() { return tanggal; }
    public String getGambarUrl() { return gambarUrl; }
    public String getLink() { return link; }
}
