package com.doit.net.View;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.doit.net.Utils.UtilBaseLog;

import java.util.List;

/**
 * Created by Zxc on 2018/11/21.
 */

public class UeidTabLayoutAdapter extends FragmentPagerAdapter{
    //添加fragment的集合
    private List<Fragment> listFragments;
    //添加标题的集合
    private List<String> listTiltes;

    public UeidTabLayoutAdapter(FragmentManager fm, List<Fragment> fragmentList, List<String> tilteLis) {
        super(fm);
        listFragments = fragmentList;
        listTiltes = tilteLis;
    }

    @Override
    public Fragment getItem(int position) {

        return listFragments.get(position);
    }

    @Override
    public int getCount() {
        return listFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return listTiltes.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
    }

    public UeidTabLayoutAdapter(FragmentManager fm) {
        super(fm);
    }
}
