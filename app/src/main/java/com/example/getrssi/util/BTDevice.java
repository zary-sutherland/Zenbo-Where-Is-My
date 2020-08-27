package com.example.getrssi.util;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BTDevice implements Serializable {
    public int id;
    public String deviceName;
    public String registeredName;
    public int rssi = -999;
    public List<Location> locations = new ArrayList<>();

    public BTDevice() {}

    public BTDevice(JSONObject deviceJSON) {
        try {
            this.id = deviceJSON.getInt("id");
            this.deviceName = deviceJSON.getString("deviceName");
            this.registeredName = deviceJSON.getString("registeredName");
            JSONArray locations = deviceJSON.getJSONArray("locations");
            for (int i = 0; i < locations.length(); i++) {
                this.locations.add(new Location(locations.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Location getLastLocation() {
        int lastIndex = this.locations.size() - 1;
        System.out.println(lastIndex);
        Location lastLocation = lastIndex >= 0 ? this.locations.get(lastIndex) : null;
        return lastLocation;
    }

    public JSONObject toJSON() {
        JSONObject deviceObj = new JSONObject();

        try {
            deviceObj.put("id", this.id);
            deviceObj.put("deviceName", this.deviceName);
            deviceObj.put("registeredName", this.registeredName);
            JSONArray locationsArr = new JSONArray();
            for (Location l : this.locations) {
                locationsArr.put(l.toJSON());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return deviceObj;
    }

    @Override
    @NonNull
    public String toString() {
        return (this.registeredName != null ? this.registeredName : this.deviceName);
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BTDevice device = (BTDevice) o;
        return this.deviceName.equals(device.deviceName);
    }
}
