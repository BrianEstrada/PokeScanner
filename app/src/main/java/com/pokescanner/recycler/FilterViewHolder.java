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

/**
 * Created by Brian on 7/21/2016.
 */
public class FilterViewHolder extends RecyclerView.ViewHolder {
    ImageView imageFilterRow;
    TextView pokemonName;
    TextView tvStatus;
    CheckBox checkBox;
    Context context;

    public FilterViewHolder(View itemView) {
        super(itemView);

        pokemonName = (TextView) itemView.findViewById(R.id.tvName);
        checkBox = (CheckBox) itemView.findViewById(R.id.checkBox);
        imageFilterRow = (ImageView) itemView.findViewById(R.id.imageFilterRow);

        this.context = itemView.getContext();

        checkBox.setClickable(true);
    }

    public void bind(final FilterItem filterItem, final FilterRecyclerAdapter.onCheckedListener listener) {

        pokemonName.setText(filterItem.getName());
        checkBox.setChecked(filterItem.isFiltered());

        String uri = "p" + filterItem.getNumber();
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
