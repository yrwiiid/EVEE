package com.example.evee;

import java.io.Serializable;

public class ArtikelModel implements Serializable {
    private String judul;
    private String tanggal;
    private String gambarUrl;
    private String link;

    public ArtikelModel(String judul, String tanggal, String gambarUrl, String link) {
        this.judul = judul;
        this.tanggal = tanggal;
        this.gambarUrl = gambarUrl;
        this.link = link;
    }

    public String getJudul() { return judul; }
    public String getTanggal() { return tanggal; }
    public String getGambarUrl() { return gambarUrl; }
    public String getLink() { return link; }
}
