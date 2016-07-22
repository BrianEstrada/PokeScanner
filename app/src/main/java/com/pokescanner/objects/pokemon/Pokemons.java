package com.pokescanner.objects.pokemon;

/**
 * Created by Brian on 7/21/2016.
 */
public class Pokemons {
    int Number;
    String Name;
    int encounterid;
    long expires;

    public Pokemons() {
    }

    public Pokemons(int number, String name, int encounterid, long expires) {
        Number = number;
        Name = name;
        this.encounterid = encounterid;
        this.expires = expires;
    }

    public int getNumber() {
        return Number;
    }

    public void setNumber(int number) {
        Number = number;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public int getEncounterid() {
        return encounterid;
    }

    public void setEncounterid(int encounterid) {
        this.encounterid = encounterid;
    }

    public long getExpires() {
        return expires;
    }

    public void setExpires(long expires) {
        this.expires = expires;
    }
}
