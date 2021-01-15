package com.doit.net.view;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.doit.net.model.BlackBoxManger;
import com.doit.net.event.EventAdapter;
import com.doit.net.model.DBUeidInfo;
import com.doit.net.model.UCSIDBManager;
import com.doit.net.utils.DateUtils;
import com.doit.net.utils.MySweetAlertDialog;
import com.doit.net.utils.ToastUtils;
import com.doit.net.ucsi.R;

import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;
import org.xutils.x;

/**
 * Created by Zxc on 2018/12/29.
 */

public class ClearHistoryTimeDialog extends Dialog {
    private View mView;
    private EditText etStartTime;
    private EditText etEndTime;
    private Button btSure;
    private Button btCancel;

    Activity activity;

    public ClearHistoryTimeDialog(Activity activity) {
        super(activity, R.style.Theme_dialog);
        this.activity = activity;
        initView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(mView);
        x.view().inject(this,mView);
    }

    @Nullable
    @Override
    public ActionBar getActionBar() {
        return super.getActionBar();
    }

    private void initView(){
        LayoutInflater inflater= LayoutInflater.from(getContext());
        mView = inflater.inflate(R.layout.layout_clear_history, null);
        setCancelable(false);

        etStartTime = mView.findViewById(R.id.etStartTime);
        etStartTime.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                MyTimePickDialog myTimePicKDialog = new MyTimePickDialog(activity, etStartTime.getText().toString());
                myTimePicKDialog.dateTimePicKDialog(etStartTime);
            }
        });

        etEndTime = mView.findViewById(R.id.etEndTime);
        etEndTime.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                MyTimePickDialog myTimePicKDialog = new MyTimePickDialog(activity, etEndTime.getText().toString());
                myTimePicKDialog.dateTimePicKDialog(etEndTime);
            }
        });

        long endTime = System.currentTimeMillis();
        long startTime = endTime - 1000 * 3600 * 24 * 7;

        etStartTime.setText(DateUtils.convert2String(startTime,DateUtils.LOCAL_DATE));
        etEndTime.setText(DateUtils.convert2String(endTime,DateUtils.LOCAL_DATE));


        btSure = mView.findViewById(R.id.btSure);
        btSure.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                final String startTime = etStartTime.getText().toString();
                final String endTime = etEndTime.getText().toString();

                if ("".equals(startTime) || "".equals(endTime)) {
                    ToastUtils.showMessage("请确定开始时间和结束时间！");
                    return;
                }

                new MySweetAlertDialog(activity, MySweetAlertDialog.WARNING_TYPE)
                    .setTitleText("警告")
                    .setContentText("历史数据删除后无法恢复,确定删除吗?")
                    .setCancelText(activity.getString(R.string.cancel))
                    .setConfirmText(activity.getString(R.string.sure))
                    .showCancelButton(true)
                    .setConfirmClickListener(new MySweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(MySweetAlertDialog sweetAlertDialog) {
                            try {
                                UCSIDBManager.getDbManager().delete(DBUeidInfo.class, WhereBuilder.b("createDate", "BETWEEN",
                                        new long[]{DateUtils.convert2long(startTime, DateUtils.LOCAL_DATE), DateUtils.convert2long(endTime, DateUtils.LOCAL_DATE)}));
                            } catch (DbException e) {
                                new MySweetAlertDialog(activity, MySweetAlertDialog.ERROR_TYPE)
                                        .setTitleText(activity.getString(R.string.failed))
                                        .setContentText(activity.getString(R.string.del_history_error))
                                        .show();
                            }

                            ToastUtils.showMessage(R.string.clear_success);
                            sweetAlertDialog.dismiss();
                        }
                    }).show();

                dismiss();
                EventAdapter.call(EventAdapter.ADD_BLACKBOX,BlackBoxManger.CLEAN_HISTORY_DATA + startTime+" - " + endTime);
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
}