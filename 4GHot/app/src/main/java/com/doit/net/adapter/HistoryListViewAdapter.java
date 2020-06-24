package com.doit.net.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.doit.net.Event.AddToLocalBlackListener;
import com.doit.net.Event.AddToLocationListener;
import com.doit.net.Event.AddToWhitelistListner;
import com.doit.net.Model.CacheManager;
import com.doit.net.Model.DBUeidInfo;
import com.doit.net.Model.ImsiMsisdnConvert;
import com.doit.net.Model.VersionManage;
import com.doit.net.ucsi.R;
import com.doit.net.Utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class HistoryListViewAdapter extends BaseSwipeAdapter {

    private Context mContext;
//    private HistoryListViewAdapter.onItemLongClickListener mOnItemLongClickListener;
    private MotionEvent motionEvent;
    private static List<DBUeidInfo> ueidList = new ArrayList<>();

    public HistoryListViewAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void refreshData(){
        notifyDataSetChanged();
    }

    public void setUeidList(List<DBUeidInfo> ueidList) {
        HistoryListViewAdapter.ueidList = ueidList;
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }

    @Override
    public View generateView(final int position, ViewGroup parent) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.doit_layout_ueid_list_item, null);
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

        return v;
    }

    @Override
    public void fillValues(int position, View convertView) {
        TextView index = convertView.findViewById(R.id.position);
        index.setText((position + 1) + ".");

        TextView text_data = convertView.findViewById(R.id.tvUeidItemText);

        DBUeidInfo resp = ueidList.get(position);
        text_data.setText("IMSI:"+resp.getImsi()+"\n"+mContext.getString(R.string.lab_rpt_time)+ DateUtils.convert2String(resp.getCreateDate(), DateUtils.LOCAL_DATE));
        text_data.setTag(position);

        if (VersionManage.isPoliceVer()){
            convertView.findViewById(R.id.add_to_black).setOnClickListener(new AddToLocalBlackListener(mContext,resp.getImsi()));
        }else if(VersionManage.isArmyVer()){
            convertView.findViewById(R.id.add_to_black).setOnClickListener(new AddToWhitelistListner(mContext,resp.getImsi()));;
        }

        //if(BuildConfig.LOC_MODEL){
        if(CacheManager.getLocMode()){
            convertView.findViewById(R.id.add_to_localtion).setOnClickListener(new AddToLocationListener(position,mContext,resp.getImsi(),resp.getTmsi()));
        }else{
            convertView.findViewById(R.id.add_to_localtion).setVisibility(View.GONE);
        }

//        if (mOnItemLongClickListener != null) {
//            //获取触摸点的坐标，以决定pop从哪里弹出
//            convertView.setOnTouchListener(new View.OnTouchListener() {
//                @SuppressLint("ClickableViewAccessibility")
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                    switch (event.getAction()) {
//                        case MotionEvent.ACTION_DOWN:
//                            motionEvent = event;
//                            break;
//                        default:
//                            break;
//                    }
//                    // 如果onTouch返回false,首先是onTouch事件的down事件发生，此时，如果长按，触发onLongClick事件；
//                    // 然后是onTouch事件的up事件发生，up完毕，最后触发onClick事件。
//                    return false;
//                }
//            });
//
//
//            final int pos = position;
//            convertView.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View v) {
//                    //int position = holder.getLayoutPosition();
//                    mOnItemLongClickListener.onItemLongClick(motionEvent, pos);
//                    //返回true 表示消耗了事件 事件不会继续传递
//                    return true; //长按了就禁止swipe弹出
//                }
//            });
//        }
    }

    public  List<DBUeidInfo> getUeidList(){
        return ueidList;
    }

//    public void setOnItemLongClickListener(HistoryListViewAdapter.onItemLongClickListener mOnItemLongClickListener) {
//        this.mOnItemLongClickListener = mOnItemLongClickListener;
//    }

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

    static class ViewHolder {
        private TextView itemText;
    }

    public interface onItemLongClickListener {
        void onItemLongClick(MotionEvent motionEvent, int position);
    }

}
