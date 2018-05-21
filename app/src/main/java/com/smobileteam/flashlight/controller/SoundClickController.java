package com.smobileteam.flashlight.controller;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

import com.smobileteam.flashlight.R;

/**
 * Created by Duong Anh Son on 7/5/2016.
 */
public class SoundClickController {
    // Maximumn sound stream.
    private static final int MAX_STREAMS = 5;
    // Stream type.
    private static final int streamType = AudioManager.STREAM_ALARM;
    private static SoundPool soundPool;
//    private static float volume;
    private static boolean loaded;
    private static int mSoundClick;
    private static int mSoundMove;
    private static int mSoundWarning;
    private final static int MAX_VOLUME = 1;

    public  static void initSoundManager(Context context){
        // AudioManager audio settings for adjusting the volume
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

/*        // Current volumn Index of particular stream type.
        float currentVolumeIndex = (float) audioManager.getStreamVolume(streamType);

        // Get the maximum volume index for a particular stream type.
        float maxVolumeIndex  = (float) audioManager.getStreamMaxVolume(streamType);

        // Volumn (0 --> 1)
        volume = currentVolumeIndex / maxVolumeIndex;*/

        // For Android SDK >= 21(LOLLIPOP)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
            // Suggests an audio stream whose volume should be changed by
            // the hardware volume controls.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                audioManager.adjustStreamVolume(streamType, AudioManager.ADJUST_RAISE, AudioManager.ADJUST_UNMUTE);
            }
            AudioAttributes audioAttrib = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            SoundPool.Builder builder= new SoundPool.Builder();
            builder.setAudioAttributes(audioAttrib).setMaxStreams(MAX_STREAMS);

            soundPool = builder.build();
        }
        // for Android SDK < 21
        else {
            // SoundPool(int maxStreams, int streamType, int srcQuality)
            soundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
        }

        // When Sound Pool load complete.
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loaded = true;
            }
        });
        // Load sound file
        mSoundClick = soundPool.load(context, R.raw.sound_toggle,1);
        mSoundMove = soundPool.load(context, R.raw.adjustment_move,1);
        mSoundWarning = soundPool.load(context,R.raw.double_bip,1);
    }

    public static void clickSoundEffect(){
        if(loaded)  {
/*            float leftVolumn = volume;
            float rightVolumn = volume;*/

            // Play sound of gunfire. Returns the ID of the new stream.
            soundPool.play(mSoundClick, MAX_VOLUME, MAX_VOLUME, 1, 0, 1f);
        }
    }
    public static void moveSoundEffect(){
        if(loaded)  {
/*            float leftVolumn = volume;
            float rightVolumn = volume;*/

            // Play sound of gunfire. Returns the ID of the new stream.
            soundPool.play(mSoundMove, MAX_VOLUME, MAX_VOLUME, 1, 0, 1f);
        }
    }

    public static void warningSoundEffect(){
        if(loaded){
            soundPool.play(mSoundWarning,MAX_VOLUME,MAX_VOLUME,1, 0, 1f);
        }
    }


}
