package com.doit.net.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.doit.net.event.AddToLocalBlackListener;
import com.doit.net.protocol.ProtocolManager;
import com.doit.net.model.BlackBoxManger;
import com.doit.net.event.EventAdapter;
import com.doit.net.model.DBBlackInfo;
import com.doit.net.model.UCSIDBManager;
import com.doit.net.utils.ToastUtils;
import com.doit.net.ucsi.R;

import org.xutils.DbManager;
import org.xutils.ex.DbException;
import org.xutils.x;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by Zxc on 2019/2/18.
 */

public class ModifyNamelistInfoDialog extends Dialog {
    private String modifyName;
    private String modifyRemake;
    private String modifyIMSI;
    private View mView;
    private EditText etName;
    private EditText etRemake;
    private EditText etIMSI;
    private Button btSave;
    private Button btCancel;

    public ModifyNamelistInfoDialog(Context context, String name, String imsi, String remake ) {
        super(context, R.style.Theme_dialog);
        modifyName = name;
        modifyRemake = remake;
        modifyIMSI = imsi;
        initView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(mView);
        x.view().inject(this,mView);
    }

    private void initView(){
        LayoutInflater inflater= LayoutInflater.from(getContext());
        mView = inflater.inflate(R.layout.layout_modify_namelist_info, null);
        setCancelable(false);

        etName = mView.findViewById(R.id.etName);
        etName.setText(modifyName);
        etRemake = mView.findViewById(R.id.etRemark);
        etRemake.setText(modifyRemake);
        etIMSI = mView.findViewById(R.id.etPassword);
        etIMSI.setText(modifyIMSI);
        btSave = mView.findViewById(R.id.btSave);
        btSave.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String imsi =  etIMSI.getText().toString();
                String name =  etName.getText().toString();
                String remark =  etRemake.getText().toString();

                if (TextUtils.isEmpty(imsi) || imsi.length() <15){
                    ToastUtils.showMessage("请输入15位IMSI");
                    return;
                }

                try {
                    DbManager db = UCSIDBManager.getDbManager();
                    DBBlackInfo tmpNamelist = db.selector(DBBlackInfo.class)
                            .where("imsi", "=", modifyIMSI)
                            .findFirst();

                    if (tmpNamelist == null){
                        ToastUtils.showMessage(R.string.modify_namelist_fail);
                        return;
                    }

                    //如果IMSI发生了变动就比较麻烦了
                    if (!imsi.equals(modifyIMSI)){
                        ProtocolManager.setBlackList("3", "#"+modifyIMSI);
                        db.delete(tmpNamelist);
                        new AddToLocalBlackListener(getContext(),name,imsi,remark).onClick(v);
                    }else{
                        tmpNamelist.setName(etName.getText().toString());
                        tmpNamelist.setRemark(etRemake.getText().toString());
                        db.update(tmpNamelist, "name", "remark");
                    }

                    ToastUtils.showMessage(R.string.modify_namelist_success);
                    EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.MODIFY_NAMELIST+"修改名单"+modifyName+"为:"+ name
                            + "+" + imsi + "+" + remark);
                } catch (DbException e) {
                    new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                            .setTitleText(getContext().getString(R.string.modify_namelist_fail))
                            .show();
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
}
