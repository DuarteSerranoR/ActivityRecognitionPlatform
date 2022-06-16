package pie.activityrecognition.platform.android.sensorsservice

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.*
import android.os.Binder
import android.os.IBinder
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.Exception
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.roundToLong
import kotlin.math.sqrt


class SensorsService : Service(), SensorEventListener {




    // Configurations

    companion object {
        private const val defaultSDurationTime = 600 // seconds while sick
        private const val defaultSTime = 120 // seconds to be sick
        private const val shakeRepNum = 20 // Duration of shake activation
        private const val sleepMinDecibels: Int = 60 // Defines the minimum decibels
                                                       // for it to be sleepy
        //private const val sleepMinAmplitude: Int = 100 // Defines the minimum amplitude
        //                                               // for it to be sleepy
        private const val sleepyTime: Long = 20 // How long it will be sleepy before falling asleep

        // For audio
        private const val sampleRate = 48000
        private const val channelConfig = AudioFormat.CHANNEL_IN_MONO
        private const val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    }




    // Constructor

    override fun onCreate() {
        super.onCreate()

        // Permission check for audio recording

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        ) {
            mRecorder = AudioRecord(MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                minBufferSize * 10)
            startAudioStream()
        }


        // Initialization of other services
        acceleration = 10f
        currentAcceleration = SensorManager.GRAVITY_EARTH
        lastAcceleration = SensorManager.GRAVITY_EARTH

        this.mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        this.mAmbientTemperature = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
        this.mRelHumidity = mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY)
        this.mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        this.mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        this.mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        if (mAccelerometer == null)
            hasAccelerometerSensor = false
        if (mRelHumidity == null)
            hasRelHumiditySensor = false
        if (mMagnetometer == null)
            hasMagnetometerSensor = false
        if (mAmbientTemperature == null)
            hasAmbientTemperatureSensor = false
        if (mLight == null)
            hasLightSensor = false

        //resumeReading()
    }

    override fun onDestroy() {
        if (running)
            pauseReading()
        mRecorder?.release()
        super.onDestroy()
    }




    // Service binder

    private val mBinder : LocalBinder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): SensorsService = this@SensorsService
    }

    override fun onBind(intent: Intent?): IBinder {
        return mBinder
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        //stopSelf() -> if we want it to not run on background when the app is closed
        super.onTaskRemoved(rootIntent)
    }




    // Sensors

    private lateinit var mSensorManager : SensorManager

    private var mAmbientTemperature : Sensor? = null
    var hasAmbientTemperatureSensor : Boolean = true
    private var temperatureReading: Float = Float.NaN

    private var mRelHumidity : Sensor? = null
    var hasRelHumiditySensor : Boolean = true
    private var humidityReading: Float = Float.NaN
    private var sicknessStartTimer = System.nanoTime()
    private var sTimerStarted = false

    private var mAccelerometer : Sensor? = null
    var hasAccelerometerSensor : Boolean = true
    private val accelerometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngle = FloatArray(9)
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f
    private var shakeIndex = 0

    private var mMagnetometer : Sensor? = null
    var hasMagnetometerSensor : Boolean = true
    private val magnetometerReading = FloatArray(3)

    private var mLight : Sensor? = null
    var hasLightSensor : Boolean = true
    private var sleepyElapsed : Long = 0

    // Audio Recorder
    private val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
    private var mRecorder: AudioRecord? = null
    private var recording : Boolean = false
    private val scope = MainScope()


    // Usable information and functions

    var running = false

    var angle: Long = 0
    var direction: String = "N"
    var shake: Boolean = false

    var temperatureStatus: String = "comfortable"

    var weather: String = "Not raining"
                             // TODO - try out using the pressure sensor also?
                             // TODO - we could say that with 60% humidity it could be raining
                             //         (67% humidity = 70% rain; 73% humidity = 70% rain; 75% humidity = 80% rain;)
                             //         - but this is too speculative and not accurate at all. The T0D0 is to find a better solution.

    var sick: Boolean = false

    var sleepStatus : String = "Awake" // TODO - use enumerable objects here and in the weather reading?
    var light : Float = 0f
    var sound: Double = 0.0 // Constantly represents the max Amplitude




    fun resumeReading() {
        running = true
        mSensorManager.registerListener(this, mAmbientTemperature, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager.registerListener(this, mRelHumidity, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL)
        mRecorder?.startRecording()
        recording = true
    }

    fun pauseReading() {
        running = false
        mSensorManager.unregisterListener(this)
        recording = false
        mRecorder?.stop()
    }




    // Sensor readings

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }

    override fun onSensorChanged(event: SensorEvent?) { // https://mdpi-res.com/d_attachment/sensors/sensors-15-17827/article_deploy/sensors-15-17827-v2.pdf?version=1438068266

        if (event?.values != null && running) {
            when (event.sensor.type) {
                Sensor.TYPE_AMBIENT_TEMPERATURE -> {

                    temperatureReading = event.values[0]

                    temperatureStatus = if (temperatureReading > 25)
                        "hot"
                    else if (temperatureReading < 10)
                        "cold"
                    else if (temperatureReading < 1)
                        "freezing"
                    else
                        "comfortable"

                    checkAndUpdateSicknessCounter()
                }
                Sensor.TYPE_RELATIVE_HUMIDITY -> {

                    humidityReading = event.values[0]

                    weather =
                        if (humidityReading > 60) {
                            if (temperatureReading <= 0)
                                "Snowing" // TODO - hail/granizo?
                            else
                                "Raining" // TODO - make real world comparison, or a weather website/API
                        } else
                            "Not raining" // TODO - sunny? Clear? Cloudy?

                    checkAndUpdateSicknessCounter()
                }
                Sensor.TYPE_ACCELEROMETER -> {

                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]

                    System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
                    updateOrientationAngles()
                    shakeDetection(x, y, z)
                }
                Sensor.TYPE_MAGNETIC_FIELD -> {
                    System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
                    updateOrientationAngles()
                }
                Sensor.TYPE_LIGHT -> {
                    light = event.values[0]
                    if (mRecorder != null &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                            == PackageManager.PERMISSION_GRANTED)
                        checkSleepWAudio()
                    else
                        checkSleep()
                }
            }

            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER
                || event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD)
                updateOrientationAngles()
            else if (event.sensor.type == Sensor.TYPE_AMBIENT_TEMPERATURE
                || event.sensor.type == Sensor.TYPE_RELATIVE_HUMIDITY)
                checkAndUpdateSicknessCounter()

            if (mAccelerometer == null)
                hasAccelerometerSensor = false
            if (mRelHumidity == null)
                hasRelHumiditySensor = false
            if (mMagnetometer == null)
                hasMagnetometerSensor = false
            if (mAmbientTemperature == null)
                hasAmbientTemperatureSensor = false
            if (mLight == null)
                hasLightSensor = false
        }
    }

    private fun startAudioStream() = runBlocking {
            scope.launch {
                try {
                    delay(1000L)
                    val buffer = ShortArray(minBufferSize)
                    while (true) {
                        delay(10L)
                        if (recording) {
                            var maxAmplitude = 0.0
                            // read the data into the buffer
                            val readSize: Int = mRecorder!!.read(buffer, 0, buffer.size)
                            for (i in 0 until readSize) {
                                if (abs(buffer[i].toDouble()) > maxAmplitude) {
                                    maxAmplitude = abs(buffer[i].toDouble())
                                }
                            }
                            // this converts the amplitude to decibels
                            var db = 0.0
                            if (maxAmplitude != 0.0) {
                                db = 20.0 * log10(maxAmplitude / 32767.0) + 90
                            }
                            sound = db
                        }
                        else
                            sound = 0.0
                    }
                } catch (e: Exception) {
                    println("Exception on AudioRecording Coroutine: $e")
                }
            }
    }




    // Status update functions

    private fun checkAndUpdateSicknessCounter() {
        // Sickness Counter
        if (sick) {
            if (temperatureReading < 10 && humidityReading > 60) {
                sicknessStartTimer = System.nanoTime()
            }
            val currentTime = System.nanoTime()
            if ((currentTime - sicknessStartTimer) / 1_000_000_000 >= defaultSDurationTime) {
                sick = false
                sicknessStartTimer = System.nanoTime()
            }
        } else {
            if (!sTimerStarted && temperatureReading < 10 && humidityReading > 60) {
                sTimerStarted = true
                sicknessStartTimer = System.nanoTime()
            } else if (temperatureReading > 10 || humidityReading < 60) {
                if (sTimerStarted) {
                    val currentTime = System.nanoTime()
                    val elapsedTime = (currentTime - sicknessStartTimer) / 1_000_000_000
                    if (elapsedTime >= defaultSTime) {
                        sick = true
                        sicknessStartTimer = System.nanoTime()
                    }
                }
                sTimerStarted = false
            }
        }
    }

    private fun updateOrientationAngles() {       SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)
        val orientation = SensorManager.getOrientation(rotationMatrix, orientationAngle)
        //val degrees = (Math.toDegrees(orientation.get(0).toDouble()) + 360) % 360.0
        val degrees = (Math.toDegrees(orientation[0].toDouble()) + 360) % 360.0
        angle = (degrees * 100).roundToLong() / 100
        direction = SensorsUtils.compassDirection(degrees)
    }

    private fun shakeDetection(x: Float, y: Float, z: Float) {
        lastAcceleration = currentAcceleration
        currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        val delta: Float = currentAcceleration - lastAcceleration
        acceleration = acceleration * 0.9f + delta
        if (acceleration > 12) {
            shakeIndex = 0
            shake = true
            // TODO - detect the direction/orientation (up&down, etc. with angles or main angle)
        }
        else if (shakeIndex < shakeRepNum)
            shakeIndex++
        else if (shake && shakeIndex >= shakeRepNum)
            shake = false
    }

    private fun checkSleep() {
        if (light < 4 && sleepStatus == "Awake") {
            sleepyElapsed = TimeUnit.SECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS)
            sleepStatus = "Sleepy"
        }
        else if (light > 4) {
            sleepStatus = "Awake"
        }

        val currentTimeSecs = TimeUnit.SECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS)
        val sleepySecs: Long = currentTimeSecs - sleepyElapsed
        if (sleepySecs > sleepyTime && sleepStatus == "Sleepy") {
            sleepStatus = "Sleeping"
        }
    }

    private fun checkSleepWAudio() {
        if (light < 4 && sound < sleepMinDecibels) {
            sleepyElapsed = TimeUnit.SECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS)
            sleepStatus = "Sleepy"
        }
        else if (light > 4 || sound > sleepMinDecibels) {
            sleepStatus = "Awake"
        }

        val currentTimeSecs = TimeUnit.SECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS)
        val sleepySecs: Long = currentTimeSecs - sleepyElapsed
        if (sleepySecs < sleepyTime && sleepStatus == "Sleepy") {
            sleepStatus = "Sleeping"
        }
    }
}

