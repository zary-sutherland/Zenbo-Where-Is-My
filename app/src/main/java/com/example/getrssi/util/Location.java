package com.example.getrssi.util;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Objects;

public class Location implements Serializable {
    public int id;
    public String name;
    // TODO: INCLUDE IMAGE

    public Location() {}

    public Location(JSONObject locationJSON) {
        try {
            this.id = locationJSON.getInt("id");
            this.name = locationJSON.getString("name");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject toJSON() {
        JSONObject locationObj = new JSONObject();

        try {
            if (this.id > 0) {
                locationObj.put("id", this.id);
            }
            locationObj.put("name", this.name);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return locationObj;
    }

    @Override
    @NonNull
    public String toString() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return this.id == location.id &&
                this.name.equals(location.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
