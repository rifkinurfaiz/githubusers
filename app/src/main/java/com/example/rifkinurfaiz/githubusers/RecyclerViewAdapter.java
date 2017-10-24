package com.example.rifkinurfaiz.githubusers;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by rifkinurfaiz on 10/22/2017.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    List<ListItem> listItems;
    private Context context;

    public RecyclerViewAdapter(Context context, List<ListItem> listItems) {
        this.context = context;
        this.listItems = listItems;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d("Item Count", "" + listItems.size());
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_view_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final ListItem listItem = listItems.get(position);

        holder.username.setText(listItem.getUsername());
        Picasso.with(context).load(listItem.getAvatar()).into(holder.avatar);
        holder.linearLayoutItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "User: " + listItem.getUsername(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView username;
        public ImageView avatar;
        LinearLayout linearLayoutItem;

        public ViewHolder(View itemView) {
            super(itemView);
            username = (TextView) itemView.findViewById(R.id.textViewUsername);
            avatar = (ImageView) itemView.findViewById(R.id.imageViewAvatar);
            linearLayoutItem = (LinearLayout) itemView.findViewById(R.id.linearLayoutItem);
        }
    }
}
