package com.txt.sl


/**
 * Created by JustinWjq
 * @date 2020/9/24.
 * description：
 */

public data class TxConfig(

    @JvmField var wxKey: String = "",

    @JvmField var miniprogramType  : TXSdk.Environment= TXSdk.Environment.TEST,

    @JvmField var  userName: String = "",

    @JvmField var  miniprogramTitle: String = "",

    @JvmField var  miniprogramDescription: String = "",

    @JvmField var  miniProgramPath: String = "/pages/index/index"


)

