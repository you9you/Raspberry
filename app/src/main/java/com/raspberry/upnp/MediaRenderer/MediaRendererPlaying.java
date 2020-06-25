package com.raspberry.upnp.MediaRenderer;

import android.content.Intent;

import com.raspberry.MainActivity;

import org.fourthline.cling.support.avtransport.impl.state.AbstractState;
import org.fourthline.cling.support.avtransport.impl.state.Playing;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable;
import org.fourthline.cling.support.model.AVTransport;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.SeekMode;

import java.net.URI;

public class MediaRendererPlaying extends Playing {
    public MediaRendererPlaying(AVTransport transport) {
        super(transport);
    }

    @Override
    public void onEntry() {
        super.onEntry();
        // Start playing now!
    }

    @Override
    public Class<? extends AbstractState> setTransportURI(URI uri, String metaData) {
        getTransport().setMediaInfo(new MediaInfo(uri.toString(), metaData));

        // if you can, you should find and set the duration of the track here!
        getTransport().setPositionInfo(new PositionInfo(1, metaData, uri.toString()));

        // it's up to you what "last changes" you want to announce to event listeners
        getTransport().getLastChange().setEventedValue(
                getTransport().getInstanceId(),
                new AVTransportVariable.AVTransportURI(uri),
                new AVTransportVariable.CurrentTrackURI(uri)
        );

        MainActivity.newMedia(uri);
        return MediaRendererStopped.class;
    }

    @Override
    public Class<? extends AbstractState> stop() {
        // Stop playing!
        //MainActivity.videoPlayer.stop();
        return MediaRendererStopped.class;
    }

    @Override
    public Class<? extends AbstractState> play(String s) {
        return MediaRendererPlaying.class;
    }

    @Override
    public Class<? extends AbstractState> pause() {
        System.out.println("pause");
        return MediaRendererPausedPlay.class;
    }

    @Override
    public Class<? extends AbstractState> next() {
        return null;
    }

    @Override
    public Class<? extends AbstractState> previous() {
        return null;
    }

    @Override
    public Class<? extends AbstractState> seek(SeekMode seekMode, String s) {
        System.out.println("seek:" + seekMode.ordinal() + "------" + s);

        return MediaRendererPausedPlay.class;
    }
}
