package com.example.getrssi;

import android.content.DialogInterface;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;

import com.asus.robotframework.API.RobotAPI;
import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotErrorCode;
import com.asus.robotframework.API.RobotFace;
import com.example.getrssi.util.BTDevice;
import com.example.getrssi.util.HttpUtils;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.robot.asus.robotactivity.RobotActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends RobotActivity {
    private static final String TAG = "MainActivity";
    public final static String DOMAIN = "4BF19BF00E604E61AC287ED125FB48C7";
    public final static String PLAN = "launchGetRSSI";

    private static RobotAPI staticRobotAPI;
    private static String alertMsg;

    private Button btnItemList, btnRegisterItem;
    private AlertDialog alertDialog;

    private static List<BTDevice> itemList = new ArrayList<>();

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


    public MainActivity() {
        super(robotCallback, robotListenCallback);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        staticRobotAPI = robotAPI;

        btnItemList = findViewById(R.id.btn_item_list);
        btnItemList.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent itemListIntent = new Intent(getApplicationContext(), ItemListActivity.class);
                startActivity(itemListIntent);
            }
        });

        btnRegisterItem = findViewById(R.id.btn_register_new_item);
        btnRegisterItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(getApplicationContext(), RegisterNewItemActivity.class);
                startActivity(registerIntent);
            }
        });

        getItems();
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Close face
        robotAPI.robot.setExpression(RobotFace.HIDEFACE);

        // Jump to dialog domain plan
        // Dunno if this works
        robotAPI.robot.jumpToPlan(DOMAIN, PLAN);

        String tts = "Hi I'm Zenbo. I can help you find what you are looking for.";
        robotAPI.robot.speak(tts);
    }

    public void getItems() {
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
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                createAlertDialog();
            }
        });
    }


    public static void checkForItem(String userRequest) {
        // TODO: parse user request for requested item
        String requestedItemName = "TestItem".toLowerCase();

        for (BTDevice item : itemList) {
            if (requestedItemName.equals(item.registeredName.toLowerCase())) {
                Log.d(TAG, "Item found");

                String ttsResponse = String.format("%s was last found at %s.", item.registeredName, item.getLastLocation().name);
                staticRobotAPI.robot.speak(ttsResponse);
                break;
            }
        }
    }




    // HELPER FUNCTIONS
    private void createAlertDialog() {
        // Set up alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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
}
