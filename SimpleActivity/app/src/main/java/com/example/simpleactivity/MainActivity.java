package com.example.simpleactivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import androidx.activity.ComponentActivity;

import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

//import androidx.activity.compose.setContent;
//import androidx.compose.foundation.layout.fillMaxSize;
//import androidx.compose.material3.Surface;
//import androidx.compose.material3.Text;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
//import com.example.simpleactivity.ui.theme.SimpleActivityTheme;

public class MainActivity extends ComponentActivity {

    private static final int REQUEST_PERMISSION = 200;
    String[]  m_permissionUserNames = new String[] { "Audio Recording", "Internet Access" };
    String[]  m_permissionNames = new String[] { Manifest.permission.RECORD_AUDIO, Manifest.permission.INTERNET };

    Button recordButton;

    String fileName = null;
    MediaRecorder recorder = null;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        setContentView( R.layout.activity_main );

//        Resources res = getResources();
  //      String  gloub = res.getResourceName( R.id.button );

        recordButton = findViewById( R.id.button );
        recordButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                recordButton.setText( "PROUT!" );
                if ( recorder != null ) {
                    stopRecording();
                } else {
                    startRecording();
                }
            }
        } );

        // Runtime permissions (https://developer.android.com/training/permissions/requesting)
        Boolean    allPermissionsGranted = true;
        for ( int i=0; i < m_permissionNames.length; i++ ) {
            if ( ContextCompat.checkSelfPermission(this, m_permissionNames[i]) != PackageManager.PERMISSION_GRANTED ) {
                allPermissionsGranted = false;
                break;
            }
        }
        if ( !allPermissionsGranted ) {
            ActivityCompat.requestPermissions(this, m_permissionNames, REQUEST_PERMISSION );
        }

        // Ask for external storage management (https://stackoverflow.com/questions/68140893/android-studio-throwing-ioexception-operation-not-permitted)
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ) {
            if ( !Environment.isExternalStorageManager() ) {
                Intent intent = new Intent( Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION );
                startActivity( intent );
                return;
            }
        }

        // Réponse de https://stackoverflow.com/questions/37290752/java-lang-runtimeexception-setaudiosource-failed
//       if ( ActivityCompat.checkSelfPermission( this, Manifest.permission.RECORD_AUDIO ) != PackageManager.PERMISSION_GRANTED ) {
//           ActivityCompat.requestPermissions( this, new String[] { Manifest.permission.RECORD_AUDIO }, RECORD_AUDIO );
//       } else {
//           startRecording();
//       }
    }

    // Ensures all permissions have been granted, otherwise disables the UI
    @Override
    public void onRequestPermissionsResult( int requestCode, String[] permissions, int[] grantResults ) {
        super.onRequestPermissionsResult( requestCode, permissions, grantResults );
        if ( requestCode != REQUEST_PERMISSION )
            return; // Not our request...
        if ( grantResults.length != m_permissionNames.length ) {
            // No result!
            Toast.makeText(this, "Permission issues!", Toast.LENGTH_SHORT).show();
            return;
        }

        for ( int i=0; i < grantResults.length; i++ ) {
            if ( grantResults[i] != PackageManager.PERMISSION_GRANTED ) {
                // One of our permissions was denied!
                Toast.makeText(this, "Permission for " + m_permissionUserNames[i] + " denied", Toast.LENGTH_SHORT).show();
                DisableUI();
                return;
            }
        }

        // Granted!
        Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
    }

    void DisableUI() {
        recordButton.setEnabled( false );
    }

    // Gets the recording filename to store to
    // It will simply be the concatenation of the date and time as "YYYYMMDD_HHMMSS.m4a"
    String  getFilePath() {
        String  filepath = Environment.getExternalStorageDirectory().getPath();
        File    targetDirectory = new File( filepath, "Recordings/DictaPom" );
        if ( !targetDirectory.exists() )
            targetDirectory.mkdirs();

        // Get formatted date & time
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern( "YYYYMMdd_HHmmss" );
        LocalDateTime   now = LocalDateTime.now();
        String          strDateTime = dtf.format( now );

        // Check if we can create the target file, then return its name
        try {
            File    targetFile = new File( targetDirectory.getAbsolutePath() + "/" + strDateTime + ".m4a" );
            targetFile.createNewFile();
            return targetFile.getPath();
        } catch ( IOException _e ) {
            _e.printStackTrace();
            return null;
        }
    }

    private void startRecording() {
        try {
            recorder = new MediaRecorder();
            recorder.setAudioSource( MediaRecorder.AudioSource.MIC );
            recorder.setOutputFormat( MediaRecorder.OutputFormat.MPEG_4 );
//            recorder.setAudioEncoder( MediaRecorder.AudioEncoder.AMR_NB );    // Not great
//            recorder.setAudioEncoder( MediaRecorder.AudioEncoder.AMR_WB );  // Unsupported
            recorder.setAudioEncoder( MediaRecorder.AudioEncoder.AAC ); // Bad quality
//            recorder.setAudioEncoder( MediaRecorder.AudioEncoder.HE_AAC );  // Better!

            // Better quality!
//            recorder.setAudioEncoder( MediaRecorder.getAudioSourceMax() );
            recorder.setAudioEncodingBitRate( 128000 );
            recorder.setAudioSamplingRate( 44100 );

            // Request to write to a new recording file
            String  fileName = getFilePath();
            if ( fileName == null ) {
                throw new Exception( "Failed to create output audio file! Can't record... Check storage permissions?" );
            }

            recorder.setOutputFile( fileName );
            recorder.prepare();
            recorder.start();

            recordButton.setText( "Stop Recording" );
            Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;

        recordButton.setText( "Start Recording" );
        Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show();
    }
}