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
import com.pokescanner.objects.MenuItem;

import java.util.ArrayList;

/**
 * Created by Brian on 7/22/2016.
 */
public class MenuRecycler extends RecyclerView.Adapter<MenuViewHolder> {
    private onItemClickListener listener;
    private ArrayList<MenuItem> menuItems;

    public interface onItemClickListener {
        void onItemClick(MenuItem item);
    }

    public MenuRecycler(ArrayList<MenuItem> menuItems, onItemClickListener listener) {
        this.menuItems = menuItems;
        this.listener = listener;
    }


    @Override
    public MenuViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recyler_menu_row, viewGroup, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MenuViewHolder menuViewHolder, int i) {
        menuViewHolder.bind(menuItems.get(i), listener);
    }

    @Override
    public int getItemCount() {
        return menuItems.size();
    }
}
