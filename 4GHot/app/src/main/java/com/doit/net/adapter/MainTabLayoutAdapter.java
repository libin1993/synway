package com.doit.net.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.view.ViewGroup;

import com.doit.net.base.BaseFragment;
import com.doit.net.fragment.UeidFragment;

import java.util.List;

/**
 * Author：Libin on 2020/6/2 14:32
 * Email：1993911441@qq.com
 * Describe：
 */
public class MainTabLayoutAdapter extends FragmentPagerAdapter {
    private List<BaseFragment> mList;
    private List<String> mTitles;
    private FragmentManager fm;
    private boolean exchangeFragment; //是否替换第一个fragment

    public MainTabLayoutAdapter(FragmentManager fm, List<BaseFragment> list, List<String> titles) {
        super(fm);
        this.mList = list;
        this.mTitles = titles;
        this.fm = fm;
    }

    @Override
    public Fragment getItem(int position) {
        return mList.get(position);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles == null ? super.getPageTitle(position) : mTitles.get(position);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        String fragmentTag = fragment.getTag();
        if (position == 0 && exchangeFragment) {
            exchangeFragment = false;
            FragmentTransaction ft = fm.beginTransaction();
            ft.remove(fragment);
            fragment = mList.get(position % mList.size());
            //添加新fragment时必须用前面获得的tag，这点很重要
            ft.add(container.getId(), fragment, fragmentTag == null ? fragment.getClass().getName() + position : fragmentTag);
            ft.attach(fragment);
            ft.commitAllowingStateLoss();

        } else {
            fragment = mList.get(position);

        }
        return fragment;
    }

    /**
     * 替换第一个Fragment
     */
    public void exchangeFragment() {
        exchangeFragment = true;
        notifyDataSetChanged();
    }
}
