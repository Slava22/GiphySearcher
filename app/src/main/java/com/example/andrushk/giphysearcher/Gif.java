package com.example.andrushk.giphysearcher;

public class Gif {
    private String urlGif;
    private int width;
    private int height;

    public Gif(String urlGif, int width, int height) {
        this.urlGif = urlGif;
        this.width = width;
        this.height = height;
    }

    public String getUrlGif() {
        return urlGif;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}