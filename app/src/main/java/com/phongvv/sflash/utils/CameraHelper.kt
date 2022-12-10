package com.phongvv.sflash.utils

import android.content.Context
import android.content.SharedPreferences
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.phongvv.sflash.R
import java.io.IOException
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.roundToInt

class CameraHelper private constructor(context: Context) {
    private var manager: CameraManager? = null
    private var camera: Camera? = null
    private val sosReference = listOf(
        250,
        250,
        250,
        250,
        250,
        750,
        750,
        250,
        750,
        250,
        750,
        750,
        250,
        250,
        250,
        250,
        250,
        1750
    )
    private val actualSos: MutableList<Int> = ArrayList()
    private var isStroboscopeFlashOn = false

    init {
        manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    fun turnOnFlashWithStrength(context: Context?) {
        try {
            manager?.turnOnTorchWithStrengthLevel(
                manager!!.cameraIdList[0],
                flashlightStrength.get()
            )
        } catch (e: CameraAccessException) {
            Toast.makeText(context, R.string.cannot_access_camera, Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    fun toggleNormalFlash(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                applyFlashlightStrengthFromSettings(context)
                toggleNormalFlashWithStrength()
            } catch (e: CameraAccessException) {
                Toast.makeText(context, R.string.cannot_access_camera, Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        } else
            try {
                toggleNormalFlashMarshmallow()
            } catch (e: CameraAccessException) {
                Toast.makeText(context, R.string.cannot_access_camera, Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        Utils.updateWidgets(context)
    }

    fun toggleSos(context: Context) {
        applySosLengthsFromSettings(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                applyFlashlightStrengthFromSettings(context)
                toggleSosWithStrength()
            } catch (e: CameraAccessException) {
                Toast.makeText(context, R.string.cannot_access_camera, Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        } else
            try {
                toggleSosMarshmallow()
            } catch (e: CameraAccessException) {
                Toast.makeText(context, R.string.cannot_access_camera, Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        Utils.updateWidgets(context)
    }

    fun toggleStroboscope(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                applyFlashlightStrengthFromSettings(context)
                toggleStroboscopeModeWithStrength()
            } catch (e: CameraAccessException) {
                Toast.makeText(context, R.string.cannot_access_camera, Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        } else
            try {
                toggleStroboscopeModeMarshmallow()
            } catch (e: CameraAccessException) {
                Toast.makeText(context, R.string.cannot_access_camera, Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        Utils.updateWidgets(context)
    }

    private fun toggleNormalFlashLollipop() {
        if (java.lang.Boolean.TRUE == isSosOn.value) {
            isSosOn.value = false
            while (isStroboscopeFlashOn) doNothing()
        }
        if (java.lang.Boolean.TRUE == isStroboscopeOn.value) {
            isStroboscopeOn.value = false
            while (isStroboscopeFlashOn) doNothing()
        }
        if (java.lang.Boolean.TRUE == isNormalFlashOn.value) {
            val parameters = camera?.parameters
            parameters?.flashMode = Camera.Parameters.FLASH_MODE_OFF
            camera?.parameters = parameters
            isNormalFlashOn.setValue(false)
        } else try {
            val parameters = camera?.parameters
            parameters?.flashMode = camera?.let { Utils.getFlashOnParameter(it) }
            camera?.parameters = parameters
            camera?.setPreviewTexture(SurfaceTexture(0))
            camera?.startPreview()
            camera?.autoFocus { _: Boolean, _: Camera? -> }
            isNormalFlashOn.setValue(true)
        } catch (ignored: IOException) {
        }
    }

    private fun toggleNormalFlashMarshmallow() {
        if (java.lang.Boolean.TRUE == isSosOn.value) {
            isSosOn.value = false
            while (isStroboscopeFlashOn) doNothing()
        }
        if (java.lang.Boolean.TRUE == isStroboscopeOn.value) {
            isStroboscopeOn.value = false
            while (isStroboscopeFlashOn) doNothing()
        }
        if (java.lang.Boolean.TRUE == isNormalFlashOn.value) {
            manager?.setTorchMode(manager!!.cameraIdList[0], false)
            isNormalFlashOn.setValue(false)
        } else {
            manager?.setTorchMode(manager!!.cameraIdList[0], true)
            isNormalFlashOn.setValue(true)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun toggleNormalFlashWithStrength() {
        if (java.lang.Boolean.TRUE == isSosOn.value) {
            isSosOn.value = false
            while (isStroboscopeFlashOn) doNothing()
        }
        if (java.lang.Boolean.TRUE == isStroboscopeOn.value) {
            isStroboscopeOn.value = false
            while (isStroboscopeFlashOn) doNothing()
        }
        if (java.lang.Boolean.TRUE == isNormalFlashOn.value) {
            manager?.setTorchMode(manager!!.cameraIdList[0], false)
            isNormalFlashOn.setValue(false)
        } else {
            manager?.turnOnTorchWithStrengthLevel(
                manager!!.cameraIdList[0],
                flashlightStrength.get()
            )
            isNormalFlashOn.setValue(true)
        }
    }

    private fun toggleSosMarshmallow() {
        if (java.lang.Boolean.FALSE == isSosOn.value) {
            if (java.lang.Boolean.TRUE == isNormalFlashOn.value) toggleNormalFlashMarshmallow()
            if (java.lang.Boolean.TRUE == isStroboscopeOn.value) {
                toggleStroboscopeModeMarshmallow()
                while (isStroboscopeFlashOn) doNothing()
            }
            isSosOn.value = true
            val sosIndex = AtomicInteger(0)
            Thread {
                while (java.lang.Boolean.TRUE == isSosOn.value) {
                    try {
                        toggleStroboscopeFlashMarshmallow()
                        Thread.sleep(
                            actualSos[sosIndex.getAndIncrement() % actualSos.size].toLong()
                        )
                        toggleStroboscopeFlashMarshmallow()
                        Thread.sleep(
                            actualSos[sosIndex.getAndIncrement() % actualSos.size].toLong()
                        )
                    } catch (e: CameraAccessException) {
                        isSosOn.postValue(false)
                        e.printStackTrace()
                    } catch (e: InterruptedException) {
                        isSosOn.postValue(false)
                        e.printStackTrace()
                    }
                }
            }.start()
        } else isSosOn.setValue(false)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun toggleSosWithStrength() {
        if (java.lang.Boolean.FALSE == isSosOn.value) {
            if (java.lang.Boolean.TRUE == isNormalFlashOn.value) toggleNormalFlashWithStrength()
            if (java.lang.Boolean.TRUE == isStroboscopeOn.value) {
                toggleStroboscopeModeWithStrength()
                while (isStroboscopeFlashOn) doNothing()
            }
            isSosOn.value = true
            val sosIndex = AtomicInteger(0)
            Thread {
                while (java.lang.Boolean.TRUE == isSosOn.value) {
                    try {
                        toggleStroboscopeFlashWithStrength()
                        Thread.sleep(
                            actualSos[sosIndex.getAndIncrement() % actualSos.size].toLong()
                        )
                        toggleStroboscopeFlashWithStrength()
                        Thread.sleep(
                            actualSos[sosIndex.getAndIncrement() % actualSos.size].toLong()
                        )
                    } catch (e: CameraAccessException) {
                        isSosOn.postValue(false)
                        e.printStackTrace()
                    } catch (e: InterruptedException) {
                        isSosOn.postValue(false)
                        e.printStackTrace()
                    }
                }
            }.start()
        } else isSosOn.setValue(false)
    }

    private fun toggleSosLollipop() {
        if (java.lang.Boolean.FALSE == isSosOn.value) {
            if (java.lang.Boolean.TRUE == isNormalFlashOn.value) toggleNormalFlashLollipop()
            if (java.lang.Boolean.TRUE == isStroboscopeOn.value) {
                toggleStroboscopeModeLollipop()
                while (isStroboscopeFlashOn) doNothing()
            }
            isSosOn.value = true
            val sosIndex = AtomicInteger(0)
            Thread {
                while (java.lang.Boolean.TRUE == isSosOn.value) {
                    try {
                        toggleStroboscopeFlashLollipop()
                        Thread.sleep(
                            actualSos[sosIndex.getAndIncrement() % actualSos.size].toLong()
                        )
                        toggleStroboscopeFlashLollipop()
                        Thread.sleep(
                            actualSos[sosIndex.getAndIncrement() % actualSos.size].toLong()
                        )
                    } catch (e: InterruptedException) {
                        isSosOn.postValue(false)
                        e.printStackTrace()
                    }
                }
            }.start()
        } else isSosOn.setValue(false)
    }

    private fun toggleStroboscopeModeMarshmallow() {
        if (java.lang.Boolean.FALSE == isStroboscopeOn.value) {
            if (java.lang.Boolean.TRUE == isNormalFlashOn.value) toggleNormalFlashMarshmallow()
            if (java.lang.Boolean.TRUE == isSosOn.value) {
                toggleSosMarshmallow()
                while (isStroboscopeFlashOn) doNothing()
            }
            isStroboscopeOn.value = true
            Thread {
                while (java.lang.Boolean.TRUE == isStroboscopeOn.value) {
                    try {
                        toggleStroboscopeFlashMarshmallow()
                        Thread.sleep(
                            stroboscopeInterval.get()
                                .toLong()
                        )
                        toggleStroboscopeFlashMarshmallow()
                        Thread.sleep(
                            stroboscopeInterval.get()
                                .toLong()
                        )
                    } catch (e: CameraAccessException) {
                        isStroboscopeOn.postValue(
                            false
                        )
                        e.printStackTrace()
                    } catch (e: InterruptedException) {
                        isStroboscopeOn.postValue(
                            false
                        )
                        e.printStackTrace()
                    }
                }
            }.start()
        } else isStroboscopeOn.setValue(false)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun toggleStroboscopeModeWithStrength() {
        if (java.lang.Boolean.FALSE == isStroboscopeOn.value) {
            if (java.lang.Boolean.TRUE == isNormalFlashOn.value) toggleNormalFlashWithStrength()
            if (java.lang.Boolean.TRUE == isSosOn.value) {
                toggleSosWithStrength()
                while (isStroboscopeFlashOn) doNothing()
            }
            isStroboscopeOn.value = true
            Thread {
                while (java.lang.Boolean.TRUE == isStroboscopeOn.value) {
                    try {
                        toggleStroboscopeFlashWithStrength()
                        Thread.sleep(
                            stroboscopeInterval.get()
                                .toLong()
                        )
                        toggleStroboscopeFlashWithStrength()
                        Thread.sleep(
                            stroboscopeInterval.get()
                                .toLong()
                        )
                    } catch (e: CameraAccessException) {
                        isStroboscopeOn.postValue(
                            false
                        )
                        e.printStackTrace()
                    } catch (e: InterruptedException) {
                        isStroboscopeOn.postValue(
                            false
                        )
                        e.printStackTrace()
                    }
                }
            }.start()
        } else isStroboscopeOn.setValue(false)
    }

    private fun toggleStroboscopeModeLollipop() {
        if (java.lang.Boolean.FALSE == isStroboscopeOn.value) {
            if (java.lang.Boolean.TRUE == isNormalFlashOn.value) toggleNormalFlashLollipop()
            if (java.lang.Boolean.TRUE == isSosOn.value) {
                toggleSosLollipop()
                while (isStroboscopeFlashOn) doNothing()
            }
            isStroboscopeOn.value = true
            Thread {
                while (java.lang.Boolean.TRUE == isStroboscopeOn.value) {
                    try {
                        toggleStroboscopeFlashLollipop()
                        Thread.sleep(
                            stroboscopeInterval.get()
                                .toLong()
                        )
                        toggleStroboscopeFlashLollipop()
                        Thread.sleep(
                            stroboscopeInterval.get()
                                .toLong()
                        )
                    } catch (e: InterruptedException) {
                        isStroboscopeOn.postValue(
                            false
                        )
                        e.printStackTrace()
                    }
                }
            }.start()
        } else isStroboscopeOn.setValue(false)
    }

    private fun toggleStroboscopeFlashLollipop() {
        if (isStroboscopeFlashOn) {
            val parameters = camera?.parameters
            parameters?.flashMode = Camera.Parameters.FLASH_MODE_OFF
            camera?.parameters = parameters
            isStroboscopeFlashOn = false
        } else try {
            val parameters = camera?.parameters
            parameters?.flashMode = camera?.let { Utils.getFlashOnParameter(it) }
            camera?.apply {
                this.parameters = parameters
                setPreviewTexture(SurfaceTexture(0))
                startPreview()
                autoFocus { _: Boolean, _: Camera? -> }
            }
            isStroboscopeFlashOn = true
        } catch (ignored: IOException) {
        }
    }

    @Throws(CameraAccessException::class)
    private fun toggleStroboscopeFlashMarshmallow() {
        manager?.setTorchMode(manager!!.cameraIdList[0], !isStroboscopeFlashOn)
        isStroboscopeFlashOn = !isStroboscopeFlashOn
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun toggleStroboscopeFlashWithStrength() {
        if (isStroboscopeFlashOn) manager?.setTorchMode(
            manager!!.cameraIdList[0],
            false
        ) else manager?.turnOnTorchWithStrengthLevel(
            manager!!.cameraIdList[0], flashlightStrength.get()
        )
        isStroboscopeFlashOn = !isStroboscopeFlashOn
    }

    private fun applySosLengthsFromSettings(context: Context) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val ditDuration = getCurrentDitLength(context)
        actualSos.clear()
        actualSos.addAll(sosReference)
        actualSos.replaceAll { duration: Int -> if (duration == 750) ditDuration * 3 else 250 }
        actualSos.replaceAll { duration: Int -> if (duration == 250) ditDuration else duration }
        actualSos[actualSos.size - 1] = ditDuration * 7
        if (preferences.getBoolean("use_farnsworth", false)) {
            val farnsworthUnitLength = preferences.getString("farnsworth_unit", "")!!.toInt()
            actualSos[5] = farnsworthUnitLength * 3
            actualSos[11] = farnsworthUnitLength * 3
            actualSos[actualSos.size - 1] = farnsworthUnitLength * 7
        }
    }

    /**
     * @return The current length of a `dit` in milliseconds
     */
    fun getCurrentDitLength(context: Context?): Int {
        val preferences = PreferenceManager.getDefaultSharedPreferences(
            context!!
        )
        return (60f / (50 * preferences.getString("words_per_min", "5")!!
            .toInt()) * 1000).roundToInt()
    }

    fun getCurrentDitLength(preferences: SharedPreferences): Int {
        return (60f / (50 * preferences.getString("words_per_min", "5")!!
            .toInt()) * 1000).roundToInt()
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    fun getFlashlightStrengthLevel(context: Context?): Int {
        return try {
            manager?.getCameraCharacteristics(manager!!.cameraIdList[0])
                ?.get(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL)!!
        } catch (e: CameraAccessException) {
            Toast.makeText(context, R.string.cannot_access_camera, Toast.LENGTH_SHORT).show()
            e.printStackTrace()
            1
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private fun applyFlashlightStrengthFromSettings(context: Context) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val strength = preferences.getInt("flashlight_strength", -1)
        if (strength != -1) flashlightStrength.set(strength) else flashlightStrength.set(
            getFlashlightStrengthLevel(context)
        )
    }

    private fun doNothing() {
        //empty method to suppress android studio warnings
    }

    companion object {
        private var instance: CameraHelper? = null
        private val isNormalFlashOn = MutableLiveData(false)
        private val isSosOn = MutableLiveData(false)
        private val isStroboscopeOn = MutableLiveData(false)
        private val stroboscopeInterval = AtomicInteger(500)

        @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
        private val flashlightStrength = AtomicInteger(1)
        val normalFlashStatus: LiveData<Boolean>
            get() = isNormalFlashOn
        val sosStatus: LiveData<Boolean>
            get() = isSosOn
        val stroboscopeStatus: LiveData<Boolean>
            get() = isStroboscopeOn

        fun setStroboscopeInterval(interval: Int) {
            stroboscopeInterval.set(interval)
        }

        @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
        fun getFlashlightStrength(): Int {
            return flashlightStrength.get()
        }

        @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
        fun setFlashlightStrength(strength: Int) {
            flashlightStrength.set(strength)
        }

        fun getInstance(context: Context): CameraHelper? {
            if (instance == null) instance = CameraHelper(context)
            return instance
        }
    }
}