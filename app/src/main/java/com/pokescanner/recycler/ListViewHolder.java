package com.pokescanner.recycler;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
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
    TextView tvTimer;
    ImageView PokemonImage;

    ImageButton btnDirections;
    Context context;

    public ListViewHolder(View itemView) {
        super(itemView);

        Distance = (TextView) itemView.findViewById(R.id.tvDistance);
        PokemonName = (TextView) itemView.findViewById(R.id.tvPokemonName);
        PokemonImage = (ImageView) itemView.findViewById(R.id.PokemonImage);

        tvTimer = (TextView) itemView.findViewById(R.id.tvTimer);
        btnDirections = (ImageButton) itemView.findViewById(R.id.btnDirections);

        this.context = itemView.getContext();

        itemView.setClickable(true);
    }

    public void bind(final Pokemons pokemons) {
        Bitmap bitmap = DrawableUtils.getBitmapFromView(pokemons.getResourceID(context),"",context,DrawableUtils.PokemonType);

        tvTimer.setText(DrawableUtils.getExpireTime(pokemons.getExpires()));
        PokemonImage.setImageBitmap(bitmap);
        PokemonName.setText(pokemons.getFormalName(context));
        Distance.setText(String.valueOf(Math.round(pokemons.getDistance()))+"m");

        btnDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + pokemons.getLatitude() + "," + pokemons.getLongitude() + "(" + pokemons.getFormalName(itemView.getContext()) + ")");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(mapIntent);
                }
            }
        });
    }
}
