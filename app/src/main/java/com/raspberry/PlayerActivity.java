package com.raspberry;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.raspberry.player.listener.VideoListener;
import com.raspberry.player.player.VideoPlayer;


import java.io.IOException;
import java.net.URI;

import org.fourthline.cling.support.lastchange.LastChangeAwareServiceManager;
import tv.danmaku.ijk.media.player.IMediaPlayer;

public class PlayerActivity extends AppCompatActivity implements VideoListener {
    private final String TAG = "PlayerActivity";
    public static final int MSG_REFRESH = 1001;
    private TextView tvTime;
    private SeekBar seekBar;

    private Handler handler;
    public VideoPlayer videoPlayer;
    private RelativeLayout Loading;
    private RelativeLayout layoutBottom;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        videoPlayer = findViewById(R.id.video);
        Loading = findViewById(R.id.loading);
        tvTime = findViewById(R.id.tv_time);
        seekBar = findViewById(R.id.seekBar);
        layoutBottom = findViewById(R.id.include_player_bottom);


        init();
    }

    private void init() {
        videoPlayer.setVideoListener(this);
        videoPlayer.setEnableMediaCodec(false);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_REFRESH:
                        if (DEV == 0) {
                            refresh();
                            handler.sendEmptyMessageDelayed(MSG_REFRESH, 500);
                        }

                        //LastChangeAwareServiceManager manager = (LastChangeAwareServiceManager) service.getManager();
                        //manager.fireLastChange();

                        break;
                }
            }
        };

        Log.d(TAG, "bindService: " + getApplicationContext().bindService(new Intent(this, VideoService.class), new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                VideoService videoService = ((VideoService.VideoBinder) service).getService();

                //注册回调接口
                videoService.bindOnDestroy(new VideoServiceEvent() {
                    @Override
                    public void onDestroy() {
                        exit();
                    }

                    @Override
                    public void onNewMedia(URI uri, String metaData) {
                    }
                });
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }

        }, BIND_AUTO_CREATE));


        Intent intent = getIntent();
        URI uri = (URI) intent.getSerializableExtra("uri");
        Log.i(TAG, "getURI: " + uri.toString());

        fadeIn(Loading);
        videoPlayer.setPath(uri.toString());
        try {
            videoPlayer.load();
        } catch (IOException e) {
            Log.e(TAG, "videoPlayer.load: ", e);
        }
    }

    private void exit(){
        videoPlayer.stop();
        videoPlayer.release();
        handler.removeCallbacksAndMessages(null);
        finish();
    }

    private long exitTime = 0;
    private Handler mHandler = new Handler();
    private RunnableSeek runnableSeek;

    int[] DEV_array = {2, 3, 5, 10, 20, 30, 60};
    private int INC = 0;
    private int DEC = 0;
    private long time = 0;
    private int DEV = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            if (videoPlayer.isPlaying()) {
                fadeIn(layoutBottom);
                videoPlayer.pause();
            } else {
                fadeOut(layoutBottom);
                videoPlayer.start();
            }
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            fadeIn(layoutBottom);
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            fadeOut(layoutBottom);
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            if (runnableSeek != null) {
                mHandler.removeCallbacks(runnableSeek);
                runnableSeek = null;
            }
            fadeIn(layoutBottom);

            runnableSeek = new RunnableSeek();
            mHandler.postDelayed(runnableSeek, 500);

            DEV -= DEV_array[DEC];
            if (DEC < DEV_array.length - 1)
                DEC += 1;
            if (INC != 0)
                INC -= 1;

            long current = videoPlayer.getCurrentPosition() / 1000 + DEV;
            long duration = videoPlayer.getDuration() / 1000;
            refresh(current, duration);

            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            if (runnableSeek != null) {
                mHandler.removeCallbacks(runnableSeek);
                runnableSeek = null;
            }
            fadeIn(layoutBottom);

            runnableSeek = new RunnableSeek();
            mHandler.postDelayed(runnableSeek, 500);

            DEV += DEV_array[INC];
            if (INC < DEV_array.length - 1)
                INC += 1;
            if (DEC != 0)
                DEC -= 1;

            long current = videoPlayer.getCurrentPosition() / 1000 + DEV;
            long duration = videoPlayer.getDuration() / 1000;
            refresh(current, duration);

            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), R.string.player_exit,
                        Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                exit();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void refresh() {
        long current = videoPlayer.getCurrentPosition() / 1000;
        long duration = videoPlayer.getDuration() / 1000;
        refresh(current, duration);
    }

    private void refresh(long current, long duration) {
        Log.d(TAG, "refresh: " + current + " " + duration);
        long current_second = current % 60;
        long current_minute = current / 60;
        long total_second = duration % 60;
        long total_minute = duration / 60;
        String time = getTime(current_minute, current_second) + "/" + getTime(total_minute, total_second);
        tvTime.setText(time);
        if (duration != 0) {
            seekBar.setProgress((int) (current * 100 / duration));
        }
    }

    private String getTime(long minute, long second) {
        String min = String.valueOf(minute);
        String sec = String.valueOf(second);
        if (min.length() == 1)
            min = "0" + min;
        if (sec.length() == 1)
            sec = "0" + sec;

        return min + ":" + sec;
    }

    private class RunnableSeek implements Runnable {
        @Override
        public void run() {
            time = videoPlayer.getCurrentPosition() + DEV * 1000L;
            if (time < 0) time = 0;

            Log.d(TAG, "seek old-time: " + videoPlayer.getCurrentPosition());
            Log.d(TAG, "seek new-value: " + DEV * 1000L);
            Log.d(TAG, "seek new-time: " + time);

            INC = 0;
            DEC = 0;
            DEV = 0;

            videoPlayer.seekTo(time);
            handler.sendEmptyMessageDelayed(MSG_REFRESH, 100);
            if (videoPlayer.isPlaying())
                fadeOut(layoutBottom);
        }
    }

    @Override
    public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int i) {

    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {

    }

    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        videoPlayer.start();
        fadeOut(Loading);
        fadeOut(layoutBottom);
        handler.sendEmptyMessageDelayed(MSG_REFRESH, 500);
    }

    @Override
    public void onSeekComplete(IMediaPlayer iMediaPlayer) {

    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i, int i1, int i2, int i3) {

    }

    public static void fadeIn(View view) {
        if (view.getVisibility() == View.VISIBLE) return;

        view.setVisibility(View.VISIBLE);
        Animation animation = new AlphaAnimation(0F, 1F);
        animation.setDuration(300);
        view.startAnimation(animation);
    }

    public static void fadeOut(View view) {
        if (view.getVisibility() != View.VISIBLE) return;

        Animation animation = new AlphaAnimation(1F, 0F);
        animation.setDuration(300);
        view.startAnimation(animation);
        view.setVisibility(View.GONE);
    }
}
