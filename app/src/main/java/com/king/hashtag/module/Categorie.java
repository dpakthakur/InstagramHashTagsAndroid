package com.king.hashtag.module;

/**
 * Created by asus on 05/04/2018.
 */

public class Categorie {
    int id;
    String name;


    public Categorie(int id, String name){
        this.id=id;
        this.name=name;
    }

    public int getId(){
        return this.id;
    }

    public String getName(){
        return this.name;
    }
}
