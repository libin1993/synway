package com.doit.net.View;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.doit.net.Bean.AnalysisResultBean;
import com.doit.net.ucsi.R;

import java.util.List;

/**
 * Created by Zxc on 2018/11/22.
 */

public class AnalysisResultAdapter extends ArrayAdapter<AnalysisResultBean> {
    private int resourceId;

    public AnalysisResultAdapter(@NonNull Context context, int textViewResourceId, @NonNull List<AnalysisResultBean> objects) {
        super(context, textViewResourceId, objects);
        this.resourceId = textViewResourceId;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        AnalysisResultBean collideTimePeriodItem = getItem(i);
        View view;
        if(convertView == null){
            view =  LayoutInflater.from(getContext()).inflate(resourceId, parent,false);
        }else{
            view = convertView;
        }
        TextView tvIMSI = (TextView)view.findViewById(R.id.tvIMSI);
        TextView tvTimes = (TextView)view.findViewById(R.id.tvTimes);
        tvIMSI.setText(collideTimePeriodItem.getImsi());
        tvTimes.setText(collideTimePeriodItem.getTimes());

        return view;
    }

    public void updateView(){
        this.notifyDataSetChanged();
    }
}
