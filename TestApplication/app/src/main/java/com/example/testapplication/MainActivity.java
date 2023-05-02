package com.example.testapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String fileName = null;
    private MediaRecorder recorder = null;
    private Button recordButton = null;
    private boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        setContentView(R.layout.activity_main);
//        setContentView(R.layout.testicule);

//        recordButton = findViewById(R.id.record_button);
        recordButton = findViewById(R.id.button_tagada);
        fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recording.3gp";

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION );
        } else {
            setupRecorder();
        }

        recordButton.setOnClickListener(
            v -> {
                if (isRecording) {
                    stopRecording();
                } else {
                    startRecording();
                }
            }
        );
    }

    private void setupRecorder() {
        recorder = new MediaRecorder();
        recorder.setAudioSource( MediaRecorder.AudioSource.MIC );
        recorder.setOutputFormat( MediaRecorder.OutputFormat.THREE_GPP );
        recorder.setAudioEncoder( MediaRecorder.AudioEncoder.AMR_NB );
        recorder.setOutputFile( fileName );
    }

    private void startRecording() {
        try {
            recorder.prepare();
            recorder.start();
            isRecording = true;
            recordButton.setText( "Stop Recording" );
            Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        recorder.stop();
        recorder.release();
        isRecording = false;
        recordButton.setText( "Start Recording" );
        Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show();
        setupRecorder();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if ( requestCode == REQUEST_RECORD_AUDIO_PERMISSION ) {
            if ( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                setupRecorder();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
