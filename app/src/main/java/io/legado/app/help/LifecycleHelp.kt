package io.legado.app.help

import android.app.Activity
import android.app.Application
import android.os.Bundle
import io.legado.app.base.BaseService
import io.legado.app.utils.LanguageUtils
import java.lang.ref.WeakReference
import java.util.*

/**
 * Activity管理器,管理项目中Activity的状态
 */
@Suppress("unused")
object LifecycleHelp : Application.ActivityLifecycleCallbacks {

    private val activities: MutableList<WeakReference<Activity>> = arrayListOf()
    private val services: MutableList<WeakReference<BaseService>> = arrayListOf()
    private var finishedListener: (() -> Unit)? = null

    fun activitySize(): Int {
        return activities.size
    }

    /**
     * 判断指定Activity是否存在
     */
    fun isExistActivity(activityClass: Class<*>): Boolean {
        activities.forEach { item ->
            if (item.get()?.javaClass == activityClass) {
                return true
            }
        }
        return false
    }

    /**
     * 关闭指定 activity(class)
     */
    fun finishActivity(vararg activityClasses: Class<*>) {
        val waitFinish = ArrayList<WeakReference<Activity>>()
        for (temp in activities) {
            for (activityClass in activityClasses) {
                if (temp.get()?.javaClass == activityClass) {
                    waitFinish.add(temp)
                    break
                }
            }
        }
        waitFinish.forEach {
            it.get()?.finish()
        }
    }

    fun setOnFinishedListener(finishedListener: (() -> Unit)) {
        this.finishedListener = finishedListener
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityStarted(activity: Activity) {

    }

    override fun onActivityDestroyed(activity: Activity) {
        for (temp in activities) {
            if (temp.get() != null && temp.get() === activity) {
                activities.remove(temp)
                if (services.size == 0 && activities.size == 0) {
                    onFinished()
                }
                break
            }
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        activities.add(WeakReference(activity))
        if (!LanguageUtils.isSameWithSetting(activity)) {
            LanguageUtils.setConfiguration(activity)
        }
    }

    @Synchronized
    fun onServiceCreate(service: BaseService) {
        services.add(WeakReference(service))
    }

    @Synchronized
    fun onServiceDestroy(service: BaseService) {
        for (temp in services) {
            if (temp.get() != null && temp.get() === service) {
                services.remove(temp)
                if (services.size == 0 && activities.size == 0) {
                    onFinished()
                }
                break
            }
        }
    }

    private fun onFinished() {
        finishedListener?.invoke()
    }
}