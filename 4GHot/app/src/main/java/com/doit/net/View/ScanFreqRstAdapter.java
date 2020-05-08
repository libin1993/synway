package com.doit.net.View;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.doit.net.Bean.ScanFreqRstBean;
import com.doit.net.ucsi.R;

import java.util.List;

/**
 * Created by Zxc on 2019/3/20.
 */

public class ScanFreqRstAdapter extends BaseAdapter {

    List<ScanFreqRstBean> listScanFreqResult;
    Context context;

    public ScanFreqRstAdapter(List<ScanFreqRstBean> listScanFreqResult, Context context) {
        // TODO Auto-generated constructor stub
        this.listScanFreqResult = listScanFreqResult;
        this.context = context;
    }

    public int getCount() {
        // TODO Auto-generated method stub
        return listScanFreqResult.size();
    }

    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return null;
    }

    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        final ViewHolder viewHolder;
        if(convertView == null){
            convertView = View.inflate(context, R.layout.scan_freq_rst_item, null);
            viewHolder = new ViewHolder();
            viewHolder.tvScanFreqRst = (TextView) convertView.findViewById(R.id.tvScanFreqRst);
            viewHolder.cbIsSelected = (CheckBox) convertView.findViewById(R.id.cbIsRstSeleted);
            convertView.setTag(viewHolder);
        }else{
            viewHolder=(ViewHolder) convertView.getTag();
        }
        viewHolder.tvScanFreqRst.setText(listScanFreqResult.get(position).getScanFreqRst());
        //显示checkBox
        viewHolder.cbIsSelected.setChecked(listScanFreqResult.get(position).isSelected());

        return convertView;

    }

    public void refreshData(){
        notifyDataSetChanged();
    }

    public void setList(List<ScanFreqRstBean> listScanFreqRst) {
        //listScanFreqResult.addAll(listScanFreqRst);
        listScanFreqResult = listScanFreqRst;
        refreshData();
    }

    class ViewHolder{
        TextView tvScanFreqRst;
        CheckBox cbIsSelected;
    }
}
