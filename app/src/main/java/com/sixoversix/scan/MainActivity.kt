package com.sixoversix.scan

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.Facing
import com.sixoversix.scan.utils.facedetector.*


class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var viewfinder: CameraView

    private lateinit var faceBoundsOverlay: FaceBoundsOverlay

    private lateinit var sensorManager: SensorManager

    private var sensor: Sensor? = null

    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI();
        setContentView(R.layout.activity_main)
        viewfinder = findViewById(R.id.viewfinder)
        viewfinder.facing = Facing.FRONT
        faceBoundsOverlay = findViewById(R.id.faceBoundsOverlay)
        setupCamera()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)
        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)
        SensorManager.getOrientation(rotationMatrix, orientationAngles)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
        } else if (event?.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
        }
        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)
        SensorManager.getOrientation(rotationMatrix, orientationAngles)

        Log.d("Face", "rotation: ${rotationMatrix[0]} ${rotationMatrix[1]} ${rotationMatrix[2]}")
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Do something here if sensor accuracy changes.
        // You must implement this callback in your code.
    }

    override fun onResume() {
        super.onResume()
        viewfinder.start()

        // Get updates from the accelerometer and magnetometer at a constant rate.
        // To make batch operations more efficient and reduce power consumption,
        // provide support for delaying updates to the application.
        //
        // In this example, the sensor reporting delay is small enough such that
        // the application receives an update before the system checks the sensor
        // readings again.
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magneticField ->
            sensorManager.registerListener(
                this,
                magneticField,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    override fun onPause() {
        super.onPause()

        // Don't receive any more updates from either sensor.
        sensorManager.unregisterListener(this)
        viewfinder.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewfinder.destroy()
    }

    private fun setupCamera() {
        val faceDetector = FaceDetector(faceBoundsOverlay)

        val listener = object : FaceBoundsOverlay.OnFaceDetectionResultListener {
            override fun onSuccess(faceBounds: List<FaceBounds>) {
                Log.d("Face", "onSuccess: " + faceBounds.size)
            }
            override fun onFailure(exception: Exception) {
                Log.d("Face", "onFailure: $exception")
            }
        }
        faceBoundsOverlay.setOnFaceDetectionListener(listener)
        viewfinder.addFrameProcessor {
            faceDetector.process(
                Frame(
                    data = it.data,
                    rotation = it.rotation,
                    size = Size(it.size.width, it.size.height),
                    format = it.format,
                    lensFacing = LensFacing.FRONT
                )
            )
        }
    }

    private fun hideSystemUI() {
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }
}