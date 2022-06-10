package pie.activityrecognition.platform.android

import android.app.Service
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.IBinder
import java.lang.Exception
import java.lang.Math.round
import kotlin.math.roundToLong
import kotlin.math.sqrt

class ActivityRecognitionSensors : Service(), SensorEventListener {




    // Configurations

    private val defaultSDurationTime = 600 // seconds while sick
    private val defaultSTime = 120 // seconds to be sick
    private val shakeRepNum = 20 // Duration of shake activation




    // Constructor

    override fun onCreate() {
        super.onCreate()

        acceleration = 10f
        currentAcceleration = SensorManager.GRAVITY_EARTH
        lastAcceleration = SensorManager.GRAVITY_EARTH

        this.mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        this.mAmbientTemperature = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
        this.mRelHumidity = mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY)
        this.mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        this.mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        //resumeReading()
    }




    // Service binder

    private val mBinder : LocalBinder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): ActivityRecognitionSensors = this@ActivityRecognitionSensors
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
    private var resume = false;

    private lateinit var mAmbientTemperature : Sensor
    private var temperatureReading: Float = Float.NaN

    private lateinit var mRelHumidity : Sensor
    private var humidityReading: Float = Float.NaN
    private var sicknessStartTimer = System.nanoTime()
    private var sTimerStarted = false

    private lateinit var mAccelerometer : Sensor
    private val accelerometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngle = FloatArray(9)
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f
    private var shakeIndex = 0

    private lateinit var mMagnetometer : Sensor
    private val magnetometerReading = FloatArray(3)

    //private lateinit var mLight : Sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)




    // Usable information and functions

    var angle: Long = 0 // TODO - use?
    var direction: String = "" // TODO - use
    var shake: Boolean = false // TODO - use

    var temperatureStatus: String = "comfortable" // TODO - use

    var weather: String = "" // TODO - use
                             // TODO - try out using the pressure sensor also?
                             // TODO - we could say that with 60% humidity it could be raining
                             //         (67% humidity = 70% rain; 73% humidity = 70% rain; 75% humidity = 80% rain;)
                             //         - but this is too speculative and not accurate at all. The T0D0 is to find a better solution.

    var sick: Boolean = false // TODO - use




    fun resumeReading() {
        this.resume = true
        mSensorManager.registerListener(this, mAmbientTemperature, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager.registerListener(this, mRelHumidity, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL)
        //mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun pauseReading() {
        this.resume = false
        mSensorManager.unregisterListener(this)
    }




    // Sensor readings

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }

    override fun onSensorChanged(event: SensorEvent?) { // https://mdpi-res.com/d_attachment/sensors/sensors-15-17827/article_deploy/sensors-15-17827-v2.pdf?version=1438068266

        if (event != null && resume) {
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
            }

            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER || event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD)
                updateOrientationAngles()
            else if (event.sensor.type == Sensor.TYPE_AMBIENT_TEMPERATURE || event.sensor.type == Sensor.TYPE_RELATIVE_HUMIDITY)
                checkAndUpdateSicknessCounter()
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

    private fun updateOrientationAngles() {
        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)
        val orientation = SensorManager.getOrientation(rotationMatrix, orientationAngle)
        val degrees = (Math.toDegrees(orientation.get(0).toDouble()) + 360) % 360.0
        angle = (degrees * 100).roundToLong() / 100
        direction = compassDirection(degrees)
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

    private fun compassDirection(angle: Double): String {
        if (angle >= 350 || angle <= 10) // from { 10 - 0|360 - 350 }
            return "N"
        else if (280 < angle && angle < 350) // { 280. - 350. }
            return "NW"
        else if (260 < angle && angle <= 280) // { 260. - 280 }
            return "W"
        else if (190 < angle && angle <= 260) // { 190. - 260 }
            return "SW"
        else if (170 < angle && angle <= 190) // { 170. - 190 }
            return "S"
        else if (100 < angle && angle <= 170) // { 100. - 170 }
            return "SE"
        else if (80 < angle && angle <= 170) // { 80. - 170 }
            return "E"
        else if (10 < angle && angle <= 80) // { 10. - 80 }
            return "NE"
        throw Exception("Compass got unknown angle")
    }
}

