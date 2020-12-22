package com.doit.net.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.doit.net.utils.FormatUtils;
import com.doit.net.utils.ToastUtils;
import com.doit.net.ucsi.R;

/**
 * Author：Libin on 2020/5/22 17:05
 * Email：1993911441@qq.com
 * Describe：添加频点
 */
public class AddFcnDialog extends Dialog {
    private Context mContext;
    private String mBand;
    private String mFcn;
    private String mTitle;
    private View mView;
    private OnConfirmListener mOnConfirmListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(mView);

    }
    public AddFcnDialog(Context context, String title,String band,String fcn,OnConfirmListener onConfirmListener) {
        super(context, R.style.Theme_dialog);
        mContext = context;
        mOnConfirmListener = onConfirmListener;
        mBand = band;
        mFcn = fcn;
        mTitle = title;
        initView();
    }

    private void initView() {
        LayoutInflater inflater= LayoutInflater.from(getContext());
        mView = inflater.inflate(R.layout.dialog_add_fcn, null);
        setCancelable(false);
        TextView tvTitle = mView.findViewById(R.id.tv_dialog_title);
        EditText etFcn1 = mView.findViewById(R.id.et_fcn1);
        EditText etFcn2 = mView.findViewById(R.id.et_fcn2);
        EditText etFcn3 = mView.findViewById(R.id.et_fcn3);
        Button btnSave = mView.findViewById(R.id.btn_save);
        Button btnCancel = mView.findViewById(R.id.btn_cancel);

        tvTitle.setText(mTitle);
        if (!TextUtils.isEmpty(mFcn)){
            String[] split = mFcn.split(",");
            for (int i = 0; i < split.length; i++) {
                switch (i){
                    case 0:
                        etFcn1.setText(split[i]);
                        break;
                    case 1:
                        etFcn2.setText(split[i]);
                        break;
                    case 2:
                        etFcn3.setText(split[i]);
                        break;
                }
            }
        }

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fcn1 = etFcn1.getText().toString().trim();
                String fcn2 = etFcn2.getText().toString().trim();
                String fcn3 = etFcn3.getText().toString().trim();

                if (TextUtils.isEmpty(fcn1) || TextUtils.isEmpty(fcn2) || TextUtils.isEmpty(fcn3)){
                    ToastUtils.showMessage("请输入3个频点");
                    return;
                }

                String fcn="";

                if (!TextUtils.isEmpty(fcn1)){
                    fcn += fcn1+",";
                }
                if (!TextUtils.isEmpty(fcn2)){
                    fcn += fcn2+",";
                }
                if (!TextUtils.isEmpty(fcn3)){
                    fcn += fcn3+",";
                }

                fcn = fcn.substring(0,fcn.length()-1);

                if (!FormatUtils.getInstance().fcnRange(mBand, fcn)){
                    return;
                }

                if (mOnConfirmListener!=null){
                    mOnConfirmListener.onConfirm(fcn);
                }
                dismiss();
            }
        });
    }



    public interface OnConfirmListener{
        void onConfirm(String value);
    }
}
