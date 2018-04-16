package com.summertaker.reader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.summertaker.reader.common.BaseDataAdapter;
import com.summertaker.reader.data.Category;

import java.util.ArrayList;

public class MainAdapter extends BaseDataAdapter {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private ArrayList<Category> mCategories;

    public MainAdapter(Context context, ArrayList<Category> categories) {
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mCategories = categories;
    }

    @Override
    public int getCount() {
        return mCategories.size();
    }

    @Override
    public Object getItem(int position) {
        return mCategories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        final MainAdapter.ViewHolder holder;
        final Category category = mCategories.get(position);

        if (convertView == null) {
            holder = new MainAdapter.ViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.main_item, null);

            holder.tvTitle = convertView.findViewById(R.id.tvTitle);

            convertView.setTag(holder);
        } else {
            holder = (MainAdapter.ViewHolder) convertView.getTag();
        }

        holder.tvTitle.setText(category.getTitle());

        return convertView;
    }

    static class ViewHolder {
        TextView tvTitle;
    }
}

