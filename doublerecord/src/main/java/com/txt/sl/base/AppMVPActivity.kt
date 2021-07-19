package com.txt.sl.base

import com.common.widget.base.BaseActivity


/**
 * Created by JustinWjq
 * @date 2019-12-23.
 * description：
 */
abstract class AppMVPActivity<V, P : BasePresenter<V>> : BaseActivity() {
    public var mPresenter: P? = null




    /**
     * 创建Presenter
     * @return 返回当前Presenter
     */
    protected abstract fun createPresenter(): P?
    override fun initData() {
        super.initData()
        mPresenter = createPresenter()
        if (mPresenter != null) {
            mPresenter?.attachView(this as V)
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        mPresenter?.detachView()
    }

    override fun changeMobile() {

    }

    override fun changeNetNull() {

    }

    override fun changeWifi() {

    }



}