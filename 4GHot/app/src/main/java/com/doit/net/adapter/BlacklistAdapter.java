package com.doit.net.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.doit.net.Event.AddToLocationListener;
import com.doit.net.Model.BlackBoxManger;
import com.doit.net.Event.EventAdapter;
import com.doit.net.Protocol.ProtocolManager;
import com.doit.net.Model.CacheManager;
import com.doit.net.Model.DBBlackInfo;
import com.doit.net.Model.UCSIDBManager;
import com.doit.net.Utils.LogUtils;
import com.doit.net.View.ModifyNamelistInfoDialog;
import com.doit.net.ucsi.R;
import com.doit.net.Utils.DateUtils;
import com.doit.net.Utils.StringUtils;

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
//        SwipeLayout swipeLayout = (SwipeLayout)v.findViewById(getSwipeLayoutResourceId(position));

//        swipeLayout.addSwipeListener(new SimpleSwipeListener() {
//            @Override
//            public void onOpen(SwipeLayout layout) {
//                YoYo.with(Techniques.Tada).duration(500).delay(100).playOn(layout.findViewById(R.id.trash));
//            }
//        });


//        swipeLayout.setOnDoubleClickListener(new SwipeLayout.DoubleClickListener() {
//            @Override
//            public void onDoubleClick(SwipeLayout layout, boolean surface) {
//                Toast.makeText(mContext, "DoubleClick", Toast.LENGTH_SHORT).show();
//            }
//        });
        DBBlackInfo resp = ueidList.get(position);
        return v;
    }

    class DeleteNameListener implements View.OnClickListener{
        private int position;

        public DeleteNameListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            DBBlackInfo resp = ueidList.get(position);
            try {
                ueidList.remove(position);
                UCSIDBManager.getDbManager().delete(resp);
                ProtocolManager.setBlackList("3", "#"+resp.getImsi());
                EventAdapter.call(EventAdapter.REFRESH_BLACKLIST);
                EventAdapter.call(EventAdapter.ADD_BLACKBOX,BlackBoxManger.DELTE_NAMELIST+resp.getImsi()+"+"+resp.getName());
            } catch (DbException e) {
                LogUtils.log("删除名单失败"+e.getMessage());

                new SweetAlertDialog(mContext, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText(mContext.getString(R.string.del_namelist_fail))
                        .show();
            }
        }
    }

    @Override
    public void fillValues(int position, View convertView) {
        TextView index = convertView.findViewById(R.id.position);
        index.setText((position + 1) + ".");

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
                    }
                });
                modifyNamelistDialog.show();
            }
        });
        //if(BuildConfig.LOC_MODEL){
        if(CacheManager.getLocMode()){
            convertView.findViewById(R.id.add_to_localtion).setOnClickListener(new AddToLocationListener(position,mContext,resp));
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
