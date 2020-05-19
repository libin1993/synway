package com.doit.net.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.doit.net.bean.CollideTimePointBean;
import com.doit.net.ucsi.R;

import java.util.List;

/**
 * Created by Zxc on 2018/12/17.
 */

public class CollideTimePointAdapter extends BaseAdapter {
    private List<CollideTimePointBean> listItem;
    private LayoutInflater layoutinflater;//视图容器，用来导入布局


    public CollideTimePointAdapter(Context context, List<CollideTimePointBean> dataList) {
        this.listItem = dataList;
        this.layoutinflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        CollideTimePointBean collideTimePeriodItem = listItem.get(i);
        CollideTimePointAdapter.ViewHolder holder;
        View view;
        if(convertView == null) {
            holder= new CollideTimePointAdapter.ViewHolder();
            view = layoutinflater.inflate(R.layout.collide_time_point_item, null);

            holder.tvTime = (TextView) view.findViewById(R.id.tvTime);
            holder.tvLocation = (TextView) view.findViewById(R.id.tvRemark);
            view.setTag(holder);
        }
        else {
            view = convertView;
            holder = (CollideTimePointAdapter.ViewHolder) view.getTag();
        }

        holder.tvTime.setText(collideTimePeriodItem.getTime());
        holder.tvLocation.setText(collideTimePeriodItem.getLocation());

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

    static class ViewHolder {
        private TextView tvTime;
        private TextView tvLocation;
    }

}
