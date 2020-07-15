package com.doit.net.View;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.doit.net.adapter.ChannelListViewAdapter;
import com.doit.net.bean.LteChannelCfg;
import com.doit.net.Model.CacheManager;
import com.doit.net.Utils.MySweetAlertDialog;
import com.doit.net.ucsi.R;
import com.doit.net.Utils.ToastUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xutils.view.annotation.Event;
import org.xutils.x;

/**
 * Created by wiker on 2016/4/29.
 */
public class ChannelsDialog extends Dialog {

    private final static Logger log = LoggerFactory.getLogger(ChannelsDialog.class);

    private View mView;
    private Context mContext;
    private ListView channelListView;
    private ChannelListViewAdapter channelListViewAdapter;

    public ChannelsDialog(Context context) {
        super(context,R.style.Theme_dialog);
        mContext = context;
        initView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(mView);
        x.view().inject(this,mView);
        setCancelable(false);

        if(!CacheManager.isDeviceOk()){
            return;
        }
    }

    private void initView(){
        LayoutInflater inflater= LayoutInflater.from(getContext());
        mView = inflater.inflate(R.layout.doit_layout_channels_dialog, null);

//        channelListView = mView.findViewById(R.id.listview);
//        channelListViewAdapter = new ChannelListViewAdapter(getContext());
//        channelListView.setAdapter(channelListViewAdapter);
    }

    @Event(value = R.id.button_cancel)
    private void cancelClick(View v){
        dismiss();
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (!hasFocus) {
            return;
        }
        setHeight();
    }


    //设置dialog高度百分比
    private void setHeight() {
        Window window = getWindow();
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        WindowManager.LayoutParams attributes = window.getAttributes();
        if (window.getDecorView().getHeight() >= (int) (displayMetrics.heightPixels * 0.8)) {
            attributes.height = (int) (displayMetrics.heightPixels * 0.8);
        }
        window.setAttributes(attributes);
    }

}
