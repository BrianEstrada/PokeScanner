package com.pokescanner.recycler;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pokescanner.R;
import com.pokescanner.objects.Pokemons;

import java.util.ArrayList;

/**
 * Created by Brian on 7/28/2016.
 */
public class ListViewRecyclerAdapter extends RecyclerView.Adapter<ListViewHolder>{
    ArrayList<Pokemons> pokemon;

    public ListViewRecyclerAdapter(ArrayList<Pokemons> pokemon) {
        this.pokemon = pokemon;
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_list_view, parent, false);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ListViewHolder holder, int position) {
        holder.bind(pokemon.get(position));
    }

    @Override
    public int getItemCount() {
        return pokemon.size();
    }
}
