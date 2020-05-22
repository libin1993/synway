package com.doit.net.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseSectionQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.doit.net.Model.CacheManager;
import com.doit.net.Model.DBChannel;
import com.doit.net.Model.UCSIDBManager;
import com.doit.net.Utils.ToastUtils;
import com.doit.net.View.AddFcnDialog;
import com.doit.net.base.BaseActivity;
import com.doit.net.bean.LteChannelCfg;
import com.doit.net.bean.SectionBean;
import com.doit.net.ucsi.R;

import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Author：Libin on 2020/5/22 13:33
 * Email：1993911441@qq.com
 * Describe：自定义频点
 */
public class CustomFcnActivity extends BaseActivity {
    @BindView(R.id.rv_custom_fcn)
    RecyclerView rvCustomFcn;



    private BaseSectionQuickAdapter<SectionBean, BaseViewHolder> adapter;
    private List<SectionBean> dataList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_fcn);
        ButterKnife.bind(this);

        initView();
        initData();
    }

    private void initView() {
        rvCustomFcn.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BaseSectionQuickAdapter<SectionBean, BaseViewHolder>(R.layout.layout_fcn_item,
                R.layout.layout_fcn_header,dataList) {

            @Override
            protected void convert(BaseViewHolder helper, SectionBean item) {
                DBChannel channel = item.t;
                helper.setText(R.id.tv_fcn,"FCN:"+channel.getFcn());
                ImageView ivCheck = helper.getView(R.id.iv_select_fcn);
                LinearLayout llEdit  = helper.getView(R.id.ll_edit_fcn);
                LinearLayout llDelete = helper.getView(R.id.ll_delete_fcn);
                if (channel.isCheck() == 1){
                    ivCheck.setImageResource(R.mipmap.ic_fcn_checked);
                }else {
                    ivCheck.setImageResource(R.mipmap.ic_fcn_normal);
                }

                if (channel.isDefault() == 1){
                    llEdit.setVisibility(View.GONE);
                    llDelete.setVisibility(View.GONE);
                }else {
                    llEdit.setVisibility(View.VISIBLE);
                    llDelete.setVisibility(View.VISIBLE);
                }

                helper.addOnClickListener(R.id.iv_select_fcn);
                helper.addOnClickListener(R.id.ll_edit_fcn);
                helper.addOnClickListener(R.id.ll_delete_fcn);
            }

            @Override
            protected void convertHead(BaseViewHolder helper, SectionBean item) {
                String[] split = item.header.split(",");
                helper.setText(R.id.tv_band, "通道: "+split[0]+"    "+ "频段: " + split[1]);
                helper.addOnClickListener(R.id.iv_add_fcn);
            }


        };

        rvCustomFcn.setAdapter(adapter);

        adapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                switch (view.getId()){
                    case R.id.iv_add_fcn:
                        AddFcnDialog addFcnDialog = new AddFcnDialog(CustomFcnActivity.this,
                                "添加FCN", "", new AddFcnDialog.OnConfirmListener() {
                            @Override
                            public void onConfirm(String value) {
                                SectionBean sectionBean = dataList.get(position);
                                String[] split = sectionBean.header.split(",");
                                addFcn(position,split[0],split[1],value);
                            }
                        });
                        addFcnDialog.show();
                        break;
                    case R.id.iv_select_fcn:
                        SectionBean section = dataList.get(position);
                        if (section.t.isCheck() == 1){
                            return;
                        }
                        checkFcn(section.t.getBand(),section.t.getFcn());
                        break;
                    case R.id.ll_edit_fcn:
                        AddFcnDialog editFcnDialog = new AddFcnDialog(CustomFcnActivity.this,
                                "编辑FCN", dataList.get(position).t.getFcn(), new AddFcnDialog.OnConfirmListener() {
                            @Override
                            public void onConfirm(String value) {
                                SectionBean sectionBean = dataList.get(position);
                                update(position,sectionBean.t.getId(),sectionBean.t.getBand(),value);
                            }
                        });
                        editFcnDialog.show();
                        break;
                    case R.id.ll_delete_fcn:
                        SectionBean sectionBean = dataList.get(position);
                        deleteDcn(position,sectionBean.t.getId(),sectionBean.t.getBand(),sectionBean.t.isCheck());
                        break;
                }
            }
        });
    }

    /**
     * 设置默认fcn
     */
    private void checkFcn(String band,String fcn) {
        for (int i = 0; i < dataList.size(); i++) {
            SectionBean sectionBean = dataList.get(i);
            if (!sectionBean.isHeader && band.equals(sectionBean.t.getBand())){
                if (fcn.equals(sectionBean.t.getFcn())){
                    dataList.get(i).t.setCheck(1);
                }else {
                    dataList.get(i).t.setCheck(0);
                }
            }
        }

        adapter.notifyDataSetChanged();

        try {
            DbManager dbManager = UCSIDBManager.getDbManager();
            List<DBChannel> dbChannel = dbManager.selector(DBChannel.class)
                    .where("band", "=", band)
                    .findAll();
            for (DBChannel channel : dbChannel) {
                if (fcn.equals(channel.getFcn())){
                    channel.setCheck(1);
                }else {
                    channel.setCheck(0);
                }
                dbManager.update(dbChannel);
            }

        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param position
     * @param id
     * @param band
     * @param fcn
     * 编辑
     */
    private void update(int position,int id,String band,String fcn){
        for (int i = 0; i < dataList.size(); i++) {
            SectionBean sectionBean = dataList.get(i);
            if (!sectionBean.isHeader && band.equals(sectionBean.t.getBand()) && fcn.equals(sectionBean.t.getFcn())){
                ToastUtils.showMessage(CustomFcnActivity.this,"已存在相同FCN");
                return;
            }
        }

        dataList.get(position).t.setFcn(fcn);
        adapter.notifyItemChanged(position);

        try {
            DbManager dbManager = UCSIDBManager.getDbManager();
            DBChannel dbChannel = dbManager.selector(DBChannel.class)
                    .where("id", "=", id)
                    .findFirst();
            if (dbChannel !=null){
                dbChannel.setFcn(fcn);
                dbManager.update(dbChannel);
            }

        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除fcn
     */
    private void deleteDcn(int position,int id,String band,int isCheck) {
        //删除已选中的，重新设置默认
        if (isCheck == 1){
            for (int i = 0; i < dataList.size(); i++) {
                SectionBean sectionBean = dataList.get(i);
                if (!sectionBean.isHeader && band.equals(sectionBean.t.getBand()) ){
                  if (sectionBean.t.isDefault() == 1){
                      dataList.get(i).t.setCheck(1);
                  }else {
                      dataList.get(i).t.setCheck(0);
                  }
                }
            }
        }
        dataList.remove(position);
        adapter.notifyItemRemoved(position);
        adapter.notifyItemRangeChanged(position,dataList.size()-position);

        try {
            UCSIDBManager.getDbManager().deleteById(DBChannel.class,id);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param idx
     * @param band
     * @param fcn
     * 新增fcn
     */
    private void addFcn(int position,String idx, String band, String fcn) {
        int index = position; //插入的位置
        for (int i = position; i < dataList.size(); i++) {
            SectionBean sectionBean = dataList.get(i);
            if (!sectionBean.isHeader && band.equals(sectionBean.t.getBand()) && fcn.equals(sectionBean.t.getFcn())){
                ToastUtils.showMessage(CustomFcnActivity.this,"已存在相同FCN");
                return;
            }

            if (sectionBean.isHeader){
                String[] split = sectionBean.header.split(",");
                if (band.equals(split[1])){
                    index++;
                }
            }else {
                if (band.equals(sectionBean.t.getBand())){
                    index++;
                }
            }

        }
        DBChannel dbChannel = new DBChannel(idx,band,fcn,0,0);
        SectionBean section = new SectionBean(dbChannel);
        if (index == dataList.size()){
            dataList.add(section);
        }else {
            dataList.add(index,section);
        }

        adapter.notifyDataSetChanged();

        try {
            UCSIDBManager.getDbManager().save(dbChannel);
        } catch (DbException e) {
            e.printStackTrace();
        }

    }

    private void initData() {
        for (LteChannelCfg channel : CacheManager.channels) {
            dataList.add(new SectionBean(true,channel.getIdx()+","+channel.getBand()));
            try {
                DbManager dbManager = UCSIDBManager.getDbManager();
                List<DBChannel> channelList = dbManager.selector(DBChannel.class)
                        .where("band", "=", channel.getBand())
                        .findAll();
                for (DBChannel dbChannel : channelList) {
                    dataList.add(new SectionBean(dbChannel));
                }
            } catch (DbException e) {
                e.printStackTrace();
            }
        }

        adapter.notifyDataSetChanged();
    }

}