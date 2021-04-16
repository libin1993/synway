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
 * Created by wiker on 2016/4/29.
 */
public class UserChannelListAdapter extends BaseAdapter {
    private LayoutInflater layoutInflater;
    private List<String> dataList;
    private String idx;

    public UserChannelListAdapter(Context context,String idx, List<String> dataList) {
        layoutInflater = LayoutInflater.from(context);
        this.dataList = dataList;
        this.idx = idx;
    }



    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        View view = convertView;

        if (view == null) {
            view = layoutInflater.inflate(R.layout.doit_layout_user_channel_list_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.tvContent = view.findViewById(R.id.id_tv_content);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.tvContent.setText("通道："+idx+"      "+"Band："+dataList.get(position));

        return view;
    }

    static class ViewHolder {
        TextView tvContent;
    }
}
