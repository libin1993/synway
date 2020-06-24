package com.doit.net.fragment;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.doit.net.adapter.UeidTabLayoutAdapter;
import com.doit.net.base.BaseFragment;
import com.doit.net.ucsi.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zxc on 2018/11/21.
 */

public class UeidFragment extends BaseFragment {

    private TabLayout tabLayout;
    private ViewPager viewPagers;
    private List<Fragment> listFragments;
    private List<String> listTitles;

    public UeidFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.layout_ueid_all, container, false);
        tabLayout = rootView.findViewById(R.id.tabLayoutTab);
        viewPagers = rootView.findViewById(R.id.vpViewPaper);
        initView();
        return rootView;
    }

    private void initView() {

        listTitles = new ArrayList<>();
        listFragments = new ArrayList<>();
//        if (VersionManage.isArmyVer()){
            listTitles.add("实时上报");
            listTitles.add("碰撞分析");
            listTitles.add("伴随分析");
            tabLayout.addTab(tabLayout.newTab().setText(listTitles.get(0)));
            tabLayout.addTab(tabLayout.newTab().setText(listTitles.get(1)));
            tabLayout.addTab(tabLayout.newTab().setText(listTitles.get(2)));

            listFragments.add(new RealTimeUeidRptFragment());
            listFragments.add(new CollideAnalysisFragment());
            listFragments.add(new GetPartnerAnalysisFragment());

            tabLayout.setTabMode(TabLayout.MODE_FIXED);
//        }else if(VersionManage.isPoliceVer()){
//            listTitles.add("实时上报");
//            //listTitles.add("中标记录");
//            listTitles.add("碰撞分析");
//            listTitles.add("伴随分析");
//            //listTitles.add("实时碰撞");
//
//            tabLayout.addTab(tabLayout.newTab().setText(listTitles.get(0)));
//            tabLayout.addTab(tabLayout.newTab().setText(listTitles.get(1)));
//            tabLayout.addTab(tabLayout.newTab().setText(listTitles.get(2)));
//            //tabLayout.addTab(tabLayout.newTab().setText(listTitles.get(3)));
//            //tabLayout.addTab(tabLayout.newTab().setText(listTitles.get(4)));
//
//            listFragments.add(new RealTimeUeidRptFragment());
//           // listFragments.add(new RealtimeNamelistRptFragment());
//            listFragments.add(new CollideAnalysisFragment());
//            listFragments.add(new GetPartnerAnalysisFragment());
//            //listFragments.add(new RealTimeAnalysisFragment());
//
//            tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
//        }

        viewPagers.setOffscreenPageLimit(listTitles.size());
        viewPagers.setAdapter(new UeidTabLayoutAdapter(getChildFragmentManager(), listFragments, listTitles));
        tabLayout.setupWithViewPager(viewPagers);

        //绑定自定义text，设置默认第一个标签放大
        TabLayout.Tab tab;
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            tab = tabLayout.getTabAt(i);
            if (tab != null) {
                tab.setCustomView(getTabView(i));
            }
        }
        tab = tabLayout.getTabAt(0);
        if (tab != null  && tab.getCustomView() instanceof TextView) {
            ((TextView) tab.getCustomView()).setTextSize(22);
            ((TextView) tab.getCustomView()).setTextColor(getResources().getColor(R.color.black));
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //viewPagers.setCurrentItem(tab.getPosition());
                View view = tab.getCustomView();
                if (null != view && view instanceof TextView) {
                    ((TextView) view).setTextSize(22);
                    ((TextView) view).setTextColor(getResources().getColor(R.color.black));
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View view = tab.getCustomView();
                if (null != view && view instanceof TextView) {
                    ((TextView) view).setTextSize(15);
                    ((TextView) view).setTextColor(getResources().getColor(R.color.white));
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

//    @Override
//    public void onFocus() {
//        listFragments.get(0).onResume();  //刷新实时上报界面功率开关
//
//    }

    private View getTabView(int currentPosition) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.comman_text, null);
        TextView textView = view.findViewById(R.id.tvCommanText);
        textView.setText(listTitles.get(currentPosition));
        return view;
    }
}
