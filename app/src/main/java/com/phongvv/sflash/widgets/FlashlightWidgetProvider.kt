package com.phongvv.sflash.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.RemoteViews
import com.phongvv.sflash.R
import com.phongvv.sflash.activities.MainActivity
import com.phongvv.sflash.utils.CameraHelper
import kotlin.IntArray

class FlashlightWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            val remoteViews = RemoteViews(context.packageName, R.layout.widget_flashlight)
            if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                remoteViews.setImageViewResource(R.id.img, R.drawable.flash_off)
                val intent = Intent(context, MainActivity::class.java)
                val pendingIntent =
                    PendingIntent.getActivity(context, 42, intent, PendingIntent.FLAG_IMMUTABLE)
                remoteViews.setOnClickPendingIntent(R.id.img, pendingIntent)
            } else {
                if (CameraHelper.normalFlashStatus.value == true) {
                    remoteViews.setImageViewResource(R.id.img, R.drawable.flashlight_on)
                } else remoteViews.setImageViewResource(R.id.img, R.drawable.flashlight_off)
                val intent = Intent(context, FlashlightWidgetProvider::class.java)
                    .setAction(ACTION_TOGGLE)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    69,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                remoteViews.setOnClickPendingIntent(R.id.img, pendingIntent)
            }
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_TOGGLE) {
            val helper: CameraHelper? = CameraHelper.getInstance(context)
            helper?.toggleNormalFlash(context)
            val manager = AppWidgetManager.getInstance(context)
            onUpdate(
                context,
                manager,
                manager.getAppWidgetIds(
                    ComponentName(
                        context,
                        FlashlightWidgetProvider::class.java
                    )
                )
            )
        }
    }

    companion object {
        private const val ACTION_TOGGLE = "com.phongvv.sflash.TOGGLE_FLASH"
    }
}