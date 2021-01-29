package com.doit.net.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.doit.net.model.BlackBoxManger;
import com.doit.net.event.EventAdapter;
import com.doit.net.model.AccountManage;
import com.doit.net.model.UCSIDBManager;
import com.doit.net.model.UserInfo;
import com.doit.net.utils.LoadingUtils;
import com.doit.net.utils.MySweetAlertDialog;
import com.doit.net.utils.ToastUtils;
import com.doit.net.view.ModifyUserInfoDialog;
import com.doit.net.ucsi.R;

import org.xutils.ex.DbException;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by Zxc on 2018/11/27.
 */

public class UserListAdapter extends BaseSwipeAdapter {
    private Context mContext;

    private static List<UserInfo> listUserInfo = new ArrayList<>();

    public UserListAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setUserInfoList(List<UserInfo> userInfoList) {
        listUserInfo = userInfoList;
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.layout_user_info;
    }

    @Override
    public View generateView(int position, ViewGroup parent) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.layout_user_item, null);
        return v;
    }


    @Override
    public void fillValues(int position, View convertView) {
        LinearLayout layoutItemText = convertView.findViewById(R.id.layoutUserItemText);
        if (position % 2 == 0){
            layoutItemText.setBackgroundColor(mContext.getResources().getColor(R.color.deepgrey2));
        }else{
            layoutItemText.setBackgroundColor(mContext.getResources().getColor(R.color.black));
        }
        TextView tvIndex = convertView.findViewById(R.id.tvIndex);
        TextView tvUserInfo = convertView.findViewById(R.id.tvUserInfo);

        final UserInfo userInfo = listUserInfo.get(position);
        tvIndex.setText(" " +(position + 1) + ".");
        tvUserInfo.setText("账号："+userInfo.getAccount()  + "            密码：" + userInfo.getPassword() + "\n" +
                            "备注："+userInfo.getRemake());
        tvUserInfo.setTag(position);

        convertView.findViewById(R.id.ivDelete).setOnClickListener(new DeleteUserListener(position));
        convertView.findViewById(R.id.ivModify).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ModifyUserInfoDialog modifyUserInfoDialog = new ModifyUserInfoDialog(mContext, userInfo.getAccount(), userInfo.getPassword(),userInfo.getRemake());
                modifyUserInfoDialog.show();
            }
        });
    }

    @Override
    public int getCount() {
        return listUserInfo.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void updateView(){
        notifyDataSetChanged();
    }

    class DeleteUserListener implements View.OnClickListener{
        private int position;

        public DeleteUserListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            new MySweetAlertDialog(mContext, MySweetAlertDialog.WARNING_TYPE)
                    .setTitleText("删除用户")
                    .setContentText("确定要删除用户吗？")
                    .setCancelText(mContext.getString(R.string.cancel))
                    .setConfirmText(mContext.getString(R.string.sure))
                    .showCancelButton(true)
                    .setConfirmClickListener(new MySweetAlertDialog.OnSweetClickListener() {

                        @Override
                        public void onClick(MySweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismiss();

                            UserInfo resp = listUserInfo.get(position);
                            try {
                                UCSIDBManager.getDbManager().delete(resp);
                                if (AccountManage.UpdateAccountToDevice()){
                                    EventAdapter.call(EventAdapter.REFRESH_USER_LIST);
                                    EventAdapter.call(EventAdapter.ADD_BLACKBOX,BlackBoxManger.DELTE_USER+resp.getAccount());
                                    LoadingUtils.loading(mContext);
                                }else{
                                    UCSIDBManager.getDbManager().save(resp);
                                    ToastUtils.showMessageLong(R.string.del_user_fail_ftp_error);
                                }

                            } catch (DbException e) {
                                new SweetAlertDialog(mContext, SweetAlertDialog.ERROR_TYPE)
                                        .setTitleText(mContext.getString(R.string.del_user_fail))
                                        .show();
                            }

                        }
                    })
                    .show();
        }
    }


}
