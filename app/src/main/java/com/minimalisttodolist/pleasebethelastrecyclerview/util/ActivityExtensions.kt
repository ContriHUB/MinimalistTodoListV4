package com.minimalisttodolist.pleasebethelastrecyclerview.util

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.util.Log

// Change App Icon
fun lightIcon(context: Context) = setAppIcon(context, true)
fun darkIcon(context: Context) = setAppIcon(context, false)

fun Context.getActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}

fun setAppIcon(context: Context, isLight: Boolean) {
    context.getActivity()?.let { activity ->
        val (enabled, disabled) = if (isLight) {
            "${activity.packageName}.MainActivityAlias" to "${activity.packageName}.MainActivity"
        } else {
            "${activity.packageName}.MainActivity" to "${activity.packageName}.MainActivityAlias"
        }

        activity.packageManager.setComponentEnabledSetting(
            ComponentName(activity, enabled),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
        activity.packageManager.setComponentEnabledSetting(
            ComponentName(activity, disabled),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )

        Log.d("TAG", if (isLight) "light" else "dark")
    }
}
