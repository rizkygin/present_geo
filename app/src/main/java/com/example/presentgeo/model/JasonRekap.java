package com.example.presentgeo.model;

public class JasonRekap {
    private boolean code;
    private String message;
    private Rekap data;

    public boolean isCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Rekap getData() {
        return data;
    }

    public class Rekap {
        private String id_pegawai ;
        private String nama_pegawai;
        private String nip;
        private String masuk;
        private String pulang;
        private String keter;

        public String getId_pegawai() {
            return id_pegawai;
        }

        public String getNama_pegawai() {
            return nama_pegawai;
        }

        public String getNip() {
            return nip;
        }

        public String getMasuk() {
            return masuk;
        }

        public String getPulang() {
            return pulang;
        }

        public String getKeter() {
            return keter;
        }
    }
}
