package com.doit.net.View;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.doit.net.Event.EventAdapter;
import com.doit.net.Utils.FTPManager;
import com.doit.net.Utils.FileUtils;
import com.doit.net.Utils.LicenceUtils;
import com.doit.net.Utils.PermissionUtils;
import com.doit.net.Utils.ToastUtils;
import com.doit.net.activity.ScanCodeActivity;
import com.doit.net.ucsi.R;


/**
 * Created by Zxc on 2018/12/29.
 */

public class LicenceDialog extends Dialog implements EventAdapter.EventCall{
    private View mView;
    private EditText etAuthorizeCode;
    private TextView tvMachineId;
    private TextView tvDueTime;
    private Context mContext;

    public final static int CAMERA_REQUEST_CODE = 2;


    private OnCloseListener onCloseListener;

    public LicenceDialog(Activity activity) {
        super(activity, R.style.Theme_dialog);
        mContext = activity;
        initView();
    }

    public void setOnCloseListener(OnCloseListener onCloseListener) {
        this.onCloseListener = onCloseListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(mView);
        EventAdapter.setEvent(EventAdapter.SCAN_CODE, this);
        tvMachineId.setText(LicenceUtils.machineID);
        tvDueTime.setText(LicenceUtils.getDueTime());
    }

    @Nullable
    @Override
    public ActionBar getActionBar() {
        return super.getActionBar();
    }

    private void initView(){
        LayoutInflater inflater= LayoutInflater.from(getContext());
        mView = inflater.inflate(R.layout.layout_licence_dialog, null);
        setCancelable(false);

        etAuthorizeCode = mView.findViewById(R.id.etAuthorizeCode);
        tvDueTime = mView.findViewById(R.id.tvDueTime);
        tvMachineId =  mView.findViewById(R.id.tvMachineId);
        Button btAuthorize = (Button) mView.findViewById(R.id.btAuthorize);
        ImageView ivScanCode = mView.findViewById(R.id.iv_scan_code);
        ivScanCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PermissionUtils.getInstance().hasPermission(mContext, Manifest.permission.CAMERA)) {
                    mContext.startActivity(new Intent(mContext, ScanCodeActivity.class));
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ActivityCompat.requestPermissions((Activity) mContext,new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
                    }
                }
            }
        });
        btAuthorize.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String authorizeCode = etAuthorizeCode.getText().toString().trim();
                if ("".equals(authorizeCode)){
                    ToastUtils.showMessage(getContext(), "请输入授权码");
                    return;
                }

                if(LicenceUtils.checkAuthorizeCode(authorizeCode)){
                    LicenceUtils.authorizeCode = authorizeCode;
                    ToastUtils.showMessageLong(getContext(),"授权成功，到期时间："+ LicenceUtils.getDueTime());
                    dismiss();

                    new Thread() {
                        public void run() {
                            try {
                                FTPManager.getInstance().connect();
                                boolean isFinish = FileUtils.getInstance().stringToFile(authorizeCode,
                                        LicenceUtils.LOCAL_LICENCE_PATH + LicenceUtils.LICENCE_FILE_NAME);
                                if (isFinish){
                                    boolean isUploaded = FTPManager.getInstance().uploadFile(LicenceUtils.LOCAL_LICENCE_PATH,
                                            LicenceUtils.LICENCE_FILE_NAME);
                                    if (isUploaded){
                                        FileUtils.getInstance().deleteFile(LicenceUtils.LOCAL_LICENCE_PATH
                                                + LicenceUtils.LICENCE_FILE_NAME);
                                    }

                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                }else{
                    ToastUtils.showMessage(getContext(), "授权码有误，请确认后输入！");
                }
            }
        });

        Button btCancel = (Button) mView.findViewById(R.id.btCancel);
        btCancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (onCloseListener !=null){
                    onCloseListener.onClose();
                }
                dismiss();
            }
        });
    }

    @Override
    public void call(String key, Object val) {
        if (key.equals(EventAdapter.SCAN_CODE)){
            etAuthorizeCode.setText(String.valueOf(val));
        }
    }

    public interface OnCloseListener{
        void onClose();
    }
}

