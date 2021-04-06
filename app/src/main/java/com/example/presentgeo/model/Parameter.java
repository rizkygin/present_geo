package com.example.presentgeo.model;

import java.util.List;

public class Parameter {
    private Boolean code;
    private String status;
    private List<Param> data;


    public Boolean getCode() {
        return code;
    }

    public String getStatus() {
        return status;
    }

    public List<Param> getData() {
        return data;
    }



    public class Param {
        private String param;
        private String val;

        public String getParam() {
            return param;
        }

        public String getVal() {
            return val;
        }
    }
}
