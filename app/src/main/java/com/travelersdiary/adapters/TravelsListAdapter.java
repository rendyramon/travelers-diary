package com.travelersdiary.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.travelersdiary.R;
import com.travelersdiary.models.Travel;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TravelsListAdapter extends FirebaseRecyclerAdapter<Travel, TravelsListAdapter.ViewHolder> {

    public interface OnItemClickListener  {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    private static OnItemClickListener  onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener  onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public TravelsListAdapter(Firebase ref) {
        super(Travel.class, R.layout.list_item_travel, TravelsListAdapter.ViewHolder.class, ref);
    }

    public TravelsListAdapter(Query ref) {
        super(Travel.class, R.layout.list_item_travel, TravelsListAdapter.ViewHolder.class, ref);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.item_travel_title_text_view)
        TextView textViewTitle;
        @Bind(R.id.item_travel_description_text_view)
        TextView textViewDescription;
        @Bind(R.id.item_travel_active_icon)
        ImageView imageViewActiveIcon;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null)
                        onItemClickListener.onItemClick(v, getLayoutPosition());
                }
            });
            view.setLongClickable(true);
/*
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemLongClick(v, getLayoutPosition());
                        return true;
                    }
                    return false;
                }
            });
*/
        }
    }

    @Override
    protected void populateViewHolder(TravelsListAdapter.ViewHolder viewHolder, Travel model, int position) {
        viewHolder.textViewTitle.setText(model.getTitle());
        viewHolder.textViewDescription.setText(model.getDescription());
        if (model.isActive()) {
            viewHolder.imageViewActiveIcon.setVisibility(View.VISIBLE);
        } else {
            viewHolder.imageViewActiveIcon.setVisibility(View.INVISIBLE);
        }
    }
}
