package com.doit.net.view;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.doit.net.utils.ToastUtils;
import com.doit.net.ucsi.R;

import org.xutils.x;

/**
 * Created by Zxc on 2018/12/25.
 */

public class AddTimePointDialog extends Dialog {
    private View mView;
    private BootstrapEditText etTimePoint;
    private BootstrapEditText etRemark;
    private BootstrapButton btTimePoint;
    private BootstrapButton btCancel;
    private String initialTime;
    private Activity activity;
    private IAddTimePointListener mListener;

    public AddTimePointDialog(Activity activity, String initialTime, IAddTimePointListener listener) {
        super(activity, R.style.Theme_dialog);
        this.initialTime = initialTime;
        this.activity = activity;
        this.mListener = listener;
        initView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(mView);
        x.view().inject(this,mView);
        etTimePoint.setText(initialTime);
    }

    private void initView(){
        LayoutInflater inflater= LayoutInflater.from(getContext());
        mView = inflater.inflate(R.layout.layout_add_time_point_dialog, null);
        setCancelable(false);

        etTimePoint = mView.findViewById(R.id.etTimePoint);
        etTimePoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyTimePickDialog myTimePicKDialog;
                if ("".equals(etTimePoint.getText().toString())){
                    myTimePicKDialog = new MyTimePickDialog(activity, initialTime);
                }else{
                    myTimePicKDialog = new MyTimePickDialog(activity, etTimePoint.getText().toString());
                }

                myTimePicKDialog.dateTimePicKDialog(etTimePoint);
            }
        });

        etRemark = mView.findViewById(R.id.etRemark);
        btTimePoint = mView.findViewById(R.id.btTimePoint);
        btTimePoint.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String timePoint = etTimePoint.getText().toString();
                String remark = etRemark.getText().toString();

                if ("".equals(timePoint)){
                    ToastUtils.showMessage( "请设定时间！");
                    return;
                }

                if (mListener != null) {
                    mListener.addTimePoint(timePoint, remark);
                }

                dismiss();
            }
        });


        btCancel = mView.findViewById(R.id.btCancel);
        btCancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public interface IAddTimePointListener {
        void addTimePoint(String timePoint, String remark);
    }

}
