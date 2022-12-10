package com.phongvv.sflash.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.phongvv.sflash.R
import com.phongvv.sflash.databinding.ActivityGrantSystemWritePermissionBinding
import com.phongvv.sflash.utils.Utils.applyThemeFromSettings

class GrantSystemWritePermissionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGrantSystemWritePermissionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        applyThemeFromSettings(this)
        super.onCreate(savedInstanceState)
        binding = ActivityGrantSystemWritePermissionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (Settings.System.canWrite(this)) completeWidgetSetup()
    }

    override fun onResume() {
        super.onResume()
        if (!Settings.System.canWrite(this)) {
            binding.noticeGrantPermission.setText(R.string.system_settings_permission_notice)
            binding.buttonGrantPermission.setOnClickListener {
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                    .setData(Uri.parse("package:$packageName"))
                startActivity(intent)
            }
        } else
            binding.noticeGrantPermission.setText(R.string.system_settings_permission_granted)
        binding.buttonGrantPermission.isEnabled = false
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (!Settings.System.canWrite(this)) {
            setResult(RESULT_CANCELED)
            finish()
        } else
            completeWidgetSetup()
    }

    private fun completeWidgetSetup() {
        setResult(RESULT_OK)
        finish()
    }
}