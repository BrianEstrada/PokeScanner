package com.pokescanner.multiboxing;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pokescanner.R;
import com.pokescanner.objects.User;

import java.util.ArrayList;

public class MultiboxingAdapter extends RecyclerView.Adapter<MultiboxingAdapter.MBViewHolder> {

    private ArrayList<User> accountData;
    private Context mContext;
    private onLongClickListener listener;

    public interface onLongClickListener {
        void onLongClick(User user);
    }


    public MultiboxingAdapter(Context context, ArrayList<User> accountData,onLongClickListener listener) {
        mContext = context;
        this.listener = listener;
        this.accountData = accountData;
    }

    @Override
    public MultiboxingAdapter.MBViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_multiboxing_list_row, parent, false);
        return new MBViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MultiboxingAdapter.MBViewHolder holder, final int position) {
        User user = accountData.get(position);
        holder.bind(user,listener,position);
    }

    @Override
    public int getItemCount() {
        return accountData.size();
    }


    public class MBViewHolder extends RecyclerView.ViewHolder {
        public TextView tvAccountNumber;
        public TextView tvAccountName;
        public ImageView imgStatus;
        public ProgressBar progressBarStatus;

        public MBViewHolder(View view) {
            super(view);
            tvAccountNumber = (TextView) view.findViewById(R.id.tvAccountNumber);
            tvAccountName = (TextView) view.findViewById(R.id.tvAccountName);
            imgStatus = (ImageView) view.findViewById(R.id.imgStatus);
            progressBarStatus = (ProgressBar) view.findViewById(R.id.progressBarStatus);
        }

        public void bind(final User user, final onLongClickListener listener, int position) {
            tvAccountNumber.setText(String.valueOf(position));
            tvAccountName.setText(user.getUsername());

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    listener.onLongClick(user);
                    return true;
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
        }
    }


}
