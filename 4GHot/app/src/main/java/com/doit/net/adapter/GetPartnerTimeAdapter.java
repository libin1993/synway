package com.doit.net.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.doit.net.ucsi.R;

import java.util.List;

/**
 * Created by Zxc on 2018/12/11.
 */

public class GetPartnerTimeAdapter extends BaseAdapter {
    private List<String> listItem;
    private LayoutInflater layoutinflater;//视图容器，用来导入布局

    public GetPartnerTimeAdapter(Context context, List<String> dataList)
    {
        this.listItem = dataList;
        this.layoutinflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        String time = listItem.get(i);
        GetPartnerTimeAdapter.ViewHolder holder;
        View view;
        if(convertView == null) {
            holder= new GetPartnerTimeAdapter.ViewHolder();
            view = layoutinflater.inflate(R.layout.time_item, null);

            holder.tvTime = view.findViewById(R.id.tvTime);
            view.setTag(holder);
        }
        else {
            view = convertView;
            holder = (GetPartnerTimeAdapter.ViewHolder) view.getTag();
        }

        holder.tvTime.setText(time);

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
    }
}
