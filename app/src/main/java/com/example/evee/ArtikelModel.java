package com.example.evee;

import java.io.Serializable;

public class ArtikelModel implements Serializable {
    private String judul;
    private String hariUpload;
    private int gambar;

    public ArtikelModel(String judul, String hariUpload, int gambar) {
        this.judul = judul;
        this.hariUpload = hariUpload;
        this.gambar = gambar;
    }

    public String getJudul() { return judul; }
    public String getHariUpload() { return hariUpload; }
    public int getGambar() { return gambar; }
}
