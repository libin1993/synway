package com.doit.net.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.doit.net.bean.CollideTimePeriodBean;
import com.doit.net.ucsi.R;

import java.util.List;

/**
 * Created by Zxc on 2018/5/22.
 */

public class CollideTimePeriodAdapter extends BaseAdapter {
    private List<CollideTimePeriodBean> listItem;
    private LayoutInflater layoutinflater;//视图容器，用来导入布局


    public CollideTimePeriodAdapter(Context context, List<CollideTimePeriodBean> dataList) {
        this.listItem = dataList;
        this.layoutinflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        CollideTimePeriodBean collideTimePeriodItem = listItem.get(i);
        ViewHolder holder;
        View view;
        if(convertView == null) {
            holder= new ViewHolder();
            view = layoutinflater.inflate(R.layout.time_period_item, null);

            holder.tvStartTime = (TextView) view.findViewById(R.id.tvStartTime);
            holder.tvEndTime = (TextView) view.findViewById(R.id.tvEndTime);
            view.setTag(holder);
        }
        else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }

        holder.tvStartTime.setText(collideTimePeriodItem.getStartTime());
        holder.tvEndTime.setText(collideTimePeriodItem.getEndTime());

        return view;
    }

    @Override
    public int getCount() {
        return listItem.size();
    }

    @Override
    public Object getItem(int position) {
        return listItem.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void updateView(){
        this.notifyDataSetChanged();
    }

    static class ViewHolder
    {
        private TextView tvStartTime;
        private TextView tvEndTime;
    }

}
