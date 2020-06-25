package com.raspberry.upnp.MediaRenderer;

import org.fourthline.cling.support.avtransport.impl.AVTransportStateMachine;
import org.seamless.statemachine.States;

@States({
        MediaRendererNoMedia.class,
        MediaRendererStopped.class,
        MediaRendererPlaying.class,
        MediaRendererPausedPlay.class
})
public interface MediaRendererStateMachine extends AVTransportStateMachine{}
