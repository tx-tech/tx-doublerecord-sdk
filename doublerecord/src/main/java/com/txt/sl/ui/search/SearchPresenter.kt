package com.txt.sl.ui.search

import android.app.Activity
import com.txt.sl.base.BasePresenter

/**
 * Created by Justin on 2018/5/22/022.
 * email：WjqJustin@163.com
 * effect：search specific work list
 */

private val TAG = SearchPresenter::class.java.simpleName

class SearchPresenter(val mContext: Activity,val view:SearchContract.View ) : BasePresenter<SearchContract.View>(),SearchContract.Presenter {


    //搜索请求数据
    override fun requestData(data: String) {


    }

}