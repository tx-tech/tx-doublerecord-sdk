package com.txt.sl.ui.home;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;


/**
 * 项目名称 ：AvictcJQB
 * 描述   ：产品列表适配器
 * 创建
 * 人  ：N.Sun
 * 创建时间 ：2017/5/12
 */
public class ApplyHomeFragmentAdapter extends FragmentPagerAdapter {

    private Context mContext;
    private List<PagerBean> mApplyStatusParams;

    public ApplyHomeFragmentAdapter(Context context, FragmentManager fm, List<PagerBean> applyStatusParams) {
        super(fm);
        this.mContext = context;
        this.mApplyStatusParams = applyStatusParams;
    }

    @Override
    public Fragment getItem(int position) {
        return TravelApplyHomeItemListFragment.newInstance(mApplyStatusParams.get(position).status);
    }

    @Override
    public CharSequence getPageTitle(int position) {
      return mApplyStatusParams.get(position).title;
    }

    @Override
    public int getCount() {
        return mApplyStatusParams.size();
    }
}
