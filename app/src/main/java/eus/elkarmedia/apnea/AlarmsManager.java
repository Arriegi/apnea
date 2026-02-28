package eus.elkarmedia.apnea;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;

public class AlarmsManager {

    private final Context context;
    private final Vibrator vibrator;
    private final AudioManager audioManager;
    private MediaPlayer player;

    private boolean isVibrating = false;
    private boolean hasToNotice = false;
    private int volumeStreak = 0;
    
    // We increment these each time they start
    private int totalVibrate = 0;
    private int totalSound = 0;
    
    private final int initialVolume;
    private int MAX_VOLUME_STREAK_IN_SECONDS;

    public AlarmsManager(Context context) {
        this.context = context;

        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        player = getMediaPlayer();

        initialVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 1, 0);
    }

    public void setMaxVolumeStreakInSeconds(int maxVolumeStreak) {
        this.MAX_VOLUME_STREAK_IN_SECONDS = maxVolumeStreak;
    }

    public void setHasToNotice(boolean hasToNotice) {
        this.hasToNotice = hasToNotice;
    }

    public void vibrate() {
        if (!hasToNotice) return;
        if (!isVibrating) {
            long[] pattern = {0, 1000, 200}; //0 to start now, 1000 to vibrate 1000 ms, 200 to sleep for 200 ms.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(android.os.VibrationEffect.createWaveform(pattern, 0));
            } else {
                vibrator.vibrate(pattern, 0); // 0 to repeat endlessly.
            }
            isVibrating = true;
            totalVibrate++;
        }
    }

    public void stopVibrating() {
        if (vibrator != null) {
            vibrator.cancel();
        }
        isVibrating = false;
    }

    public void sound() {
        if (!hasToNotice) return;
        try {
            if (player == null) {
                player = getMediaPlayer();
            }
            if (player != null && !player.isPlaying()) {
                player.setLooping(true);
                player.start();
                totalSound++;
            } else {
                // Raise volume if has past X seconds since last raising
                if (volumeStreak == MAX_VOLUME_STREAK_IN_SECONDS) {
                    volumeStreak = 0;
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0);
                } else {
                    volumeStreak++;
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void stopSounds() {
        try {
            if (player == null) {
                player = getMediaPlayer();
            }
            if (player != null && player.isPlaying()) {
                player.pause();
            }
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 1, 0);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void stopAll() {
        stopSounds();
        stopVibrating();
    }
    
    public void resetCounters() {
        totalVibrate = 0;
        totalSound = 0;
    }

    public void restoreInitialVolume() {
        if (audioManager != null) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, initialVolume, 0);
        }
    }

    public int getTotalVibrate() {
        return totalVibrate;
    }

    public int getTotalSound() {
        return totalSound;
    }

    private MediaPlayer getMediaPlayer() {
        Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alert == null) {
            alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            if (alert == null) {
                alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
        }
        if (alert != null) {
            return MediaPlayer.create(context, alert);
        } else {
            return MediaPlayer.create(context, R.raw.android_notification);
        }
    }
}
