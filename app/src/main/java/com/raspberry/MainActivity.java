package com.raspberry;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.raspberry.upnp.MediaRenderer.MediaRendererNoMedia;
import com.raspberry.upnp.MediaRenderer.MediaRendererStateMachine;
import com.raspberry.upnp.UpnpService;


import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.binding.LocalServiceBindingException;
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.support.avtransport.impl.AVTransportService;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportLastChangeParser;
import org.fourthline.cling.support.lastchange.LastChangeAwareServiceManager;
import org.fourthline.cling.support.lastchange.LastChangeParser;

import java.net.URI;
import java.net.URISyntaxException;


public class MainActivity extends AppCompatActivity {
    private AndroidUpnpService upnpService;

    private final String tag = "MainActivity";
    public static Context context;
    private TextView textView;

    private String device = Build.MODEL;
    private String brand = Build.BRAND;


    private LocalService<AVTransportService> service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.friendlyName);
        init();
    }

    private void init() {
        context = this;
        // bind upnp service
        this.bindService(
                new Intent(this, UpnpService.class),
                serviceConnection,
                Context.BIND_AUTO_CREATE
        );
        textView.setText(device + " - " + getString(R.string.app_name));

        Log.i("init", "device: " + device);
        Log.i("init", "brand: " + brand);

        //debug();
    }

    private void debug() {
        try {
            newMedia(new URI("http://192.168.31.10/Video/3%E5%B9%B4A%E7%B5%84%E2%80%95%E4%BB%8A%E3%81%8B%E3%82%89%E7%9A%86%E3%81%95%E3%82%93%E3%81%AF%E3%80%81%E4%BA%BA%E8%B3%AA%E3%81%A7%E3%81%99/1%5BCA0E76E6%5D.mp4"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static void newMedia(URI uri) {
        Log.d("newMedia", uri.toString());

        Intent intent = new Intent(context, PlayerActivity.class);
        intent.putExtra("uri", uri);
        context.startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            getApplicationContext().unbindService(serviceConnection);
        } catch (IllegalArgumentException err) {
            Log.w(tag, err);
        }
    }

    private long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次退出程序",
                        Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            upnpService = (AndroidUpnpService) service;

            // Register the device when this activity binds to the service for the first time
            try {
                upnpService.getRegistry().addDevice(createDevice());

                Log.i(tag, "Starting UPnP Server");
            } catch (Exception err) {
                Log.e(tag, "Exception: " + err);
                err.printStackTrace(System.err);
                System.exit(1);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            upnpService = null;
        }
    };

    public LocalDevice createDevice() throws ValidationException, LocalServiceBindingException {
        // 设备标识
        //DeviceIdentity identity = new DeviceIdentity(UDN.uniqueSystemIdentifier("MediaRenderer"));
        DeviceIdentity identity = new DeviceIdentity(new UDN("MediaRenderer"));

        // 设备类型，包含包含版本号。此例中是，BinaryLight:1
        DeviceType type = new UDADeviceType("MediaRenderer", 1);

        DeviceDetails details =
                new DeviceDetails(
                        device + " - " + getString(R.string.app_name),
                        new ManufacturerDetails(brand),
                        new ModelDetails(
                                "MediaRenderer",
                                "MediaRenderer",
                                "v1",
                                "https://github.com/you9you/Raspberry"
                        )
                );

        service = new AnnotationLocalServiceBinder().read(AVTransportService.class);

        // Service's which have "logical" instances are very special, they use the
        // "LastChange" mechanism for eventing. This requires some extra wrappers.
        LastChangeParser lastChangeParser = new AVTransportLastChangeParser();

        service.setManager(
                new LastChangeAwareServiceManager<AVTransportService>(service, lastChangeParser) {
                    @Override
                    protected AVTransportService createServiceInstance() throws Exception {
                        return new AVTransportService(
                                MediaRendererStateMachine.class,   // All states
                                MediaRendererNoMedia.class  // Initial state
                        );
                    }
                }
        );
        return new LocalDevice(identity, type, details, service);
    }
}
