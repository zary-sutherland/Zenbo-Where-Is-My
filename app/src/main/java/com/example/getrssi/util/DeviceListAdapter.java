package com.example.getrssi.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.getrssi.R;
import com.example.getrssi.util.BTDevice;

import java.util.List;

public class DeviceListAdapter extends ArrayAdapter<BTDevice> {
    private int mResource;
    private Context mContext;
    private List<BTDevice> devices;

    public DeviceListAdapter(@NonNull Context context, int resource, @NonNull List objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
        devices = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        BTDevice device = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(mResource, parent, false);
        }

        // Lookup view for data population
        TextView textView = convertView.findViewById(R.id.textview_device_item);

        // Populate the data into the template view using the data object
        textView.setText(device.toString());

        // Return the completed view to render on screen
        return convertView;
    }
}
