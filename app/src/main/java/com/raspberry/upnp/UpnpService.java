package com.raspberry.upnp;

import android.util.Log;
import org.fourthline.cling.android.AndroidUpnpServiceConfiguration;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;

import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.model.types.UDAServiceType;


/**
 * Android Upnp服务
 * 描述提供的服务
 */
public class UpnpService extends AndroidUpnpServiceImpl {

    @Override
    protected AndroidUpnpServiceConfiguration createConfiguration() {
        return new AndroidUpnpServiceConfiguration() {

            @Override
            public int getRegistryMaintenanceIntervalMillis() {
                return 7000;
            }

            @Override
            public ServiceType[] getExclusiveServiceTypes() {
                // only care the these service below
                return new ServiceType[]{
                        new UDAServiceType("AVTransport"),
                        new UDAServiceType("RenderingControl"),
                        new UDAServiceType("ContentDirectory"),
                };
            }

        };
    }

    @Override
    public void onDestroy() {
        Log.d("UpnpService", "onDestroy");
        super.onDestroy();
    }
}