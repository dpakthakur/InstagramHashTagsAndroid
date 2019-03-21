package com.king.hashtag.adapter;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.king.hashtag.R;
import com.king.hashtag.SettingsClass;
import com.king.hashtag.module.Categorie;
import com.king.hashtag.module.Tags;

import java.util.List;

import static com.king.hashtag.TagsActivity.showFullAd;

/**
 * Created by asus on 05/04/2018.
 */

public class TagsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<Tags> mTag;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    public TagsAdapter(Context mContext, List<Tags> mTag) {
        this.mContext = mContext;
        this.mTag = mTag;
        this.mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View menuItemLayoutView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.tags_view, parent, false);
        return new MenuItemViewHolder(menuItemLayoutView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        MenuItemViewHolder menuItemHolder = (MenuItemViewHolder) holder;
        menuItemHolder.title.setText(((Tags)mTag.get(position)).getName().trim());
        menuItemHolder.hashtags.setText(((Tags)mTag.get(position)).getHashtags().trim());
        menuItemHolder.copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFullAd(true);
                ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("hashtags",((Tags)mTag.get(position)).getHashtags().trim());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(mContext, mContext.getResources().getString(R.string.hashtags_copied),Toast.LENGTH_LONG).show();
            }
        });
        menuItemHolder.copyOpenInsta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFullAd(true);
                ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(mContext.getResources().getString(R.string.hashtags_copied),((Tags)mTag.get(position)).getHashtags().trim());
                clipboard.setPrimaryClip(clip);
                LayoutInflater inflater =(LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View alertLayout = inflater.inflate(R.layout.dailog, null);
                final TextView mssg = alertLayout.findViewById(R.id.message);
                String dailogueMessg = mContext.getResources().getString(R.string.do_you_want_open_insta);
                mssg.setText(dailogueMessg);
                AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
                ///alert.setTitle(mContext.getResources().getString(R.string.remove_from_favorite_contacts));
                // this is set the view from XML inside AlertDialog
                alert.setView(alertLayout);
                // disallow cancel of AlertDialog on click of back button and outside touch
                alert.setCancelable(false);
                alert.setNegativeButton(mContext.getResources().getString(R.string.btn_no), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showFullAd(true);
                    }
                });

                alert.setPositiveButton(mContext.getResources().getString(R.string.btn_yes), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri uri = Uri.parse("http://instagram.com/");
                        Intent likeIng = mContext.getPackageManager().getLaunchIntentForPackage("com.instagram.android");
                        if(isPackageExisted("com.instagram.android"))
                        {
                            try {
                                mContext.startActivity(likeIng);
                            } catch (ActivityNotFoundException e) {

                            }
                        }else  mContext.startActivity(new Intent(Intent.ACTION_VIEW, uri));
                    }
                });
                AlertDialog dialog = alert.create();
                dialog.show();

            }
        });
    }

    @Override
    public long getItemId(int position) {
        return mTag.get(position).getId();
    }

    @Override
    public int getItemCount() {
        return mTag.size();
    }

    public class MenuItemViewHolder  extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView title, hashtags;
        Button copy, copyOpenInsta;
        public MenuItemViewHolder (View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.tag_title) ;
            hashtags = (TextView) itemView.findViewById(R.id.tag_hashtags) ;
            copy = (Button) itemView.findViewById(R.id.btn_copy_tag) ;
            copyOpenInsta = (Button) itemView.findViewById(R.id.btn_copy_tag_open_insta) ;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    public Object getItem(int id) {
        return mTag.get(id);
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

    public boolean isPackageExisted(String targetPackage){
        PackageManager pm=mContext.getPackageManager();
        try {
            PackageInfo info=pm.getPackageInfo(targetPackage,PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }
}
