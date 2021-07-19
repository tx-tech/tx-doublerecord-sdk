package com.txt.sl.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.View
import java.util.regex.Pattern

/**
 * Created by JustinWjq
 * @date 2019-08-20.
 * description：检测null
 */

fun String.isNotNull(): Boolean {
    if (!TextUtils.isEmpty(this) && this != "null")
        return true
    return false
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun String.nameMatches(): Boolean = Pattern.matches("[\\u4e00-\\u9fa5·]{2,20}", this)

fun String.phoneNumMatches(): Boolean = Pattern.matches("^((13[0-9])|(14[5,7])|(15[0-3,5-9])|(16[6])|(17[0,1,3,5-8])|(18[0-9])|(19[1,8,9]))\\d{8}$", this)

/**
 * startActivity()
 */
public inline fun <reified T : Activity> Context.startActivity(
        vararg params: Pair<String, String>) {
    val intent = Intent(this, T::class.java)
    params.forEach { intent.putExtra(it.first, it.second) }
    startActivity(intent)
}
