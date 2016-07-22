package com.pokescanner.recycler;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pokescanner.R;
import com.pokescanner.objects.pokemon.Pokemons;

import java.util.ArrayList;

/**
 * Created by Brian on 7/21/2016.
 */
public class BlacklistRecyclerAdapter extends RecyclerView.Adapter<BlacklistViewHolder> {
    private onCheckedListener listener;
    private ArrayList<Pokemons> pokemons;

    public interface onCheckedListener {
        void onChecked(Pokemons pokemons, boolean b);
    }

    public BlacklistRecyclerAdapter(ArrayList<Pokemons> pokemons, onCheckedListener listener) {
        this.pokemons = pokemons;
        this.listener = listener;
    }


    @Override
    public BlacklistViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_pokemon_blacklist, viewGroup, false);
        return new BlacklistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BlacklistViewHolder blacklistViewHolder, int i) {
        blacklistViewHolder.bind(pokemons.get(i), listener);
    }

    @Override
    public int getItemCount() {
        return pokemons.size();
    }
}
