package com.mylunartest.dyzs.lunartest;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.List;

/**
 * Created by maidou on 2015/12/30.
 */
public abstract class BasicAdapter<T> extends BaseAdapter {
    public List<T> mList;
    public BasicAdapter(List<T> list) {
        this.mList = list;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public abstract View getView(int position, View convertView, ViewGroup parent);

}

