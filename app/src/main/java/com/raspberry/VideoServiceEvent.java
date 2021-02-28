package com.raspberry;

import java.net.URI;

public interface VideoServiceEvent {
    void onNewMedia(URI uri, String metaData);
    void onDestroy();
}
