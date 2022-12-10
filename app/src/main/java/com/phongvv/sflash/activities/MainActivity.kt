package com.phongvv.sflash.activities

import android.animation.LayoutTransition
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import com.google.android.material.slider.Slider
import com.phongvv.sflash.NoFlashlightDialog
import com.phongvv.sflash.R
import com.phongvv.sflash.databinding.MainActivityBinding
import com.phongvv.sflash.utils.CameraHelper
import com.phongvv.sflash.utils.Utils
import me.tankery.lib.circularseekbar.CircularSeekBar
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    private var brightness = -999
    private var windowSetting: Window? = null
    //kept for legacy reasons
    private lateinit var legacyPreferences : SharedPreferences
    private lateinit var defaultPreferences: SharedPreferences
    private lateinit var helper: CameraHelper
    private lateinit var binding: MainActivityBinding

    private enum class FlashlightMode {
        NORMAL, SOS, STROBOSCOPE
    }

    private val material3Listener =
        OnSharedPreferenceChangeListener { _: SharedPreferences?, key: String -> if ((key == "md3")) recreate() }

    override fun onCreate(savedInstanceState: Bundle?) {
        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false)
        defaultPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        defaultPreferences.registerOnSharedPreferenceChangeListener(material3Listener)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P && defaultPreferences.getString(
                "theme",
                "system"
            ) == "system"
        ) defaultPreferences.edit().putString("theme", "light").apply()
        Utils.applyThemeFromSettings(this)
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        helper = CameraHelper.getInstance(this)!!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && helper.getFlashlightStrengthLevel(
                this
            ) > 1 && defaultPreferences.getInt("flashlight_strength", -1) == -1
        ) { //if flash brightness is not saved into preferences
            CameraHelper.setFlashlightStrength(helper.getFlashlightStrengthLevel(this)) //then set brightness to max
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && helper.getFlashlightStrengthLevel(
                this
            ) > 1 && defaultPreferences.getInt("flashlight_strength", -1) != -1
        ) { //if flash brightness is saved into preferences
            CameraHelper.setFlashlightStrength(
                defaultPreferences.getInt(
                    "flashlight_strength",
                    -1
                )
            ) //then set brightness from there
        }
        setSupportActionBar(binding.toolbar)
        windowSetting = window
        legacyPreferences = getSharedPreferences("my_prefs", MODE_PRIVATE)

        applyListeners()
        init()

        if (savedInstanceState != null && legacyPreferences.getInt("default_option", 1) == 2) {
            brightness = savedInstanceState.getInt("brightness")
            val layoutpars = windowSetting?.attributes
            layoutpars?.screenBrightness = brightness.toFloat() / 100
            windowSetting?.attributes = layoutpars
        }
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            supportFragmentManager.setFragmentResultListener(NoFlashlightDialog.NO_FLASH_DIALOG_DISMISSED,
                this
            ) { _: String?, _: Bundle? -> binding.bgOptions.callOnClick() }
            binding.apply {
                buttonSOS.visibility = View.GONE
                imageSOS.visibility = View.GONE
                buttonStroboscope.visibility = View.GONE
                imageStroboscope.visibility = View.GONE
                textStroboscopeInterval.visibility = View.GONE
                sliderStroboscopeInterval.visibility = View.GONE
            }
        } else {
            CameraHelper.normalFlashStatus.observe(this
            ) { isOn: Boolean ->
                changeButtonColors(
                    FlashlightMode.NORMAL,
                    isOn
                )
            }
            CameraHelper.sosStatus.observe(this, (Observer { isOn: Boolean ->
                changeButtonColors(
                    FlashlightMode.SOS,
                    isOn
                )
            }))
            CameraHelper.stroboscopeStatus.observe(this, (Observer { isOn: Boolean ->
                changeButtonColors(
                    FlashlightMode.STROBOSCOPE,
                    isOn
                )
                binding.apply {
                    textStroboscopeInterval.visibility = if (isOn) View.VISIBLE else View.GONE
                    sliderStroboscopeInterval.visibility = if (isOn) View.VISIBLE else View.GONE
                }
            }))
            binding.apply {
                buttonSOS.setOnClickListener { helper.toggleSos(this@MainActivity) }
                buttonStroboscope.setOnClickListener { helper.toggleStroboscope(this@MainActivity) }
                val stroboscopeIntervalInPreferences =
                    defaultPreferences.getFloat("stroboscope_interval", -1f)
                CameraHelper.setStroboscopeInterval(if (stroboscopeIntervalInPreferences != -1f) (stroboscopeIntervalInPreferences * 1000).toInt() else 500)
                sliderStroboscopeInterval.value =
                    if (stroboscopeIntervalInPreferences != -1f) stroboscopeIntervalInPreferences else 0.5f
                sliderStroboscopeInterval.addOnSliderTouchListener(object :
                    Slider.OnSliderTouchListener {
                    override fun onStartTrackingTouch(slider: Slider) {}
                    override fun onStopTrackingTouch(slider: Slider) {
                        CameraHelper.setStroboscopeInterval((slider.value * 1000).toInt())
                    }
                })
            }
        }
    }

    override fun onPause() {
        super.onPause()
        defaultPreferences.edit()
            .putFloat("stroboscope_interval", binding.sliderStroboscopeInterval.value).apply()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.settings_menu_item) {
            startActivity(Intent(this, SettingsActivity::class.java))
            return true
        } else if (id == R.id.about_menu_item) {
            startActivity(Intent(this, AboutActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun applyListeners() {
        binding.bgOptions.setOnClickListener {
            val editor: SharedPreferences.Editor = legacyPreferences.edit()
            editor.putInt(
                "default_option",
                if (legacyPreferences.getInt("default_option", 1) == 1) 2 else 1
            )
            editor.apply()
            init()
        }
    }

    private fun init() {
        if (legacyPreferences.getInt("default_option", 1) == 1) {
            updateOptionsUI(true)
            refreshActivityForFlashLight()
        } else {
            updateOptionsUI(false)
            refreshActivityForScreenLight()
        }
    }

    private fun refreshActivityForFlashLight() {
        binding.apply {
            if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) NoFlashlightDialog().show(
                supportFragmentManager, null
            ) else if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH) && (
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) && (
                        helper.getFlashlightStrengthLevel(this@MainActivity) > 1)
            ) {
                progressCircular.apply {
                    progress = 0f
                    max = (helper.getFlashlightStrengthLevel(this@MainActivity) - 1).toFloat()
                    setOnSeekBarChangeListener(object :
                        CircularSeekBar.OnCircularSeekBarChangeListener {
                        override fun onProgressChanged(
                            circularSeekBar: CircularSeekBar?,
                            progress: Float,
                            fromUser: Boolean
                        ) {
                            CameraHelper.setFlashlightStrength((progress + 1).roundToInt())
                            if ((java.lang.Boolean.TRUE == CameraHelper.normalFlashStatus.value)) helper.turnOnFlashWithStrength(
                                this@MainActivity
                            )
                        }

                        override fun onStopTrackingTouch(seekBar: CircularSeekBar?) {
                            if (seekBar != null) defaultPreferences.edit()
                                .putInt("flashlight_strength", (seekBar.progress + 1).roundToInt())
                                .apply()
                        }

                        override fun onStartTrackingTouch(seekBar: CircularSeekBar?) {}
                    })
                    progress = (CameraHelper.getFlashlightStrength() - 1).toFloat()
                }
                buttonPower.setOnClickListener { helper.toggleNormalFlash(this@MainActivity) }
            } else if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                progressCircular.apply {
                    setOnSeekBarChangeListener(null)
                    progress = 0f
                    isEnabled = false
                    pointerColor = Color.parseColor("#AAAABB")
                }
                buttonPower.setOnClickListener { helper.toggleNormalFlash(this@MainActivity) }
            }
            rootLayout.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.transparent)) //transparent
        }
    }

    private fun changeButtonColors(mode: FlashlightMode, isTurnedOn: Boolean) {
        binding.apply {
            when (mode) {
                FlashlightMode.NORMAL -> {
                    buttonPower.setColorFilter(
                        if (isTurnedOn) Color.parseColor("#28FFB137") else Color.parseColor(
                            "#F3F3F7"
                        )
                    )
                    imagePowerIcon.setColorFilter(
                        if (isTurnedOn) Color.parseColor("#FFB137") else Color.parseColor(
                            "#AAAABB"
                        )
                    )
                }
                FlashlightMode.SOS -> {
                    buttonSOS.setColorFilter(
                        if (isTurnedOn) Color.parseColor("#28FFB137") else Color.parseColor(
                            "#F3F3F7"
                        )
                    )
                    imageSOS.setColorFilter(
                        if (isTurnedOn) Color.parseColor("#FFB137") else Color.parseColor(
                            "#AAAABB"
                        )
                    )
                }
                FlashlightMode.STROBOSCOPE -> {
                    buttonStroboscope.setColorFilter(
                        if (isTurnedOn) Color.parseColor("#28FFB137") else Color.parseColor(
                            "#F3F3F7"
                        )
                    )
                    imageStroboscope.setColorFilter(
                        if (isTurnedOn) Color.parseColor("#FFB137") else Color.parseColor(
                            "#AAAABB"
                        )
                    )
                }
            }
        }
    }

    private fun updateOptionsUI(isFlash: Boolean) {
        binding.apply {
            if (isFlash) {
                //Change UI for options
                val params = bgOptionsCircle.layoutParams as RelativeLayout.LayoutParams
                params.removeRule(RelativeLayout.ALIGN_PARENT_END)
                bgOptionsCircle.layoutParams = params
                imageFlash.setColorFilter(Color.parseColor("#FFB137"))
                imageScreen.setColorFilter(Color.parseColor("#AAAABB"))
                progressCircular.progress = 0f
            } else {
                bgOptionsCircle.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
                val params = bgOptionsCircle.layoutParams as RelativeLayout.LayoutParams
                params.addRule(RelativeLayout.ALIGN_PARENT_END)
                bgOptionsCircle.layoutParams = params
                imageFlash.setColorFilter(Color.parseColor("#AAAABB"))
                imageScreen.setColorFilter(Color.parseColor("#FFB137"))
            }
        }
    }

    private fun refreshActivityForScreenLight() {
        binding.apply {
            progressCircular.apply {
                pointerColor = Color.parseColor("#FFB137")
                isEnabled = true
            }
            if (defaultPreferences.getBoolean(
                    "no_flash_when_screen",
                    true
                ) && packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
            ) turnOff()
            rootLayout.setBackgroundColor(Color.parseColor("#FFFFFF")) //force set white, because it does not make sense for the app to be dark when using screen light
            if (progressCircular.progress > 0) {
                binding.progressCircular.apply {
                    setOnSeekBarChangeListener(null)
                    progress = 0f
                }
            }
            progressCircular.apply {
                max = 100f
                setOnSeekBarChangeListener(object :
                    CircularSeekBar.OnCircularSeekBarChangeListener {
                    override fun onProgressChanged(
                        circularSeekBar: CircularSeekBar?,
                        progress: Float,
                        fromUser: Boolean
                    ) {
                        brightness = if (progress != 0f) progress.toInt() else -1
                        val layoutpars = window!!.attributes
                        layoutpars.screenBrightness = brightness.toFloat() / 100
                        window!!.attributes = layoutpars
                    }

                    override fun onStopTrackingTouch(seekBar: CircularSeekBar?) {}
                    override fun onStartTrackingTouch(seekBar: CircularSeekBar?) {}
                })
            }
            buttonPower.setOnClickListener {
                progressCircular.progress = if (brightness != 100) 100f else 0f
            }
        }
    }

    private fun turnOff() {
        if ((java.lang.Boolean.TRUE == CameraHelper.normalFlashStatus.value)) {
            helper.toggleNormalFlash(this)
        }
        if ((java.lang.Boolean.TRUE == CameraHelper.sosStatus.value)) {
            helper.toggleSos(this)
        }
        if ((java.lang.Boolean.TRUE == CameraHelper.stroboscopeStatus.value)) {
            helper.toggleStroboscope(this)
        }
    }
}