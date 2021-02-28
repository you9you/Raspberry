package com.raspberry;

import android.app.Service;
import android.content.Intent;

import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;


public class VideoService extends Service {
    private final String TAG = "VideoService";

    // onDestroy
    private static List<VideoServiceEvent> eventList = new ArrayList<>();

    private static VideoServiceEvent event = new VideoServiceEvent() {
        // default
        @Override
        public void onNewMedia(URI uri, String metaData) {
            Log.d("VideoService", "onNewMedia(default): " + uri.toString());
        }

        @Override
        public void onDestroy() {
            Log.d("VideoService", "onDestroy(default)");
        }
    };


    public class VideoBinder extends Binder {
        VideoService getService() {
            return VideoService.this;
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        return super.onStartCommand(intent, flags, startId);
    }

    public void bindOnDestroy(VideoServiceEvent event) {
        Log.d(TAG, "bindEvent");

        eventList.add(event);
    }

    public static void destroyAll() {
        for (VideoServiceEvent event : eventList) {
            event.onDestroy();
        }
        eventList.clear();
    }

    public void bindEvent(VideoServiceEvent event) {
        Log.d(TAG, "bindEvent");
        VideoService.event = event;
    }

    public static VideoServiceEvent getEvent() {
        return event;
    }


    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        //  通过 Binder 来保持 Activity 和 Service 的通信
        return new VideoBinder();
    }
}