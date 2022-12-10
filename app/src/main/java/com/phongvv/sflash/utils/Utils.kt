package com.phongvv.sflash.utils

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.hardware.Camera
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.phongvv.sflash.R
import com.phongvv.sflash.widgets.FlashlightWidgetProvider
import com.phongvv.sflash.widgets.SOSWidgetProvider

object Utils {
    fun getFlashOnParameter(camera: Camera): String {
        val flashModes = camera.parameters.supportedFlashModes
        if (flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
            return Camera.Parameters.FLASH_MODE_TORCH
        } else if (flashModes.contains(Camera.Parameters.FLASH_MODE_ON)) {
            return Camera.Parameters.FLASH_MODE_ON
        } else if (flashModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
            return Camera.Parameters.FLASH_MODE_AUTO
        }
        throw UnsupportedOperationException()
    }

    /**
     * Must be called after toggling flashlight to update the widget UI.
     * @see CameraHelper.toggleNormalFlash
     * @see CameraHelper.toggleSos
     * @see CameraHelper.toggleStroboscope
     */
    fun updateWidgets(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val flashlightWidgetIds = manager.getAppWidgetIds(
            ComponentName(
                context,
                FlashlightWidgetProvider::class.java
            )
        )
        if (flashlightWidgetIds.isNotEmpty()) {
            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, flashlightWidgetIds)
                .setClass(context, FlashlightWidgetProvider::class.java)
            context.sendBroadcast(intent)
        }
        val sosWidgetIds = manager.getAppWidgetIds(
            ComponentName(
                context,
                SOSWidgetProvider::class.java
            )
        )
        if (sosWidgetIds.isNotEmpty()) {
            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, sosWidgetIds)
                .setClass(context, SOSWidgetProvider::class.java)
            context.sendBroadcast(intent)
        }
    }

    fun applyThemeFromSettings(context: Context) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        when (preferences.getString("theme", "system")) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
        context.setTheme(
            if (preferences.getBoolean(
                    "md3",
                    false
                )
            ) R.style.AppTheme_Material3 else R.style.AppTheme
        )
    }
}