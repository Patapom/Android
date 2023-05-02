/*package com.example.testapplication

import android.Manifest
import android.R
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private var recorder: MediaRecorder? = null
    private var recordButton: Button? = null
    private var isRecording = false

    override protected fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recordButton = findViewById(R.id.record_button)
        fileName = Environment.getExternalStorageDirectory().absolutePath + "/recording.3gp"
        if ( ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_PERMISSION )
        } else {
            setupRecorder()
        }
        recordButton!!.setOnClickListener { v: View? ->
            if (isRecording) {
                stopRecording()
            } else {
                startRecording()
            }
        }
    }

    private fun setupRecorder() {
        recorder = MediaRecorder()
        recorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        recorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        recorder!!.setOutputFile(fileName)
    }

    private fun startRecording() {
        try {
            recorder!!.prepare()
            recorder!!.start()
            isRecording = true
            recordButton!!.text = "Stop Recording"
            Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun stopRecording() {
        recorder!!.stop()
        recorder!!.release()
        isRecording = false
        recordButton!!.text = "Start Recording"
        Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show()
        setupRecorder()
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>?,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.size > 0 && grantResults == PackageManager.PERMISSION_GRANTED) {
                setupRecorder()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
        private var fileName: String? = null
    }
}
*/