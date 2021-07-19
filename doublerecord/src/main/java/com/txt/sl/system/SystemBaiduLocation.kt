package com.txt.sl.receive

import android.annotation.SuppressLint
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.Poi
import com.txt.sl.TXSdk
import com.txt.sl.entity.bean.LocationInfo
import com.txt.sl.system.SystemCommon
import com.txt.sl.system.SystemSocket
import com.txt.sl.utils.LogUtils


/**
 * 获取地理位置
 */

class SystemBaiduLocation {
    private val TAG = SystemBaiduLocation::class.java.simpleName
    private var mInfo: LocationInfo? = null

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var singleton: SystemBaiduLocation? = null

        @JvmStatic
        val instance: SystemBaiduLocation?
            get() {
                if (singleton == null) {
                    synchronized(SystemBaiduLocation::class.java) {
                        if (singleton == null) {
                            singleton =
                                    SystemBaiduLocation()
                        }
                    }
                }
                return singleton
            }
    }

    public fun getLocationInfo(): LocationInfo? {
        return mInfo
    }



    fun instantiation() {
        if (mInfo == null)
            mInfo = LocationInfo()
    }

    private var locationService: LocationService? = null
    fun initLocation() {
        locationService = LocationService(TXSdk.getInstance().application)
        //获取locationservice实例，建议应用中只初始化1个location实例，然后使用，可以参考其他示例的activity，都是通过此种方式获取locationservice实例的
        locationService!!.registerListener(mListener)
        locationService!!.setLocationOption(locationService!!.defaultLocationClientOption)

    }

    fun startLocationService() {
        if (null != locationService) {

        }else{
            initLocation()
        }

        locationService?.start()
    }

    fun requestLocation() {
        locationService?.requestLocation()
    }

    fun stopLocationService() {
        locationService?.stop()
        locationService?.unregisterListener(mListener)
        locationService = null
    }

    private val mListener = object : BDAbstractLocationListener() {

        override fun onReceiveLocation(location: BDLocation?) {
            if (null != location && location.locType != BDLocation.TypeServerError) {
                val sb = StringBuffer(256)
                sb.append("time : ")
                /**
                 * 时间也可以使用systemClock.elapsedRealtime()方法 获取的是自从开机以来，每次回调的时间；
                 * location.getTime() 是指服务端出本次结果的时间，如果位置不发生变化，则时间不变
                 */
                sb.append(location.time)
                sb.append("\nlocType : ")// 定位类型
                sb.append(location.locType)
                sb.append("\nlocType description : ")// *****对应的定位类型说明*****
                sb.append(location.locTypeDescription)
                sb.append("\nlatitude : ")// 纬度
                sb.append(location.latitude)
                sb.append("\nlontitude : ")// 经度
                sb.append(location.longitude)
                sb.append("\nradius : ")// 半径
                sb.append(location.radius)
                sb.append("\nCountryCode : ")// 国家码
                sb.append(location.countryCode)
                sb.append("\nCountry : ")// 国家名称
                sb.append(location.country)
                sb.append("\ncitycode : ")// 城市编码
                sb.append(location.cityCode)
                sb.append("\ncity : ")// 城市
                sb.append(location.city)
                sb.append("\nDistrict : ")// 区
                sb.append(location.district)
                sb.append("\nStreet : ")// 街道
                sb.append(location.street)
                sb.append("\naddr : ")// 地址信息
                sb.append(location.addrStr)
                sb.append("\nUserIndoorState: ")// *****返回用户室内外判断结果*****
                sb.append(location.userIndoorState)
                sb.append("\nDirection(not all devices have value): ")
                sb.append(location.direction)// 方向
                sb.append("\nlocationdescribe: ")
                sb.append(location.locationDescribe)// 位置语义化信息
                sb.append("\nPoi: ")// POI信息
                if (location.poiList != null && !location.poiList.isEmpty()) {
                    for (i in 0 until location.poiList.size) {
                        val poi = location.poiList[i] as Poi
                        sb.append(poi.name + ";")
                    }
                }
                if (location.locType == BDLocation.TypeGpsLocation) {// GPS定位结果
                    sb.append("\nspeed : ")
                    sb.append(location.speed)// 速度 单位：km/h
                    sb.append("\nsatellite : ")
                    sb.append(location.satelliteNumber)// 卫星数目
                    sb.append("\nheight : ")
                    sb.append(location.altitude)// 海拔高度 单位：米
                    sb.append("\ngps status : ")
                    sb.append(location.gpsAccuracyStatus)// *****gps质量判断*****
                    sb.append("\ndescribe : ")
                    sb.append("gps定位成功")
                } else if (location.locType == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                    // 运营商信息
                    if (location.hasAltitude()) {// *****如果有海拔高度*****
                        sb.append("\nheight : ")
                        sb.append(location.altitude)// 单位：米
                    }
                    sb.append("\noperationers : ")// 运营商信息
                    sb.append(location.operators)
                    sb.append("\ndescribe : ")
                    sb.append("网络定位成功")
                } else if (location.locType == BDLocation.TypeOffLineLocation) {// 离线定位结果
                    sb.append("\ndescribe : ")
                    sb.append("离线定位成功，离线定位结果也是有效的")
                } else if (location.locType == BDLocation.TypeServerError) {
                    sb.append("\ndescribe : ")
                    sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因")
                } else if (location.locType == BDLocation.TypeNetWorkException) {
                    sb.append("\ndescribe : ")
                    sb.append("网络不同导致定位失败，请检查网络是否通畅")
                } else if (location.locType == BDLocation.TypeCriteriaException) {
                    sb.append("\ndescribe : ")
                    sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机")
                }

                LogUtils.i("百度地图", "onReceiveLocation: " + sb.toString())
                if (location.addrStr != null) {
                    instantiation()
                    mInfo?.apply {
                        latitude = location.latitude.toString() + ""
                        longitude = location.longitude.toString() + ""
                        address = location.addrStr
                        city = location.city
                        province = location.district
                    }

                }

            }


        }

    }

}
