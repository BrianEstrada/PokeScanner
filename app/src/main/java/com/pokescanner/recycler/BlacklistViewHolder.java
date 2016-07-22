package com.pokescanner.recycler;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.pokescanner.R;
import com.pokescanner.objects.pokemon.Pokemons;

/**
 * Created by Brian on 7/21/2016.
 */
public class BlacklistViewHolder extends RecyclerView.ViewHolder {
    TextView pokemonName;
    Switch swt;

    public BlacklistViewHolder(View itemView) {
        super(itemView);

        pokemonName = (TextView) itemView.findViewById(R.id.tvName);
        swt = (Switch) itemView.findViewById(R.id.swt);

        swt.setClickable(true);
    }

    public void bind(final Pokemons pokemons, final BlacklistRecyclerAdapter.onCheckedListener listener) {
        swt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                listener.onChecked(pokemons,b);
            }
        });
    }
}
