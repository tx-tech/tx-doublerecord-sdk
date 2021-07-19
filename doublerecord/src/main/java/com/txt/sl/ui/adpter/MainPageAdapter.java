package com.txt.sl.ui.adpter;



import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 应用模块:
 * <p>
 * 类描述: FragmentPagerAdapter ：会留在内存中 用于更少的页面
 *
 *        FragmentStatePagerAdapter ：离开当前页面 会释放资源  多的页面
 * <p>
 *
 */
public class MainPageAdapter extends FragmentPagerAdapter {
    private List<Fragment> fragments ;
    public MainPageAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    public void setData(List<Fragment> data){
        if (fragments == null){
            fragments = new ArrayList<>();
        }
        fragments.addAll(data);
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (fragments != null && fragments.size() > 0){
            return fragments.get(position);
        }
        return null;
    }
    @Override
    public CharSequence getPageTitle(int position) {
        if (position==0){
            return "详情";
        }else{
            return "历史记录";
        }
    }
    @Override
    public int getCount() {
        if (fragments != null && fragments.size() > 0){
            return fragments.size();
        }
        return 0;
    }
}
