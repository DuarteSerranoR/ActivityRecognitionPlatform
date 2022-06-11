package pie.activityrecognition.platform.android.sensorsservice

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.IBinder
import kotlin.math.roundToLong
import kotlin.math.sqrt

class SensorsService : Service(), SensorEventListener {




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

    override fun onDestroy() {
        if (running)
            pauseReading()
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

    var running = false

    var angle: Long = 0 // TODO - use?
    var direction: String = "N" // TODO - use
    var shake: Boolean = false // TODO - use

    var temperatureStatus: String = "comfortable" // TODO - use

    var weather: String = "Not raining" // TODO - use
                             // TODO - try out using the pressure sensor also?
                             // TODO - we could say that with 60% humidity it could be raining
                             //         (67% humidity = 70% rain; 73% humidity = 70% rain; 75% humidity = 80% rain;)
                             //         - but this is too speculative and not accurate at all. The T0D0 is to find a better solution.

    var sick: Boolean = false // TODO - use




    fun resumeReading() {
        running = true
        mSensorManager.registerListener(this, mAmbientTemperature, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager.registerListener(this, mRelHumidity, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL)
        //mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun pauseReading() {
        running = false
        mSensorManager.unregisterListener(this)
    }




    // Sensor readings

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }

    override fun onSensorChanged(event: SensorEvent?) { // https://mdpi-res.com/d_attachment/sensors/sensors-15-17827/article_deploy/sensors-15-17827-v2.pdf?version=1438068266

        if (event != null && running) {
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
}

