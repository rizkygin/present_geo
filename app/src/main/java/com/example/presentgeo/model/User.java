package com.example.presentgeo.model;

public class User {
    private String username;
    private String nama;
    private String id_pegawai;
    private String avatar;
    private String login_state;
    private String nama_role;
    private String id_peg_jabatan;
    private String uraian_jabatan;
    private String id_unit;
    private String unit;
    private String id_bidang;
    private String bidang;
    private boolean isAdmin;

    public boolean isAdmin() {
        return isAdmin;
    }

    public String getUsername() {
        return username;
    }

    public String getNama() {
        return nama;
    }

    public String getId_pegawai() {
        return id_pegawai;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getLogin_state() {
        return login_state;
    }

    public String getNama_role() {
        return nama_role;
    }

    public String getId_peg_jabatan() {
        return id_peg_jabatan;
    }

    public String getUraian_jabatan() {
        return uraian_jabatan;
    }

    public String getId_unit() {
        return id_unit;
    }

    public String getUnit() {
        return unit;
    }

    public String getId_bidang() {
        return id_bidang;
    }

    public String getBidang() {
        return bidang;
    }
}
