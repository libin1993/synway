package com.doit.net.View;

import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.doit.net.Base.BaseFragment;
import com.doit.net.Event.EventAdapter;
import com.doit.net.Event.ProtocolManager;
import com.doit.net.Model.CacheManager;
import com.doit.net.Utils.MySweetAlertDialog;
import com.doit.net.Utils.ToastUtils;
import com.doit.net.Utils.UtilBaseLog;
import com.doit.net.ucsi.R;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Zxc on 2019/12/17.
 */

public class StartPageFragment extends BaseFragment {
    private View rootView;

    private ImageButton ivPowerStart;
    public StartPageFragment(){
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (null != rootView) {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (null != parent) {
                parent.removeView(rootView);
            }
            return rootView;
        }
        rootView = inflater.inflate(R.layout.start_page_fragment, null);

        initWidget();

        return rootView;
    }

    private void initWidget() {
        ivPowerStart = rootView.findViewById(R.id.ivPowerStart);
        ivPowerStart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    ivPowerStart.setImageResource(R.drawable.start_button_press);
                }else if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    //改为抬起时的图片
                    ivPowerStart.setImageResource(R.drawable.start_button);

                    if (CacheManager.isDeviceOk()){
                        turnToDetectPage();
                        ProtocolManager.openAllRf();
                    }else{
                        new MySweetAlertDialog(getContext(), MySweetAlertDialog.WARNING_TYPE)
                                .setTitleText("提示")
                                .setContentText("设备未连接或未初始化完成，是否进入工作页面？")
                                .setCancelText(getContext().getString(R.string.cancel))
                                .setConfirmText(getContext().getString(R.string.sure))
                                .showCancelButton(true)
                                .setConfirmClickListener(new MySweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(MySweetAlertDialog sweetAlertDialog) {
                                        CacheManager.setPressStartButtonFlag(true);
                                        turnToDetectPage();
                                        sweetAlertDialog.dismiss();
                                    }

                                })
                                .show();
                    }


                }
                return false;
            }
        });
    }

    private void turnToDetectPage() {
        EventAdapter.call(EventAdapter.POWER_START);
    }
}
