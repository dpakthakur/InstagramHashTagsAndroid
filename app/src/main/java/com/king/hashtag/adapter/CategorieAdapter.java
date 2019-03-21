package com.king.hashtag.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.king.hashtag.R;
import com.king.hashtag.SettingsClass;
import com.king.hashtag.module.Categorie;
import java.util.Collections;
import java.util.List;

/**
 * Created by asus on 05/04/2018.
 */

public class CategorieAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<Categorie> mCategorie = Collections.emptyList();
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    public CategorieAdapter(Context mContext, List<Categorie> mCategorie) {
        this.mContext = mContext;
        this.mCategorie = mCategorie;
        this.mInflater = LayoutInflater.from(mContext);
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View menuItemLayoutView ;
        if(SettingsClass.supportRTL)
            menuItemLayoutView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.categorie_view_rtl, parent, false);
        else menuItemLayoutView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.categorie_view, parent, false);
        return new MenuItemViewHolder(menuItemLayoutView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MenuItemViewHolder menuItemHolder = (MenuItemViewHolder) holder;
        menuItemHolder.title.setText(((Categorie)mCategorie.get(position)).getName().trim());
    }

    @Override
    public long getItemId(int position) {
        return mCategorie.get(position).getId();
    }

    @Override
    public int getItemCount() {
        return mCategorie.size();
    }

    public class MenuItemViewHolder  extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView title;
        public MenuItemViewHolder (View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title) ;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    public Object getItem(int id) {
        return mCategorie.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    @Override
    public int getItemViewType(int position){
        return 1;
    }
}
