package com.doit.net.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.doit.net.ucsi.R;

/**
 * Created by wiker on 2016/4/29.
 */
public class SystemSetupAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;

    public SystemSetupAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        View view = convertView;

        if (view == null) {
            view = layoutInflater.inflate(R.layout.doit_layout_device_contl, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.textView = (TextView) view.findViewById(R.id.text_view);
            viewHolder.imageView = (ImageView) view.findViewById(R.id.image_view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        Context context = parent.getContext();
        switch (position) {
            case 0:
                viewHolder.textView.setText("小区设置");
                viewHolder.imageView.setImageResource(R.drawable.icon_setting);
                break;
            case 1:
                viewHolder.textView.setText("通道设置");
                viewHolder.imageView.setImageResource(R.drawable.icon_list);
                break;
            case 2:
                viewHolder.textView.setText("设备重启");
                viewHolder.imageView.setImageResource(R.drawable.icon_power);
                break;
            case 3:
                viewHolder.textView.setText("刷新参数");
                viewHolder.imageView.setImageResource(R.drawable.icon_refresh);
                break;
        }

        return view;
    }

    static class ViewHolder {
        TextView textView;
        ImageView imageView;
    }
}
