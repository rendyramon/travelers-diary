package com.travelersdiary.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.travelersdiary.R;
import com.travelersdiary.models.TodoTask;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TodoTaskAdapter extends RecyclerView.Adapter<TodoTaskAdapter.ViewHolder> {

    private boolean mViewAsCheckboxes;

    public void setViewAsCheckboxes(boolean viewAsCheckboxes) {
        this.mViewAsCheckboxes = viewAsCheckboxes;
        notifyDataSetChanged();
    }

    private boolean mEditable;

    public void setEditable(boolean editable) {
        this.mEditable = editable;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.task_item_checkbox)
        AppCompatCheckBox checkBox;
        @Bind(R.id.task_item_edit_text)
        EditText editText;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    private ArrayList<TodoTask> mTodoTaskItemList;

    public TodoTaskAdapter(ArrayList<TodoTask> itemList) {
        this.mTodoTaskItemList = itemList;
        this.mViewAsCheckboxes = false;
        this.mEditable = false;
    }

    public TodoTaskAdapter(ArrayList<TodoTask> itemList, boolean viewAsCheckboxes) {
        this.mTodoTaskItemList = itemList;
        this.mViewAsCheckboxes = viewAsCheckboxes;
        this.mEditable = false;
    }

    private Context mContext;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_task_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        TodoTask model = mTodoTaskItemList.get(position);

        if (mEditable) {
            viewHolder.editText.setInputType(InputType.TYPE_CLASS_TEXT
                    | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                    | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
                    | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            viewHolder.editText.setFocusable(true);
            viewHolder.editText.setFocusableInTouchMode(true);
        } else {
            viewHolder.editText.setInputType(InputType.TYPE_NULL);
            viewHolder.editText.setFocusable(false);
            viewHolder.editText.setFocusableInTouchMode(false);
        }

        viewHolder.editText.setText(model.getItem());
        viewHolder.checkBox.setChecked(model.isChecked());

        if (mViewAsCheckboxes) {
            viewHolder.checkBox.setVisibility(View.VISIBLE);
            if (model.isChecked()) {
                viewHolder.editText.setPaintFlags(viewHolder.editText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                viewHolder.editText.setPaintFlags(viewHolder.editText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }
        } else {
            viewHolder.checkBox.setVisibility(View.GONE);
            viewHolder.editText.setPaintFlags(viewHolder.editText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    @Override
    public int getItemCount() {
        return mTodoTaskItemList.size();
    }
}
