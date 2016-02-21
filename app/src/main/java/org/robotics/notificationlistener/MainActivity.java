package org.robotics.notificationlistener;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {

    TableLayout tab;

    public final String prefenceKey = "org.robotics.notificationlistener.PREFERENCES";

    SharedPreferences pref;
    private Toolbar toolbar, bottomBar;
    private boolean connected = false;
    private static final String TAG = "MainActivity";

    // UUIDs for UAT service and associated characteristics.
    public static UUID UART_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID TX_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID RX_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");

    // UUID for the BTLE client characteristic which is necessary for notifications.
    public static UUID CLIENT_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    // UI elements
    //private TextView messages;
    //private EditText input;
    private ViewPager viewPager;
    private PagerAdapter pagerAdapter;


    private Handler handler;

    // BTLE state
    private BluetoothAdapter adapter;
    private BluetoothGatt gatt;
    private BluetoothGattCharacteristic tx;
    private BluetoothGattCharacteristic rx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.tool_bar); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);
        bottomBar = (Toolbar) findViewById(R.id.toolbar_bottom);
        handler = new Handler();
        pref = this.getPreferences(Context.MODE_PRIVATE);
        setBluetoothConnectedText(false);

        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Notifications"));
        tabLayout.addTab(tabLayout.newTab().setText("Raw BT Msg"));
        tabLayout.addTab(tabLayout.newTab().setText("Marco Polo"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        viewPager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        final IntentFilter mIntentFilter = new IntentFilter(MyAccessibilityService.Constants.ACTION_CATCH_NOTIFICATION);
        mIntentFilter.addAction(MyAccessibilityService.Constants.ACTION_CATCH_TOAST);
        registerReceiver(onNotice, mIntentFilter);
        Log.v(TAG, "Receiver registered.");
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, mIntentFilter /*new IntentFilter("Msg")*/);

        adapter = BluetoothAdapter.getDefaultAdapter();

    }

    private BluetoothGattCallback callback = new BluetoothGattCallback() {
        // Called whenever the device connection state changes, i.e. from disconnected to connected.
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                writeLine("Connected!");
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        setBluetoothConnectedText(true);
                    }
                });
                adapter.stopLeScan(scanCallback);
                // Discover services.
                if (!gatt.discoverServices()) {
                    writeLine("Failed to start discovering services!");
                }
            }
            else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                writeLine("Disconnected!");
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        setBluetoothConnectedText(false);
                    }
                });
                adapter.startLeScan(scanCallback);
            }
            else {
                writeLine("Connection state changed.  New state: " + newState);
            }
        }

        // Called when services have been discovered on the remote device.
        // It seems to be necessary to wait for this discovery to occur before
        // manipulating any services or characteristics.
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                writeLine("Service discovery completed!");
            }
            else {
                writeLine("Service discovery failed with status: " + status);
            }
            // Save reference to each characteristic.
            tx = gatt.getService(UART_UUID).getCharacteristic(TX_UUID);
            rx = gatt.getService(UART_UUID).getCharacteristic(RX_UUID);
            // Setup notifications on RX characteristic changes (i.e. data received).
            // First call setCharacteristicNotification to enable notification.
            if (!gatt.setCharacteristicNotification(rx, true)) {
                writeLine("Couldn't set notifications for RX characteristic!");
            }
            // Next update the RX characteristic's client descriptor to enable notifications.
            if (rx.getDescriptor(CLIENT_UUID) != null) {
                BluetoothGattDescriptor desc = rx.getDescriptor(CLIENT_UUID);
                desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                if (!gatt.writeDescriptor(desc)) {
                    writeLine("Couldn't write RX client descriptor value!");
                }
            }
            else {
                writeLine("Couldn't get RX client descriptor!");
            }
        }

        // Called when a remote characteristic changes (like the RX characteristic).
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            writeLine("Received: " + characteristic.getStringValue(0));
            Log.d("TEST_MSG", "inside the switch block");
            switch (characteristic.getStringValue(0).charAt(0)){
                case 'a': /*method call*/
                    Log.d("TEST_MSG", "inside A");
                    break;
                case 'b': /*method call*/
                    Log.d("TEST_CALL","inside B");
                    break;
                case 'c': /*method call*/
                    break;

            }
        }

    };

    private BluetoothAdapter.LeScanCallback scanCallback = new BluetoothAdapter.LeScanCallback() {
        // Called when a device is found.
        @Override
        public void onLeScan(final BluetoothDevice bluetoothDevice, int i, final byte[] bytes) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    writeLine("Found device: " + bluetoothDevice.getAddress());
                    // Check if the device has the UART service.
                    if (parseUUIDs(bytes).contains(UART_UUID)) {
                        // Found a device, stop the scan.
                        adapter.stopLeScan(scanCallback);
                        writeLine("Found UART service!");
                        // Connect to the device.
                        // Control flow will now go to the callback functions when BTLE events occur.
                        gatt = bluetoothDevice.connectGatt(getApplicationContext(), false, callback);
                    }
                }
            });
          thread.start();
        }
    };



    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unregisterReceiver(onNotice);
    }



    public void handleMessageReceived(String sender, String message){
       // Log.d("TEST","Message Handler");
        Log.d(TAG, "Received message from "+ sender);

        final PackageManager pm = getPackageManager();
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo( sender, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            ai = null;
        }
        String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");

        Fragment f = (Fragment) pagerAdapter.instantiateItem(viewPager, 0);
        if (f instanceof NotificationsFragment && message != null) {
            ((NotificationsFragment) f).addNotificationRow(applicationName,message);
        }

        applicationName = applicationName.toLowerCase();
        message = message.toLowerCase();
        sender = sender.toLowerCase();

        if (!pref.getBoolean(applicationName,false))
            return;

        if (sender.equalsIgnoreCase("maps")){
            if (message.contains("left") && message.contains("50")){
                vibrate("left", 1000,0);
                vibrate("left", 1000,500);
            }else if(message.contains("right") && message.contains("50")){
                vibrate("right", 1000,0);
                vibrate("left", 1000,500);
            }
        }

        else {
            vibrate("both", 3000,0);
        }
    }
    // OnResume, called right before UI is displayed.  Start the BTLE connection.
    @Override
    protected void onResume() {
        super.onResume();
        // Scan for all BTLE devices.
        // The first one with the UART service will be chosen--see the code in the scanCallback.
        writeLine("Scanning for devices...");
        adapter.startLeScan(scanCallback);
    }

    // OnStop, called right before the activity loses foreground focus.  Close the BTLE connection.
    @Override
    protected void onStop() {
        super.onStop();
        if (gatt != null) {
            // For better reliability be careful to disconnect and close the connection.
            gatt.disconnect();
            gatt.close();
            gatt = null;
            tx = null;
            rx = null;
        }
    }
    // Handler for mouse click on the send button.



    public void sendClick(String message) {
        //String message = input.getText().toString();
        if (tx == null || message == null || message.isEmpty()) {
            // Do nothing if there is no device or message to send.
            return;
        }
        // Update TX characteristic value.  Note the setValue overload that takes a byte array must be used.
        tx.setValue(message.getBytes(Charset.forName("UTF-8")));
        if (gatt.writeCharacteristic(tx)) {
            writeLine("Sent: " + message);
        }
        else {
            writeLine("Couldn't write TX characteristic!");
        }
    }

    public boolean sendBTMessage(String message){
        Log.d(TAG, "Sending BT Message: " + message);
        if (tx == null || message == null)
            return false;
        tx.setValue(message);
        if (gatt.writeCharacteristic(tx)) {
            writeLine("Sent: " + message);
            return true;
        } else {
            writeLine("Failed to Send: "+ message);
        }
        return false;
    }

    // Write some text to the messages text view.
    // Care is taken to do this on the main UI thread so writeLine can be called
    // from any thread (like the BTLE callback).
    private void writeLine(final CharSequence text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Fragment f = (Fragment) pagerAdapter.instantiateItem(viewPager, viewPager.getCurrentItem());
                if (f instanceof RawBTFragment && f != null && text != null) {
                    ((RawBTFragment) f).addMessage(text.toString());
                    Log.d(TAG, text.toString());
                }
            }
        });
    }

    // Filtering by custom UUID is broken in Android 4.3 and 4.4, see:
    //   http://stackoverflow.com/questions/18019161/startlescan-with-128-bit-uuids-doesnt-work-on-native-android-ble-implementation?noredirect=1#comment27879874_18019161
    // This is a workaround function from the SO thread to manually parse advertisement data.
    private List<UUID> parseUUIDs(final byte[] advertisedData) {
        List<UUID> uuids = new ArrayList<UUID>();

        int offset = 0;
        while (offset < (advertisedData.length - 2)) {
            int len = advertisedData[offset++];
            if (len == 0)
                break;

            int type = advertisedData[offset++];
            switch (type) {
                case 0x02: // Partial list of 16-bit UUIDs
                case 0x03: // Complete list of 16-bit UUIDs
                    while (len > 1) {
                        int uuid16 = advertisedData[offset++];
                        uuid16 += (advertisedData[offset++] << 8);
                        len -= 2;
                        uuids.add(UUID.fromString(String.format("%08x-0000-1000-8000-00805f9b34fb", uuid16)));
                    }
                    break;
                case 0x06:// Partial list of 128-bit UUIDs
                case 0x07:// Complete list of 128-bit UUIDs
                    // Loop through the advertised 128-bit UUID's.
                    while (len >= 16) {
                        try {
                            // Wrap the advertised bits and order them.
                            ByteBuffer buffer = ByteBuffer.wrap(advertisedData, offset++, 16).order(ByteOrder.LITTLE_ENDIAN);
                            long mostSignificantBit = buffer.getLong();
                            long leastSignificantBit = buffer.getLong();
                            uuids.add(new UUID(leastSignificantBit,
                                    mostSignificantBit));
                        } catch (IndexOutOfBoundsException e) {
                            // Defensive programming.
                            //Log.e(LOG_TAG, e.toString());
                            continue;
                        } finally {
                            // Move the offset to read the next uuid.
                            offset += 15;
                            len -= 16;
                        }
                    }
                    break;
                default:
                    offset += (len - 1);
                    break;
            }
        }
        return uuids;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void setBluetoothConnectedText(boolean on){
        TextView mTitle = (TextView) bottomBar.findViewById(R.id.bottom_title);
        connected = on;
        if (!on) {
            mTitle.setText("BLUETOOTH DISCONNECTED");
            mTitle.setTextColor(Color.RED);
        } else {
            mTitle.setText("BLUETOOTH CONNECTED");
            mTitle.setTextColor(Color.GREEN);
        }
    }

    public void vibrate(String side, int millis, int delay){
        String payload = "00";
        if (side.equalsIgnoreCase("left")){
            payload = "01";
        }else if (side.equalsIgnoreCase("right")){
            payload = "10";
        }
        else if (side.equalsIgnoreCase("both")){
            payload = "11";
        }
        final String pl = payload;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendBTMessage(pl);
            }
        }, delay);
        sendBTMessage(payload);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendBTMessage("00");
            }
        }, millis);
    }

    private BroadcastReceiver onNotice = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String pack = intent.getStringExtra("package");
            String title = intent.getStringExtra("title");
            String text = intent.getStringExtra("text");

            if (pack == null)
                pack = intent.getStringExtra(MyAccessibilityService.Constants.EXTRA_PACKAGE);
            if (text == null)
                text =  intent.getStringExtra(MyAccessibilityService.Constants.EXTRA_MESSAGE);



            handleMessageReceived(pack, text);

            Log.v(TAG, "Received message");
            Log.v(TAG, "intent.getAction() :: " + intent.getAction());
            Log.v(TAG, "intent.getStringExtra(Constants.EXTRA_PACKAGE) :: " + intent.getStringExtra(MyAccessibilityService.Constants.EXTRA_PACKAGE));
            Log.v(TAG, "intent.getStringExtra(Constants.EXTRA_MESSAGE) :: " + intent.getStringExtra(MyAccessibilityService.Constants.EXTRA_MESSAGE));
        }
    };

}




