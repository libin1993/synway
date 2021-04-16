package com.doit.net.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.doit.net.event.AddToLocationListener;
import com.doit.net.utils.BlackBoxManger;
import com.doit.net.event.EventAdapter;
import com.doit.net.utils.CacheManager;
import com.doit.net.bean.DBBlackInfo;
import com.doit.net.utils.UCSIDBManager;
import com.doit.net.utils.LogUtils;
import com.doit.net.view.MySweetAlertDialog;
import com.doit.net.view.ModifyNamelistInfoDialog;
import com.doit.net.ucsi.R;
import com.doit.net.utils.DateUtils;
import com.doit.net.utils.StringUtils;

import org.xutils.ex.DbException;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class BlacklistAdapter extends BaseSwipeAdapter {

    private Context mContext;

    private static List<DBBlackInfo> ueidList = new ArrayList<>();

    public BlacklistAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void refreshData(){
        notifyDataSetChanged();
    }

    public void setUeidList(List<DBBlackInfo> ueidList) {
        BlacklistAdapter.ueidList = ueidList;
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }

    @Override
    public View generateView(final int position, ViewGroup parent) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.doit_layout_name_list_item, null);
        //动画

        return v;
    }

    class DeleteNameListener implements View.OnClickListener{
        private int position;

        public DeleteNameListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            new MySweetAlertDialog(mContext, MySweetAlertDialog.WARNING_TYPE)
                    .setTitleText("删除名单")
                    .setContentText("确定要删除名单吗？")
                    .setCancelText(mContext.getString(R.string.cancel))
                    .setConfirmText(mContext.getString(R.string.sure))
                    .showCancelButton(true)
                    .setConfirmClickListener(new MySweetAlertDialog.OnSweetClickListener() {

                        @Override
                        public void onClick(MySweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismiss();

                            DBBlackInfo resp = ueidList.get(position);
                            try {
                                ueidList.remove(position);
                                UCSIDBManager.getDbManager().delete(resp);
//                                ProtocolManager.setBlackList("3", "#"+resp.getImsi());
                                EventAdapter.call(EventAdapter.REFRESH_BLACKLIST);
                                EventAdapter.call(EventAdapter.ADD_BLACKBOX,BlackBoxManger.DELETE_NAMELIST +resp.getName()+"+"+resp.getImsi()+"+"+resp.getRemark());
                            } catch (DbException e) {
                                LogUtils.log("删除名单失败"+e.getMessage());

                                new SweetAlertDialog(mContext, SweetAlertDialog.ERROR_TYPE)
                                        .setTitleText(mContext.getString(R.string.del_namelist_fail))
                                        .show();
                            }

                        }
                    })
                    .show();
        }
    }

    @Override
    public void fillValues(int position, View convertView) {
        TextView index = convertView.findViewById(R.id.position);
        index.setText((position + 1) + ".");
        SwipeLayout swipeLayout = convertView.findViewById(R.id.swipe);
        TextView BlacklistInfo = convertView.findViewById(R.id.text_data);

        DBBlackInfo resp = ueidList.get(position);
        String name = "";
        if(!StringUtils.isBlank(resp.getName())){
            name = mContext.getString(R.string.lab_name)+resp.getName()+"    ";
        }
        String remark = "";
        if (!TextUtils.isEmpty(resp.getRemark())){
            remark = "备注："+resp.getRemark()+"\n";
        }
        BlacklistInfo.setText(name +"IMSI："+resp.getImsi()+ "\n" +remark+ mContext.getString(R.string.lab_create_date)
                + DateUtils.getDateByFormat(resp.getCreateDate(),"yyyy-MM-dd HH:mm:ss"));
        BlacklistInfo.setTag(position);


        convertView.findViewById(R.id.delete).setOnClickListener(new DeleteNameListener(position));
        convertView.findViewById(R.id.ivModify).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ModifyNamelistInfoDialog modifyNamelistDialog = new ModifyNamelistInfoDialog(mContext, resp.getName(), resp.getImsi(),resp.getRemark());
                modifyNamelistDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                      EventAdapter.call(EventAdapter.REFRESH_BLACKLIST);

                        if (swipeLayout !=null){
                            swipeLayout.close();
                        }
                    }
                });
                modifyNamelistDialog.show();
            }
        });

        if(CacheManager.getLocMode()){
            convertView.findViewById(R.id.add_to_localtion).setOnClickListener(new AddToLocationListener(mContext,resp.getImsi()));
        }else{
            convertView.findViewById(R.id.add_to_localtion).setVisibility(View.GONE);
        }

    }

    @Override
    public int getCount() {
        return ueidList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
