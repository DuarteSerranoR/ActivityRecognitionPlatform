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

class ActivityRecognitionSensors : Service(), SensorEventListener {


    private val mBinder = LocalBinder()

    override fun onCreate() {
        super.onCreate()
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAmbientTemperature = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
        mRelHumidity = mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY)
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        resumeReading()
    }

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



    private lateinit var mSensorManager : SensorManager


    private lateinit var mAmbientTemperature : Sensor
    private val temperatureReading: Float = Float.NaN
    //private val highTempTimer

    private lateinit var mRelHumidity : Sensor
    private val humidityReading: Float = Float.NaN

    private lateinit var mAccelerometer : Sensor
    private val accelerometerReading = FloatArray(3)

    // sleeping detection sound

    private lateinit var mMagnetometer : Sensor
    private val magnetometerReading = FloatArray(3)

    //private lateinit var mGravity : Sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)

    //private lateinit var mLight : Sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    //private lateinit var mGyroscope : Sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private val rotationMatrix = FloatArray(9)
    private val orientationAngle = FloatArray(9)

    private var resume = false;

    var angle: Long = 0
    var direction: String = "N"

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }

    override fun onSensorChanged(event: SensorEvent?) { // https://mdpi-res.com/d_attachment/sensors/sensors-15-17827/article_deploy/sensors-15-17827-v2.pdf?version=1438068266

        /*
        * event.values[0] is x
        * event.values[1] is y
        * event.values[2] is z
        * */

        if (event != null && resume) {
            when (event.sensor.type) {
                Sensor.TYPE_AMBIENT_TEMPERATURE -> {
                    // TODO - read humidity comments
                    print(event.values[0].toString()) // float with temp values
                }
                Sensor.TYPE_RELATIVE_HUMIDITY -> {
                    // if high_humidity && low_temperature -> TimerState.Start -> startTimer() -> do this,
                    //                  and start another thread that counts x time and checks if it was interrupted,
                    //                  if so, it will start a new timer for how much time the pet is sick
                    print(event.values[0].toString()) // Float with relative humidity in percentage
                }
                Sensor.TYPE_ACCELEROMETER -> {
                    // Use stored in arrays with magnetic, later use to build a compass and maybe something else with coordinates
                    // (maybe if the phone is being shacked and in what direction)
                    System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
                }
                Sensor.TYPE_MAGNETIC_FIELD -> {
                    // TODO - read accelerometer
                    System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
                }
            }

            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER || event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD)
                updateOrientationAngles()
        }
    }

    fun resumeReading() {
        this.resume = true
        mSensorManager.registerListener(this, mAmbientTemperature, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager.registerListener(this, mRelHumidity, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL)
        //mSensorManager.registerListener(this, mGravity, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun pauseReading() {
        this.resume = false
        mSensorManager.unregisterListener(this)
    }

    private fun updateOrientationAngles() {
        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)
        val orientation = SensorManager.getOrientation(rotationMatrix, orientationAngle)
        val degrees = (Math.toDegrees(orientation.get(0).toDouble()) + 360) % 360.0
        angle = (degrees * 100).roundToLong() / 100
        direction = compassDirection(degrees)
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
