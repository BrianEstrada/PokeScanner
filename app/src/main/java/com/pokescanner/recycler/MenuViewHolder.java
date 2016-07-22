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
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.pokescanner.R;
import com.pokescanner.objects.MenuItem;

/**
 * Created by Brian on 7/22/2016.
 */
public class MenuViewHolder extends RecyclerView.ViewHolder {
    TextView text;
    ImageView img;

    public MenuViewHolder(View itemView) {
        super(itemView);
        text = (TextView) itemView.findViewById(R.id.textView);
        itemView.setClickable(true);
    }

    public void bind(final MenuItem item, final MenuRecycler.onItemClickListener listener) {
        text.setText(item.getText());

        //Add icons in the future

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(item);
            }
        });
    }

}
