package com.raspberry;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.*;

import androidx.appcompat.app.AppCompatActivity;

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
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.support.avtransport.callback.Play;
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI;
import org.fourthline.cling.support.avtransport.impl.AVTransportService;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportLastChangeParser;
import org.fourthline.cling.support.lastchange.LastChangeAwareServiceManager;
import org.fourthline.cling.support.lastchange.LastChangeParser;

import java.net.URI;


public class MainActivity extends AppCompatActivity {
    private AndroidUpnpService upnpService;

    private final String TAG = "MainActivity";
    private VideoService videoService;

    private final String device = Build.MODEL;
    private final String brand = Build.BRAND;


    private LocalService<AVTransportService> service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView textView = findViewById(R.id.friendlyName);
        bindService();

        textView.setText(device + " - " + getString(R.string.app_name));
        Log.i(TAG, "device: " + device);
        Log.i(TAG, "brand: " + brand);

        final Handler handler = new Handler(Looper.getMainLooper());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                //要做的事情
                LastChangeAwareServiceManager manager = (LastChangeAwareServiceManager) service.getManager();
                manager.fireLastChange();

                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(runnable, 1000);
    }

    private void bindService() {
        // bind video service
        Log.d(TAG, "bindService(VideoService): " + getApplicationContext().bindService(new Intent(this, VideoService.class), new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                videoService = ((VideoService.VideoBinder) service).getService();

                //注册回调接口
                videoService.bindEvent(new VideoServiceEvent() {
                    @Override
                    public void onDestroy() {

                    }

                    @Override
                    public void onNewMedia(URI uri, String metaData) {
                        Log.d(TAG, "onNewMedia(Event): " + uri.toString() + ", metaData: " + metaData);

                        VideoService.destroyAll();

                        Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
                        intent.putExtra("uri", uri);
                        MainActivity.this.startActivity(intent);
                    }
                });
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }

        }, BIND_AUTO_CREATE));

        // bind upnp service
        Log.d(TAG, "bindService(UpnpService): " + getApplicationContext().bindService(new Intent(this, UpnpService.class), new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                upnpService = (AndroidUpnpService) service;

                // Register the device when this activity binds to the service for the first time
                try {
                    upnpService.getRegistry().addDevice(createDevice());


                    Log.i(TAG, "Starting UPnP Server");
                } catch (Exception err) {
                    Log.e(TAG, "Exception: " + err);
                    err.printStackTrace(System.err);
                    System.exit(1);
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                upnpService = null;
            }
        }, BIND_AUTO_CREATE));
    }

    private long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), R.string.app_exit,
                        Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

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

        // 但是，还有一个细节需要考虑:LastChange事件的传播。
        // 当任何播放状态或转换向LastChange添加“更改”时，这些数据将被累积。
        // 它不会立即或自动发送到GENA订户!如何以及何时将所有累积的更改刷新到控制点由你决定。
        // 一种常见的方法是后台线程每秒钟(甚至更频繁地)执行这个操作
        //LastChangeAwareServiceManager manager = (LastChangeAwareServiceManager)service.getManager();
        //manager.fireLastChange();

        /*
        ActionCallback setAVTransportURIAction =
                new SetAVTransportURI(service, "http://10.0.0.1/file.mp3", "NO METADATA") {
                    @Override
                    public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                        // Something was wrong
                    }
                };

        ActionCallback playAction =
                new Play(service) {
                    @Override
                    public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                        // Something was wrong
                    }
                };
        */

        return new LocalDevice(identity, type, details, service);
    }
}
