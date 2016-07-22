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

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Brian on 7/22/2016.
 */
public class FilterItem extends RealmObject{
    @PrimaryKey
    int Number;
    String Name;
    boolean filtered;

    public FilterItem(int number, String name, boolean filtered) {
        Number = number;
        Name = name;
        this.filtered = filtered;
    }

    public FilterItem() {
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

    public boolean isFiltered() {
        return filtered;
    }

    public void setFiltered(boolean filtered) {
        this.filtered = filtered;
    }
}
