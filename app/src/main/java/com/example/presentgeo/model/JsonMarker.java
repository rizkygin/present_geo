package com.example.presentgeo.model;

import java.util.List;

public class JsonMarker {
    private Boolean code;
    private String message;
    private List<PolygonMarker> data;

    public Boolean getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public List<PolygonMarker> getData() {
        return data;
    }
}
