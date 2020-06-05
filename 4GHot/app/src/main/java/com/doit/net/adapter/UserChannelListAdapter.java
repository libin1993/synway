package com.doit.net.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.doit.net.bean.LteChannelCfg;
import com.doit.net.Model.CacheManager;
import com.doit.net.ucsi.R;

/**
 * Created by wiker on 2016/4/29.
 */
public class UserChannelListAdapter extends BaseAdapter {
    private LayoutInflater layoutInflater;

    public UserChannelListAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
    }

    private String idx;

    public String getIdx() {
        return idx;
    }

    public void setIdx(String idx) {
        this.idx = idx;
    }

    @Override
    public int getCount() {
        return 1;
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
            view = layoutInflater.inflate(R.layout.doit_layout_user_channel_list_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.tvContent = view.findViewById(R.id.id_tv_content);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        //Context context = parent.getContext();
        LteChannelCfg cfg = CacheManager.getChannelByIdx(idx);
        String txt = "";
        if(cfg != null){
            //UtilBaseLog.printLog("显示："+cfg.getChangeBand() + "     " + cfg.getBand());
            viewHolder.tvContent.setText("通道："+cfg.getIdx()+"      "+"Band："+cfg.getChangeBand());
        }

        return view;
    }

    static class ViewHolder {
        TextView tvContent;
    }
}
