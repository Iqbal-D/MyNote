package com.example.mynote.entiti;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "nyatat")
public class Note implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "judul")
    private String judul;

    @ColumnInfo(name = "catatan")
    private String catatan;

    @ColumnInfo(name = "waktu")
    private String waktu;

    @ColumnInfo(name = "image")
    private String image;

    @ColumnInfo(name = "link")
    private String link;

//    @ColumnInfo(name = "align")
//    private String align;
//
//    public String getAlign() {
//        return align;
//    }
//
//    public void setAlign(String align) {
//        this.align = align;
//    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getJudul() {
        return judul;
    }

    public void setJudul(String judul) {
        this.judul = judul;
    }

    public String getCatatan() {
        return catatan;
    }

    public void setCatatan(String catatan) {
        this.catatan = catatan;
    }

    public String getWaktu() {
        return waktu;
    }

    public void setWaktu(String waktu) {
        this.waktu = waktu;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @NonNull
    @Override
    public String toString() {
        return judul + " " + waktu + " " + image + " " + catatan ;
    }
}
