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



package com.pokescanner.recycler;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pokescanner.R;
import com.pokescanner.objects.FilterItem;

import java.util.ArrayList;

/**
 * Created by Brian on 7/21/2016.
 */
public class WhitelistRecyclerAdapter extends RecyclerView.Adapter<WhitelistViewHolder> {
    private onCheckedListener listener;
    private ArrayList<FilterItem> filteritems;

    public interface onCheckedListener {
        void onChecked(FilterItem filterItem);
    }

    public WhitelistRecyclerAdapter(ArrayList<FilterItem> filteritems, onCheckedListener listener) {
        this.filteritems = filteritems;
        this.listener = listener;
    }


    @Override
    public WhitelistViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_pokemon_filter_row, viewGroup, false);
        return new WhitelistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(WhitelistViewHolder filterViewHolder, int i) {
        filterViewHolder.bind(filteritems.get(i), listener);
    }

    @Override
    public int getItemCount() {
        return filteritems.size();
    }
}
