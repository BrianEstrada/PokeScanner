package com.pokescanner.multiboxing;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pavelsikun.vintagechroma.ChromaDialog;
import com.pavelsikun.vintagechroma.IndicatorMode;
import com.pavelsikun.vintagechroma.colormode.ColorMode;
import com.pokescanner.R;
import com.pokescanner.objects.User;

import java.util.ArrayList;

public class MultiboxingAdapter extends RecyclerView.Adapter<MultiboxingAdapter.MBViewHolder> {

    private ArrayList<User> accountData;
    private Context mContext;
    private accountRemovalListener listener;
    private accountChangeColorListener listener2;

    public interface accountRemovalListener {
        void onRemove(User user);
    }

    public interface accountChangeColorListener {
        void changeColor(User user);
    }



    public MultiboxingAdapter(Context context, ArrayList<User> accountData,accountRemovalListener listener,accountChangeColorListener listener2) {
        mContext = context;
        this.listener = listener;
        this.accountData = accountData;
        this.listener2 = listener2;
    }

    @Override
    public MultiboxingAdapter.MBViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_multiboxing_list_row, parent, false);
        return new MBViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MultiboxingAdapter.MBViewHolder holder, final int position) {
        User user = accountData.get(position);
        holder.bind(user,listener,listener2);
    }

    @Override
    public int getItemCount() {
        return accountData.size();
    }


    public class MBViewHolder extends RecyclerView.ViewHolder {
        public TextView tvAccountName;
        public ImageView accountColor;
        public ImageView imgStatus;
        public ProgressBar progressBarStatus;
        public ImageButton btnRemoveAccount;

        public MBViewHolder(View view) {
            super(view);
            tvAccountName = (TextView) view.findViewById(R.id.tvAccountName);
            accountColor = (ImageView) view.findViewById(R.id.accountColor);
            imgStatus = (ImageView) view.findViewById(R.id.imgStatus);
            progressBarStatus = (ProgressBar) view.findViewById(R.id.progressBarStatus);
            btnRemoveAccount = (ImageButton) view.findViewById(R.id.btnRemoveAccount);
        }

        public void bind(final User user, final accountRemovalListener listener, final accountChangeColorListener listener2) {
            if(user.getAuthType() == User.GOOGLE)
                tvAccountName.setText("Google account");
            else
                tvAccountName.setText(user.getUsername());

            btnRemoveAccount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onRemove(user);
                }
            });

            int color = ResourcesCompat.getColor(mContext.getResources(),R.color.colorPrimary,null);

            if (user.getAccountColor() != 0) {
                color = user.getAccountColor();
            }

            Drawable drawable = ResourcesCompat.getDrawable(mContext.getResources(),R.drawable.circle_account_color,null);

            if (drawable != null) {
                drawable = DrawableCompat.wrap(drawable);
                drawable.setAlpha(128);
                drawable.setColorFilter(color, PorterDuff.Mode.SRC_OVER);
                accountColor.setImageDrawable(drawable);
            }

            accountColor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener2.changeColor(user);
                }
            });

            switch(user.getStatus())
            {
                case User.STATUS_UNKNOWN:
                    progressBarStatus.setVisibility(View.VISIBLE);
                    imgStatus.setVisibility(View.GONE);
                    break;
                case User.STATUS_INVALID:
                    progressBarStatus.setVisibility(View.GONE);
                    imgStatus.setVisibility(View.VISIBLE);
                    imgStatus.setImageResource(R.drawable.ic_cancel_red);
                    break;
                case User.STATUS_VALID:
                    progressBarStatus.setVisibility(View.GONE);
                    imgStatus.setVisibility(View.VISIBLE);
                    imgStatus.setImageResource(R.drawable.ic_check_green);
                    break;
            }

            //For google, accept automatically
            if(user.getAuthType() == User.GOOGLE) {
                progressBarStatus.setVisibility(View.GONE);
                imgStatus.setVisibility(View.VISIBLE);
                imgStatus.setImageResource(R.drawable.ic_check_green);
            }
        }
    }


}
