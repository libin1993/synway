package com.doit.net.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.doit.net.Event.AddToLocationListener;
import com.doit.net.Event.UIEventManager;
import com.doit.net.Model.CacheManager;
import com.doit.net.ucsi.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zxc on 2018/11/30.
 */

public class RealtimeNamelistRptAdapter extends BaseSwipeAdapter {

    private Context mContext;

    private List<String> listNamelist = new ArrayList<>();  //这个列表需要去重，但是LinkedHashSet不能根据position获取

    private TextView tvNamelistItem;
    private TextView tvIndex;

    public RealtimeNamelistRptAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void refreshData(){
        notifyDataSetChanged();
    }

    public void addItem(String namelist){
        listNamelist.add(0, namelist);
        refreshData();
    }

    public void clear(){
        listNamelist.clear();
        refreshData();
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }

    @Override
    public View generateView(int position, ViewGroup parent) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.doit_layout_name_list_item, null);
        return v;
    }

    @Override
    public void fillValues(final int position, View convertView) {
        tvIndex = convertView.findViewById(R.id.position);
        tvNamelistItem = convertView.findViewById(R.id.text_data);

        tvIndex.setText((position + 1) + ". ");
        final String resp = listNamelist.get(position);
        tvNamelistItem.setText(resp);

        convertView.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listNamelist.remove(position);
                UIEventManager.call(UIEventManager.KEY_REFRESH_NAMELIST_RPT_LIST);
            }
        });

        if(CacheManager.getLocMode()){
            convertView.findViewById(R.id.add_to_localtion).setOnClickListener(new AddToLocationListener(
                    position,mContext,resp.split("\n")[0].split(":")[1],""));
        }else{
            convertView.findViewById(R.id.add_to_localtion).setVisibility(View.GONE);
        }

        convertView.findViewById(R.id.ivModify).setVisibility(View.GONE);
        tvNamelistItem.setTag(position);
    }

    @Override
    public int getCount() {
        return listNamelist.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
