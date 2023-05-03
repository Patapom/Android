package com.example.simpleactivity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import androidx.activity.ComponentActivity;

import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// So the idea behind this simple activity is to operate as a dictaphone:
//  • Start recording as soon as it launches
//  • Stop recording whenever the application quits (or when the power button is pressed)
//
// It's very easy to run on a side key (i.e. power button) double-click (Settings > Advanced Features > Side Key > Open App > DictaPom).
// Then close it with another click on the power button.
// It should feel like an actual dictaphone...
//
public class MainActivity extends ComponentActivity {

    private static final int REQUEST_PERMISSION = 200;
    String[]  m_permissionUserNames = new String[] { "Audio Recording", "Internet Access" };
    String[]  m_permissionNames = new String[] { Manifest.permission.RECORD_AUDIO, Manifest.permission.INTERNET };

    Button recordButton;

    MediaRecorder recorder = null;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        setContentView( R.layout.activity_main );

        recordButton = findViewById( R.id.button );
        recordButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( recorder != null ) {
                    stopRecording();
                } else {
                    startRecording();
                }
            }
        } );

        // Ask for external storage management
        // Code from https://stackoverflow.com/questions/68140893/android-studio-throwing-ioexception-operation-not-permitted
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ) {
            if ( !Environment.isExternalStorageManager() ) {
                Intent intent = new Intent( Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION );
                startActivity( intent );
            }
        }

        // Ask for runtime permissions to allow mic usage and internet connection (for uploading recordings to a server)
        // Code from https://developer.android.com/training/permissions/requesting
        Boolean    allPermissionsGranted = true;
        for ( int i=0; i < m_permissionNames.length; i++ ) {
            if ( ContextCompat.checkSelfPermission(this, m_permissionNames[i]) != PackageManager.PERMISSION_GRANTED ) {
                allPermissionsGranted = false;
                break;
            }
        }
        if ( allPermissionsGranted ) {
            PermissionsGranted();   // All good!
        } else {
            ActivityCompat.requestPermissions(this, m_permissionNames, REQUEST_PERMISSION );    // Ask for permissions...
        }

        // Hook the power button to kill the app (and incidently, to stop any recording in progress)
        // Example from https://www.tutorialspoint.com/how-to-hook-a-function-into-the-power-button-in-android
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.e("DictaPom", "Power button pressed... Closing." );
                finishAffinity();   // This will in turn stop any recording...
            }
        };

        IntentFilter filter = new IntentFilter( Intent.ACTION_SCREEN_OFF );
        registerReceiver( receiver, filter );

/* Actually, this code is useless for my purpose:
    • If the app is already running (launched manually), then it will show even when screen is locked but we don't care
    • If the app is not running and we're trying to run it after a double-click o the power button, then it still needs unlocking to work, and this option is useless...

        // Allows the app to show even when screen is locked
        // (Code from https://stackoverflow.com/questions/35356848/android-how-to-launch-activity-over-lock-screen/55998126#55998126)
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 ){
            setShowWhenLocked( true );
            setTurnScreenOn( true );
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService( Context.KEYGUARD_SERVICE );
            if( keyguardManager != null )
                keyguardManager.requestDismissKeyguard( this, null );
        } else {
            getWindow().addFlags(   WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON );
        }
*/
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Toast( "onResume() called!" );
    }

    @Override
    protected void onPause() {
        super.onPause();
//        Toast( "onPause() called!" );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRecording();
    }

    // Ensures all permissions have been granted, otherwise disables the UI
    @Override
    public void onRequestPermissionsResult( int requestCode, String[] permissions, int[] grantResults ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQUEST_PERMISSION)
            return; // Not our request...
        if (grantResults.length != m_permissionNames.length) {
            // No result!
            Toast("Permission issues!");
            return;
        }

        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                // One of our permissions was denied!
                Toast("Permission for " + m_permissionUserNames[i] + " denied");
                PermissionsDenied();
                return;
            }
        }

        PermissionsGranted();
    }

    void    PermissionsGranted() {
        // Granted!
        Toast( "Permission granted!" );

        // Start recording immediately...
        startRecording();
    }
    void    PermissionsDenied() {
        Toast("Permission denied!" );

        // Prevent any usage...
        DisableUI();
    }

    void Toast( String _text ) {
        Toast.makeText(this, _text, Toast.LENGTH_SHORT ).show();
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
            if ( recorder != null )
                throw new Exception( "Recording already in progress!" );

            recorder = new MediaRecorder();
            recorder.setAudioSource( MediaRecorder.AudioSource.MIC );
            recorder.setOutputFormat( MediaRecorder.OutputFormat.MPEG_4 );
            recorder.setAudioEncoder( MediaRecorder.AudioEncoder.AAC );
//            recorder.setAudioEncoder( MediaRecorder.getAudioSourceMax() );    // CRASH!
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

            Toast("Recording started" );

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        if ( recorder == null )
            return; // Already stopped!

        recorder.stop();
        recorder.release();
        recorder = null;

        recordButton.setText( "Start Recording" );

        Toast("Recording stopped" );
    }
}