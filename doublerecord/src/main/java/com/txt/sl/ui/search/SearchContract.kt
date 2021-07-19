package com.txt.sl.ui.search

import java.util.*

/**
 * Created by Justin on 2018/5/22/022.
 * email：WjqJustin@163.com
 * effect：
 */
interface SearchContract {

    interface View {

        fun showNoDataLayout()

        fun showErrorLayout()

        fun refreshData(mListData: ArrayList<String>? = null)

    }

    interface Presenter {

        fun requestData(data: String)

    }


}