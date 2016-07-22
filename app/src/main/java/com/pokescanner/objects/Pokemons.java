/*
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */



package com.pokescanner.objects;

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
