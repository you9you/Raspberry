package com.raspberry.upnp.MediaRenderer;

import com.raspberry.MainActivity;

import org.fourthline.cling.Main;
import org.fourthline.cling.support.avtransport.impl.state.AbstractState;
import org.fourthline.cling.support.avtransport.impl.state.NoMediaPresent;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable;
import org.fourthline.cling.support.model.AVTransport;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;

import java.net.URI;

public class MediaRendererNoMedia extends NoMediaPresent {
    public MediaRendererNoMedia(AVTransport transport){
        super(transport);
    }


    @Override
    public Class<? extends AbstractState> setTransportURI(URI uri, String metaData){
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
}
