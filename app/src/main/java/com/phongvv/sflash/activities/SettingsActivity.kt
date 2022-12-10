package com.phongvv.sflash.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.*
import com.phongvv.sflash.R
import com.phongvv.sflash.databinding.SettingsActivityBinding
import com.phongvv.sflash.utils.CameraHelper
import com.phongvv.sflash.utils.CameraHelper.Companion.getInstance
import com.phongvv.sflash.utils.Utils.applyThemeFromSettings

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        applyThemeFromSettings(this)
        super.onCreate(savedInstanceState)
        val binding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        setSupportActionBar(binding.toolbarSettings)
        if (supportActionBar != null) supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        private var helper: CameraHelper? = null
        private val listener =
            OnSharedPreferenceChangeListener { sharedPreferences: SharedPreferences, key: String ->
                if (key == "words_per_min" && sharedPreferences.getString(
                        "farnsworth_unit",
                        "0"
                    )!!
                        .toInt() <= helper?.getCurrentDitLength(sharedPreferences)!! ||
                    key == "farnsworth_unit" && sharedPreferences.getString(
                        "farnsworth_unit",
                        ""
                    )?.isEmpty() == true
                ) {
                    val ditLength = helper?.getCurrentDitLength(sharedPreferences)
                    val farnsworthUnitLength =
                        findPreference<EditTextPreference>("farnsworth_unit")!!
                    if (ditLength != null) {
                        sharedPreferences.edit()
                            .putString("farnsworth_unit", (ditLength + ditLength / 4).toString())
                            .apply()
                        farnsworthUnitLength.summary = (ditLength + ditLength / 4).toString()
                        farnsworthUnitLength.text = (ditLength + ditLength / 4).toString()
                    }
                }
            }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            val preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
            preferences.registerOnSharedPreferenceChangeListener(listener)
            helper = getInstance(requireContext())
            helper?.toggleSos(requireContext())
            val themePref = findPreference<ListPreference>("theme")!!
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                themePref.setEntries(R.array.theme_entries_p)
                themePref.setEntryValues(R.array.theme_values_p)
            }
            themePref.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _: Preference?, _: Any? ->
                    requireActivity().recreate()
                    true
                }
            val md3Pref = findPreference<SwitchPreferenceCompat>("md3")!!
            md3Pref.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _: Preference?, _: Any? ->
                    requireActivity().recreate()
                    true
                }
            val wordsPerMin = findPreference<EditTextPreference>("words_per_min")!!
            val useFarnsworth = findPreference<SwitchPreferenceCompat>("use_farnsworth")
            val farnsworthUnitLength = findPreference<EditTextPreference>("farnsworth_unit")
            assert(useFarnsworth != null)
            assert(farnsworthUnitLength != null)
            val learnMoreAboutMorseTiming = findPreference<Preference>("learn_more_morse_timing")!!
            val noFlashWhenScreen = findPreference<SwitchPreferenceCompat>("no_flash_when_screen")!!
            if (!requireContext().packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                wordsPerMin.isVisible = false
                useFarnsworth?.isVisible = false
                farnsworthUnitLength?.isVisible = false
                noFlashWhenScreen.isVisible = false
                learnMoreAboutMorseTiming.isVisible = false
            } else {
                wordsPerMin.setOnBindEditTextListener { editText: EditText ->
                    editText.inputType = InputType.TYPE_CLASS_NUMBER
                }
                wordsPerMin.onPreferenceChangeListener =
                    Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
                        if (newValue.toString().isEmpty()) {
                            Toast.makeText(
                                requireContext(),
                                R.string.words_per_min_error,
                                Toast.LENGTH_SHORT
                            ).show()
                            return@OnPreferenceChangeListener false
                        }
                        val newNum = newValue.toString().toInt()
                        if (newNum == 0) {
                            Toast.makeText(
                                requireContext(),
                                R.string.words_per_min_error,
                                Toast.LENGTH_SHORT
                            ).show()
                            return@OnPreferenceChangeListener false
                        }
                        true
                    }
                useFarnsworth?.onPreferenceChangeListener =
                    Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
                        farnsworthUnitLength?.isVisible = newValue as Boolean
                        true
                    }
                if (preferences.getString("farnsworth_unit", "")!!
                        .isEmpty()
                ) { //initialize default value
                    val ditLength = helper?.getCurrentDitLength(requireContext())
                    if (ditLength != null) {
                        preferences.edit()
                            .putString("farnsworth_unit", (ditLength + ditLength / 4).toString())
                            .apply()
                    farnsworthUnitLength?.summary = (ditLength + ditLength / 4).toString()
                    farnsworthUnitLength?.text = (ditLength + ditLength / 4).toString()
                    }
                } else farnsworthUnitLength?.summary = preferences.getString("farnsworth_unit", "")
                farnsworthUnitLength?.isVisible = preferences.getBoolean("use_farnsworth", false)
                farnsworthUnitLength?.setOnBindEditTextListener { editText: EditText ->
                    editText.inputType = InputType.TYPE_CLASS_NUMBER
                }
                farnsworthUnitLength?.onPreferenceClickListener =
                    Preference.OnPreferenceClickListener {
                        farnsworthUnitLength?.dialogMessage = getString(
                            R.string.farnsworth_unit_length_explanation,
                            helper?.getCurrentDitLength(requireContext())
                        )
                        true
                    }
                farnsworthUnitLength?.onPreferenceChangeListener =
                    Preference.OnPreferenceChangeListener { preference: Preference, newValue: Any ->
                        if (newValue.toString().isNotEmpty()) {
                            val newLength = newValue.toString().toInt()
                            if (newLength <= helper?.getCurrentDitLength(requireContext())!!) {
                                Toast.makeText(
                                    requireContext(),
                                    R.string.farnsworth_unit_length_error,
                                    Toast.LENGTH_LONG
                                ).show()
                                return@OnPreferenceChangeListener false
                            }
                            preference.summary = newLength.toString()
                        }
                        true
                    }
                learnMoreAboutMorseTiming.onPreferenceClickListener =
                    Preference.OnPreferenceClickListener {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW)
                                .setData(Uri.parse("https://morsecode.world/international/timing.html"))
                            startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(
                                requireContext(),
                                R.string.no_app_can_handle,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        true
                    }
            }
        }
    }
}