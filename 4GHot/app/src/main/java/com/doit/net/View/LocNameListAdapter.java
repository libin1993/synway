package com.doit.net.View;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.doit.net.Model.CacheManager;
import com.doit.net.ucsi.R;

/**
 * Created by wiker on 2016/4/29.
 */
public class LocNameListAdapter extends BaseSwipeAdapter {

    private LayoutInflater layoutInflater;

    private Context mContext;
    public LocNameListAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return CacheManager.locations.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View generateView(int position, ViewGroup parent) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.doit_layout_loc_namelist, null);
        return v;
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }

    class DeleteNameListener implements View.OnClickListener{
        private int position;

        public DeleteNameListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
//            CacheManager.removeBlackList(position);
            notifyDataSetChanged();
        }
    }

    @Override
    public void fillValues(int position, View convertView) {
        TextView t = (TextView)convertView.findViewById(R.id.position);
        t.setText((position + 1) + ".");

//        TextView ueidContent = (TextView)convertView.findViewById(R.id.ueidContent);
//        DBBlackInfo info = CacheManager.blackList.get(position);
//        String name = "";
//        if(!StringUtils.isBlank(info.getAccount())){
//            name = mContext.getString(R.string.lab_name)+info.getAccount()+", ";
//        }
//        String conn = "";
//        if(info.isConn()){
//            conn = "\n"+mContext.getString(R.string.user_has_conn);
//        }
//        ueidContent.setText(name+info.getImsi()+conn);
//        ueidContent.setTag(position);
//        convertView.findViewById(R.id.delete).setOnClickListener(new DeleteNameListener(position));
    }

}
