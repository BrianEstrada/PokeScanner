package com.pokescanner.recycler;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.pokescanner.R;
import com.pokescanner.objects.Pokemons;
import com.pokescanner.utils.DrawableUtils;

/**
 * Created by Brian on 7/28/2016.
 */
public class ListViewHolder extends RecyclerView.ViewHolder {
    TextView Distance;
    TextView PokemonName;
    ImageView PokemonImage;
    Context context;

    public ListViewHolder(View itemView) {
        super(itemView);

        Distance = (TextView) itemView.findViewById(R.id.tvDistance);
        PokemonName = (TextView) itemView.findViewById(R.id.tvPokemonName);
        PokemonImage = (ImageView) itemView.findViewById(R.id.PokemonImage);

        this.context = itemView.getContext();

        itemView.setClickable(true);
    }

    public void bind(final Pokemons pokemons, final ListViewRecyclerAdapter.OnClickListener listener) {
        Bitmap bitmap = DrawableUtils.getBitmapFromView(pokemons.getResourceID(context),"",context,DrawableUtils.PokemonType);


        PokemonImage.setImageBitmap(bitmap);
        PokemonName.setText(pokemons.getFormalName(context));
        Distance.setText(String.valueOf(Math.round(pokemons.getDistance()))+"M");

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClick(pokemons);
            }
        });
    }
}
