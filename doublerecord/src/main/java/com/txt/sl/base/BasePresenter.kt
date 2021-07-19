package com.txt.sl.base

import java.lang.ref.WeakReference

/**
 * Created by JustinWjq
 * @date 2020/6/5.
 * description：
 */
open class BasePresenter<V> {

    var mWeakRef: WeakReference<V?>? = null


    /**
     * 创建弱引用View
     *
     * @param view
     */
    open fun attachView(view: V?) {
        mWeakRef = WeakReference<V?>(view)
    }

    /**
     * 将View取出
     *
     * @return
     */
    protected open fun getView(): V? {
        return if (null == mWeakRef) null else mWeakRef!!.get()
    }

    /**
     * 判断是否使用弱引用创建View
     *
     * @return
     */
    protected open fun isViewAttached(): Boolean {
        return null != mWeakRef && null != mWeakRef!!.get()
    }

    /**
     * 释放View 并取消订阅
     */
    open fun detachView() {
        if (null != mWeakRef) {
            mWeakRef!!.clear()
            mWeakRef = null
        }
    }
}