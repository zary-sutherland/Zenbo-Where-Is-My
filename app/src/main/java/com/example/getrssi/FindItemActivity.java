package com.example.getrssi;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotErrorCode;
import com.asus.robotframework.API.RobotFace;
import com.example.getrssi.util.BTDevice;
import com.example.getrssi.util.HttpUtils;
import com.example.getrssi.util.Location;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.robot.asus.robotactivity.RobotActivity;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class FindItemActivity extends RobotActivity {
    private static final String TAG = "FindItemActivity";
    private static final int REQUEST_DISCOVER_BT = 1;

    private final Context context = this;

    private ProgressBar progressBarSpinner;
    private Button btnCancel, btnFindItem;
    private TextView textViewRegisteredName, textViewDeviceName, textViewRSSIValue, textViewLastLocation;
    private BluetoothAdapter BTAdapter;

    private boolean itemDiscoveredFlag = false, isReceiverRegistered = false;;
    private int initialRSSI, previousStrength, followCommandSerialNumber;
    private String selectedDevName;
    private BTDevice itemObj;

    public static RobotCallback robotCallback = new RobotCallback() {
        @Override
        public void onResult(int cmd, int serial, RobotErrorCode err_code, Bundle result) {
            super.onResult(cmd, serial, err_code, result);
        }

        @Override
        public void onStateChange(int cmd, int serial, RobotErrorCode err_code, RobotCmdState state) {
            super.onStateChange(cmd, serial, err_code, state);
        }

        @Override
        public void initComplete() {
            super.initComplete();

        }
    };


    public static RobotCallback.Listen robotListenCallback = new RobotCallback.Listen() {
        @Override
        public void onFinishRegister() {

        }

        @Override
        public void onVoiceDetect(JSONObject jsonObject) {

        }

        @Override
        public void onSpeakComplete(String s, String s1) {

        }

        @Override
        public void onEventUserUtterance(JSONObject jsonObject) {

        }

        @Override
        public void onResult(JSONObject jsonObject) {

        }

        @Override
        public void onRetry(JSONObject jsonObject) {

        }
    };

    public FindItemActivity(){
        super(robotCallback, robotListenCallback);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_item);
        BTAdapter = BluetoothAdapter.getDefaultAdapter();

        Intent intent = getIntent();
        itemObj = (BTDevice)intent.getSerializableExtra("deviceObj");
        selectedDevName = itemObj.deviceName;

        initialRSSI = itemObj.rssi;
        previousStrength = initialRSSI;

        textViewRegisteredName = findViewById(R.id.textview_item_registered_name);
        textViewRegisteredName.setText(itemObj.registeredName);
        textViewDeviceName = findViewById(R.id.textview_item_device_name);
        textViewDeviceName.setText(itemObj.deviceName);
        textViewRSSIValue = findViewById(R.id.textview_rssi_value);
        String rssi = String.valueOf(itemObj.rssi);
        textViewRSSIValue.setText(rssi + " DBM");
        textViewLastLocation = findViewById(R.id.textview_last_location);
        Location lastLocation = itemObj.getLastLocation();
        if (lastLocation != null) textViewLastLocation.setText(lastLocation.name);

        progressBarSpinner = (ProgressBar) findViewById(R.id.progress_find_item);

        btnCancel = findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                BTAdapter.cancelDiscovery();
                progressBarSpinner.setVisibility(View.INVISIBLE);
                Log.d(TAG, "Cancel Selection");
                Intent cancelSearch = new Intent(FindItemActivity.this, MainActivity.class);
                setResult(RESULT_CANCELED, cancelSearch);
                FindItemActivity.this.finish();
            }
        });

        btnFindItem = (Button)findViewById(R.id.btn_find_item);
        btnFindItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Finding Item");

                String tts = "Okay I'm looking for it now. I'll tell you when I'm close.";
                robotAPI.robot.speak(tts);
//                followCommandSerialNumber = robotAPI.utility.followUser();
                scanDevices();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        robotAPI.robot.setExpression(RobotFace.HIDEFACE);
    }

//    protected void onPause() {
//        super.onPause();
//        unregisterReceiver(receiver);
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (isReceiverRegistered) {
            unregisterReceiver(receiver);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUEST_DISCOVER_BT:
                if (resultCode == RESULT_OK) {
                    showToast("Scan started");
                }
                else {
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkBTPermissions() {
        int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
        permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
        if (permissionCheck != 0) {

            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
        }
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void scanDevices(){
//        // Make device discoverable
//        if (!BTAdapter.isDiscovering()) {
//            showToast("Making Your Device Discoverable");
//            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
//            startActivityForResult(discoverableIntent, REQUEST_DISCOVER_BT);
//        }

        if (!isReceiverRegistered) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(receiver, filter);
            isReceiverRegistered = true;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkBTPermissions();
            }
        }
        progressBarSpinner.setVisibility(View.VISIBLE);
        BTAdapter.startDiscovery();
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.d(TAG, "Discovery started");
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG, "Discovery finished");
                if (!itemDiscoveredFlag) {
                    BTAdapter.startDiscovery();
                }
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                int updatedRSSI = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                if (name == null) {
                    BluetoothDevice dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    name = dev.getAddress();
                }

                if (name.equals(selectedDevName)) {
                    textViewRSSIValue.setText(updatedRSSI + " DBM");
                    if (updatedRSSI < -50) {
//                        robotAPI.robot.speak("rssi is less than -50");
                        // if (updatedRSSI - 10) < previousStrength
                        //      turn left
                        // while cannot move forward (detect object in front)
                        //      turn left
                        // move 1 metre forward
                        if ((updatedRSSI - 10) < previousStrength && updatedRSSI < previousStrength) {
//                            robotAPI.motion.moveBody(0, 0, (float) 1.57);
//                            int random = new Random().nextInt(2);
//                            switch (random) {
//                                case 0:
//                                    robotAPI.motion.moveBody(0, 0, (float) 1.57);
//                                    break;
//                                case 1:
//                                    robotAPI.motion.moveBody(0, 0, (float) -1.57);
//                                    break;
//                            }
                        } else {
//                            robotAPI.robot.speak("move 1m forward");
                            robotAPI.motion.moveBody(0, 1, 0);
                        }
                        previousStrength = updatedRSSI;
//                        scanDevices();
//                        BTAdapter.cancelDiscovery();
//                        BTAdapter.startDiscovery();
                    } else {
                        // Stop moving
                        robotAPI.cancelCommandAll();
                        robotAPI.motion.stopMoving();

                        // Trigger flag
                        itemDiscoveredFlag = true;

                        // TTS response and prompt user to save location
                        String ttsResponse = String.format("Your %s is in 1m range. Enter the location where you found it and press save.", itemObj.registeredName);
                        robotAPI.robot.speak(ttsResponse);
                        progressBarSpinner.setVisibility(View.INVISIBLE);
                        BTAdapter.cancelDiscovery();
                        saveLocation();
                    }
                }

            }
//        }
        }
    };


    public void saveLocation() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_location, null);
        dialogBuilder.setView(dialogView);

        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        Button btnSaveLocation = dialogView.findViewById(R.id.btn_save_location);
        Button btnCancelSaveLocation = dialogView.findViewById(R.id.btn_cancel_save);

        btnSaveLocation.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editTextLocationFound = dialogView.findViewById(R.id.edittext_location_found);
                String locationFound = editTextLocationFound.getText().toString();
                Location loc = new Location();
                loc.name = locationFound;

                StringEntity locationEntity = null;
                try {
                    locationEntity = new StringEntity(loc.toJSON().toString());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                String postUrl = String.format("%d/addLocation", itemObj.id);

                HttpUtils.post(context, postUrl, locationEntity, "application/json", new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Log.d(TAG, response.toString());

                        String ttsResponse = "Okay, I'll remember this is where I last found the item for you.";
                        robotAPI.robot.speak(ttsResponse);
                        alertDialog.dismiss();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        Log.d(TAG, errorResponse.toString());
                        alertDialog.dismiss();
                    }
                });
            }
        });

        btnCancelSaveLocation.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });
    }
}
