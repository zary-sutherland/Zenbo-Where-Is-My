package com.example.getrssi;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;

import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotErrorCode;
import com.example.getrssi.util.BTDevice;
import com.example.getrssi.util.DeviceListAdapter;
import com.example.getrssi.util.HttpUtils;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.robot.asus.robotactivity.RobotActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class ItemListActivity extends RobotActivity {
    private static final String TAG = "ItemListActivity";
    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DISCOVER_BT = 1;
    private static final int FIND_ITEM_ACTIVITY = 2;

    private static String alertMsg;

    private List<BTDevice> itemList = new ArrayList<>();
    private ArrayAdapter<BTDevice> arrayAdapter;

    private ProgressBar progressItemList;
    private Button btnReloadList;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        progressItemList = findViewById(R.id.progress_item_list);
        btnReloadList = findViewById(R.id.btn_reload_list);
        btnReloadList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getItems();
            }
        });

        final ListView listViewItemList = findViewById(R.id.listview_registered_items);
        arrayAdapter = new DeviceListAdapter(this, R.layout.device_item, itemList);
        listViewItemList.setAdapter(arrayAdapter);
        listViewItemList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BTDevice selectedItem = (BTDevice) listViewItemList.getItemAtPosition(position);
                Intent intent = new Intent(getApplicationContext(), FindItemActivity.class);
                intent.putExtra("deviceObj", (Serializable) selectedItem);
                startActivityForResult(intent, FIND_ITEM_ACTIVITY);
            }
        });

        getItems();
    }


    public void getItems() {
        btnReloadList.setVisibility(View.GONE);
        progressItemList.setVisibility(View.VISIBLE);
        HttpUtils.get("items", new RequestParams(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.d(TAG, response.toString());
                try {
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject obj = response.getJSONObject(i);
                        BTDevice item = new BTDevice(obj);
                        itemList.add(item);
                    }
                    progressItemList.setVisibility(View.GONE);

                    String ttsResponse;
                    if (itemList.size() != 0) {
                        ttsResponse = "These are all of the items I have saved. Select one of them to begin searching for it.";
                    } else {
                        ttsResponse = "It looks like you have not registered any items yet. You should register an item before trying to search for it.";
                    }
                    robotAPI.robot.speak(ttsResponse);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                try {
                    alertMsg = errorResponse != null
                            ? errorResponse.get("message").toString()
                            : "Connection timeout occurred";
                    progressItemList.setVisibility(View.GONE);
                    btnReloadList.setVisibility(View.VISIBLE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                createAlertDialog();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case FIND_ITEM_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    // Do something
                } else if (resultCode == RESULT_CANCELED) {
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }




    // HELPER FUNCTIONS
    private void createAlertDialog() {
        // Set up alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(ItemListActivity.this);
        builder.setTitle("Error Occurred");
        builder.setMessage(alertMsg);
        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Close the alert dialog
                dialog.cancel();
            }
        });

        // Create and show the alert dialog
        alertDialog = builder.create();
        alertDialog.show();
    }



    // ROBOT FUNCTIONS
    public static RobotCallback robotCallback = new RobotCallback() {
        @Override
        public void onResult(int cmd, int serial, RobotErrorCode err_code, Bundle result) {
            super.onResult(cmd, serial, err_code, result);;
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

    public ItemListActivity() {
        super(robotCallback, robotListenCallback);
    }
}
