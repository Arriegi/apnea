package eus.elkarmedia.apnea;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * Measures ambient sound level in decibels using the device microphone.
 *
 * Uses MediaRecorder configured to capture audio without actually saving it,
 * providing real-time amplitude readings converted to decibels.
 *
 * Follows the Single Responsibility Principle: this class only measures sound.
 */
public class SoundLevelMeter {

    private static final String TAG = "SoundLevelMeter";
    private static final double AMPLITUDE_REFERENCE = 1.0;

    private MediaRecorder recorder;
    private boolean isRunning = false;

    /**
     * Starts the sound level meter.
     *
     * @param context The context used to initialize the MediaRecorder and access cache directory.
     */
    public void start(Context context) {
        if (isRunning) {
            return;
        }
        try {
            recorder = createMediaRecorder(context);
            recorder.prepare();
            recorder.start();
            isRunning = true;
        } catch (IOException | RuntimeException e) {
            Log.e(TAG, "Failed to start sound level meter. This can happen if the microphone " +
                    "is in use by another app or if the app is in the background.", e);
            releaseRecorder();
        }
    }

    public void stop() {
        if (!isRunning) {
            return;
        }
        try {
            recorder.stop();
        } catch (RuntimeException e) {
            Log.e(TAG, "Error stopping recorder", e);
        }
        releaseRecorder();
        isRunning = false;
    }

    public double getCurrentDecibelLevel() {
        if (!isRunning || recorder == null) {
            return 0.0;
        }
        try {
            int amplitude = recorder.getMaxAmplitude();
            return amplitudeToDecibels(amplitude);
        } catch (RuntimeException e) {
            Log.e(TAG, "Error getting max amplitude", e);
            return 0.0;
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Converts a raw amplitude value to decibels (dB SPL approximation).
     *
     * Formula: dB = 20 * log10(amplitude / reference)
     * Reference amplitude of 1.0 yields 0 dB for silence.
     *
     * This is a pure function, easily testable without Android dependencies.
     */
    public static double amplitudeToDecibels(double amplitude) {
        if (amplitude <= 0) {
            return 0.0;
        }
        return 20.0 * Math.log10(amplitude / AMPLITUDE_REFERENCE);
    }

    private MediaRecorder createMediaRecorder(Context context) {
        MediaRecorder mediaRecorder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            mediaRecorder = new MediaRecorder(context);
        } else {
            mediaRecorder = new MediaRecorder();
        }
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        
        // Use a temporary file in the cache directory instead of /dev/null 
        // for better compatibility across different Android versions and devices.
        File tempFile = new File(context.getCacheDir(), "sound_meter_temp.3gp");
        mediaRecorder.setOutputFile(tempFile.getAbsolutePath());
        
        return mediaRecorder;
    }

    private void releaseRecorder() {
        if (recorder != null) {
            try {
                recorder.release();
            } catch (Exception e) {
                Log.e(TAG, "Error releasing recorder", e);
            }
            recorder = null;
        }
    }
}
