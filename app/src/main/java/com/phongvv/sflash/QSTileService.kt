package com.phongvv.sflash

import android.content.pm.PackageManager
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.phongvv.sflash.utils.CameraHelper
import com.phongvv.sflash.utils.CameraHelper.Companion.getInstance

@RequiresApi(api = Build.VERSION_CODES.N)
class QSTileService : TileService() {
    private var helper: CameraHelper? = null
    override fun onCreate() {
        super.onCreate()
        helper = getInstance(this)
    }

    override fun onStartListening() {
        super.onStartListening()
        qsTile.state = if (CameraHelper.normalFlashStatus.value == true) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        qsTile.updateTile()
    }

    override fun onTileAdded() {
        super.onTileAdded()
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            qsTile.state = Tile.STATE_UNAVAILABLE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                qsTile.subtitle = getString(R.string.no_camera)
            }
        } else if (CameraHelper.normalFlashStatus.value == true) {
            qsTile.state = Tile.STATE_ACTIVE
        }
        qsTile.updateTile()
    }

    override fun onTileRemoved() {
        super.onTileRemoved()
        if (CameraHelper.normalFlashStatus.value == true) {
            helper?.toggleNormalFlash(this)
        }
    }

    override fun onClick() {
        super.onClick()
        helper?.toggleNormalFlash(this)
        qsTile.state = if (CameraHelper.normalFlashStatus.value == true
        ) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        qsTile.updateTile()
    }
}