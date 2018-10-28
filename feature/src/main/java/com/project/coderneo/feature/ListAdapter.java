package com.project.coderneo.feature;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class ListAdapter extends ArrayAdapter<Camera> {

    private int resourceLayout;
    private Context mContext;

    public ListAdapter(Context context, int resource, List<Camera> items) {
        super(context, resource, items);
        this.resourceLayout = resource;
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(mContext);
            v = vi.inflate(resourceLayout, null);
        }

        Camera camera = getItem(position);

        if (camera != null) {
            TextView txtModel = (TextView) v.findViewById(R.id.txtModel);
            TextView txtPort = (TextView) v.findViewById(R.id.txtPort);

            if (txtModel != null) {
                txtModel.setText(camera.getModel());
            }
            if (txtPort != null) {
                txtPort.setText(camera.getPort());
            }
            if(camera.getRecordingStatus() != null){
                Log.d("LOG", camera.getRecordingStatus());
                if(camera.getRecordingStatus().equalsIgnoreCase("1")){
                    v.setBackgroundResource(R.color.light_red);
                }else {
                    v.setBackgroundResource(R.color.light_green);
                }
            }

        }

        return v;
    }

}
