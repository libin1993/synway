package com.doit.net.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.doit.net.application.MyApplication;
import com.doit.net.bean.LteChannelCfg;
import com.doit.net.Event.EventAdapter;
import com.doit.net.Protocol.ProtocolManager;
import com.doit.net.Model.CacheManager;
import com.doit.net.Utils.ToastUtils;
import com.doit.net.ucsi.R;
import com.doit.net.udp.g4.bean.G4MsgChannelCfg;
import com.suke.widget.SwitchButton;

import java.util.HashMap;
import java.util.Map;

public class ChannelListViewAdapter extends BaseAdapter {

    private Context mContext;
    private Map<Integer,G4MsgChannelCfg> values = new HashMap<>();


    public ChannelListViewAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void refreshData(){
        notifyDataSetChanged();
    }

    static class ViewHolder{
        TextView title;
        SwitchButton rfButton;
//        BootstrapEditText reLevelMin;
        EditText fcn;
        EditText plmn;
        EditText pa;
        EditText ga;
        EditText rlm;
        EditText etAltFcn;
        Button saveBtn;

    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if(convertView == null){
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.doit_layout_channel_item, null);
            holder.title = (TextView)convertView.findViewById(R.id.title_text);
            holder.rfButton = (SwitchButton)convertView.findViewById(R.id.id_switch_rf);
//            holder.reLevelMin = (BootstrapEditText)convertView.findViewById(R.id.editText_rxlevmin);
            holder.fcn = (EditText)convertView.findViewById(R.id.editText_fcn);
            holder.plmn = (EditText)convertView.findViewById(R.id.editText_plmn);
            holder.pa = (EditText)convertView.findViewById(R.id.editText_pa);
            holder.ga = (EditText)convertView.findViewById(R.id.editText_ga);
            holder.rlm = (EditText)convertView.findViewById(R.id.etRLM);
            holder.etAltFcn = (EditText)convertView.findViewById(R.id.etAltFcn);
            holder.saveBtn = (Button)convertView.findViewById(R.id.button_save);

            convertView.setTag(holder);
        }else{
            holder = (ViewHolder)convertView.getTag();
        }
        fillValues(position,holder);
        return convertView;
    }


    public void fillValues(int position, final ViewHolder holder) {
        final LteChannelCfg cfg = CacheManager.channels.get(position);
        if(cfg == null){
            return;
        }
        holder.title.setText("通道："+cfg.getIdx()+"    "+ "频段:" + cfg.getBand());
        holder.rfButton.setChecked(cfg.isRfOpen());
//        holder.reLevelMin.setText(cfg.getRxlevmin()==null?"":""+cfg.getRxlevmin().intValue());
        holder.fcn.setText(cfg.getFcn()==null?"":""+cfg.getFcn());
        holder.plmn.setText(cfg.getPlmn());
        holder.ga.setText(cfg.getGa()==null?"":""+cfg.getGa());
        holder.pa.setText(cfg.getPa()==null?"":""+cfg.getPa());
        holder.rlm.setText(cfg.getRlm()==null?"":""+cfg.getRlm());
        holder.etAltFcn.setText(cfg.getAltFcn()==null?"":""+cfg.getAltFcn());
        holder.rfButton.setOnClickListener(new SwitchButton.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (CacheManager.checkDevice(MyApplication.mContext)){
                    return;
                }

                SwitchButton button = (SwitchButton)v;
                if(button.isChecked()){
                    ProtocolManager.openRf(cfg.getIdx());
                    button.setChecked(false);
                }else{
                    ProtocolManager.closeRf(cfg.getIdx());
                    button.setChecked(true);
                }
            }
        });

        holder.saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtils.showMessage(mContext, R.string.tip_15);
                EventAdapter.call(EventAdapter.SHOW_PROGRESS);
                String fcn = holder.fcn.getText().toString();
                String plmn = holder.plmn.getText().toString();
                String pa = holder.pa.getText().toString();
                String ga = holder.ga.getText().toString();
                String rlm = holder.rlm.getText().toString();
                String alt_fcn = holder.etAltFcn.getText().toString();
                ProtocolManager.setChannelConfig(cfg.getIdx(), fcn, plmn, pa, ga,rlm,"",alt_fcn);
            }
        });
        holder.rfButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
//                if(isChecked){
//                    ProtocolManager.openRf(cfg.getIdx());
//                }else{
//                    ProtocolManager.closeRf(cfg.getIdx());
//                }
            }
        });
    }


    @Override
    public int getCount() {
        return CacheManager.channels.size();
    }

    @Override
    public Object getItem(int position) {
        return CacheManager.channels.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
