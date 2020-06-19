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

        channelListView = mView.findViewById(R.id.listview);
        channelListViewAdapter = new ChannelListViewAdapter(getContext());
        channelListView.setAdapter(channelListViewAdapter);
    }

    @Event(value = R.id.button_cancel)
    private void cancelClick(View v){
        dismiss();
    }

    //@Event(value = R.id.button_save)
    private void saveClick(View v){
        try {
            ListAdapter listAdapter = channelListView.getAdapter();
            for (int i = 0; i < listAdapter.getCount(); i++) {
                LteChannelCfg cfg = CacheManager.channels.get(i);
//                LinearLayout layout = (LinearLayout)listAdapter.getView(i,null,null);
//                BootstrapEditText tv_rxlevmin = (BootstrapEditText)layout.findViewById(R.id.editText_rxlevmin);
//                BootstrapEditText tv_fcn = (BootstrapEditText)layout.findViewById(R.id.editText_fcn);
//                BootstrapEditText tv_plmn = (BootstrapEditText)layout.findViewById(R.id.editText_plmn);
//                BootstrapEditText tv_pa = (BootstrapEditText)layout.findViewById(R.id.editText_pa);
//                BootstrapEditText tv_ga = (BootstrapEditText)layout.findViewById(R.id.editText_ga);
//                int fcn = StringUtils.toInt(tv_fcn.getText().toString());
//                int rxlevmin = StringUtils.toInt(tv_rxlevmin.getText().toString());
//                String plmn = tv_plmn.getText().toString();
//                int pa = StringUtils.toInt(tv_pa.getText().toString());
//                int ga = StringUtils.toInt(tv_ga.getText().toString());
//                tv_plmn.setText("22222");

                //G4MsgChannelCfg tmpCfg = (G4MsgChannelCfg)tmpObj;
                //ProtocolManager.setChannelConfig(cfg.getIdx(),null,tmpCfg.getPlmn(),tmpCfg.getPa(),tmpCfg.getGa(),null,null);
            }

            ToastUtils.showMessage(R.string.tip_15);
//            dismiss();
        } catch (NumberFormatException e) {
            new MySweetAlertDialog(getContext(), MySweetAlertDialog.ERROR_TYPE)
                    .setTitleText(getContext().getString(R.string.tip_16))
                    .show();
        } catch (Exception e){
            log.error("set channels error",e);
//            new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
//                    .setTitleText(e.getMessage())
//                    .show();
        }
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
