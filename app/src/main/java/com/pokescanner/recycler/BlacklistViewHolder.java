
package com.pokescanner.recycler;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.pokescanner.R;
import com.pokescanner.objects.FilterItem;
import com.pokescanner.utils.SettingsUtil;

/**
 * Created by Brian on 7/21/2016.
 */
public class BlacklistViewHolder extends RecyclerView.ViewHolder {
    ImageView imageFilterRow;
    TextView pokemonName;
    TextView tvStatus;
    CheckBox checkBox;
    Context context;

    public BlacklistViewHolder(View itemView) {
        super(itemView);

        pokemonName = (TextView) itemView.findViewById(R.id.tvName);
        checkBox = (CheckBox) itemView.findViewById(R.id.checkBox);
        imageFilterRow = (ImageView) itemView.findViewById(R.id.imageFilterRow);

        this.context = itemView.getContext();

        checkBox.setClickable(true);
    }

    public void bind(final FilterItem filterItem, final BlacklistRecyclerAdapter.onCheckedListener listener) {
        checkBox.setOnCheckedChangeListener(null);

        pokemonName.setText(filterItem.getName());
        checkBox.setChecked(filterItem.isFiltered());

        String uri;
        int pokemonnumber = filterItem.getNumber();

        if (SettingsUtil.getSettings(context).isShuffleIcons()) {
            uri = "ps" + pokemonnumber;
        }
        else uri = "p" + pokemonnumber;

        int resourceID = context.getResources().getIdentifier(uri, "drawable", context.getPackageName());
        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), resourceID);

        imageFilterRow.setImageBitmap(bm);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                filterItem.setFiltered(b);
                listener.onChecked(filterItem);
            }
        });
    }
}
