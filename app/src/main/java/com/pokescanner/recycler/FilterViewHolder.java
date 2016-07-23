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
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
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
    Switch swt;
    Context context;

    public FilterViewHolder(View itemView) {
        super(itemView);

        pokemonName = (TextView) itemView.findViewById(R.id.tvName);
        tvStatus = (TextView) itemView.findViewById(R.id.tvStatus);
        swt = (Switch) itemView.findViewById(R.id.swt);
        imageFilterRow = (ImageView) itemView.findViewById(R.id.imageFilterRow);

        this.context = itemView.getContext();

        swt.setClickable(true);
    }

    public void bind(final FilterItem filterItem, final FilterRecyclerAdapter.onCheckedListener listener) {

        pokemonName.setText(filterItem.getName());
        swt.setChecked(filterItem.isFiltered());

        String uri = "p" + filterItem.getNumber();
        int resourceID = context.getResources().getIdentifier(uri, "drawable", context.getPackageName());
        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), resourceID);

        imageFilterRow.setImageBitmap(bm);

        swt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            filterItem.setFiltered(b);
            if (filterItem.isFiltered()) {
                tvStatus.setText(itemView.getContext().getString(R.string.filter_on));
            }else{
                tvStatus.setText(itemView.getContext().getString(R.string.filter_off));
            }
            listener.onChecked(filterItem);
        }
    });
    }
}
