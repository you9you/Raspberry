package com.raspberry.upnp.MediaRenderer;

import com.raspberry.VideoService;

import org.fourthline.cling.support.avtransport.impl.state.PausedPlay;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable;
import org.fourthline.cling.support.model.AVTransport;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;

import java.net.URI;

public class MediaRendererPausedPlay extends PausedPlay {
    public MediaRendererPausedPlay(AVTransport transport) {
        super(transport);
    }

    @Override
    public Class<?> setTransportURI(URI uri, String metaData) {
        getTransport().setMediaInfo(new MediaInfo(uri.toString(), metaData));

        // if you can, you should find and set the duration of the track here!
        getTransport().setPositionInfo(new PositionInfo(1, metaData, uri.toString()));

        // it's up to you what "last changes" you want to announce to event listeners
        getTransport().getLastChange().setEventedValue(
                getTransport().getInstanceId(),
                new AVTransportVariable.AVTransportURI(uri),
                new AVTransportVariable.CurrentTrackURI(uri)
        );

        VideoService.getEvent().onNewMedia(uri, metaData);
        //MainActivity.newMedia(uri);

        return MediaRendererStopped.class;
    }

    @Override
    public Class<?> stop() {
        /// Same here, if you are stopped already and someone calls STOP, well...
        VideoService.destroyAll();
        return MediaRendererStopped.class;
    }

    @Override
    public Class<?> play(String s) {
        System.out.println("play");
        return MediaRendererPlaying.class;
    }
}
