package tech.fedorov.fedchatclient.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import tech.fedorov.fedchatclient.Messages.Message;
import tech.fedorov.fedchatclient.R;


public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ViewHolder>{
    private ArrayList<Message> mData;

    private LayoutInflater mInflater;
    private Context activityContext;
    private boolean hasGeo = false;

    // data is passed into the constructor
    public MessageListAdapter(Context context, ArrayList<Message> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.activityContext = context;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.message_item_cardview, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Message msg = mData.get(position);
        String text = msg.text;
        String name = msg.username;
        String dataTime = msg.time;
        holder.messageText.setText(text);
        holder.username.setText(name);
        holder.dataTime.setText(dataTime);
        if (msg.geo != null) {
            hasGeo = true;
            Log.d("INF", "i have geo: " + msg.geo);
            holder.mapView.setVisibility(View.VISIBLE);

            holder.bindMap(position);
        } else {
            holder.mapView.setVisibility(View.GONE);
            Log.d("INF", "i havent got geo");
        }
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements OnMapReadyCallback {
        TextView messageText;
        TextView username;
        TextView dataTime;
        MapView mapView;
        View layout;
        LatLng geolocation = new LatLng(55.74356948607958, 37.68156059562104);
        public GoogleMap map;

        ViewHolder(View itemView) {
            super(itemView);
            layout = itemView;
            messageText = layout.findViewById(R.id.message);
            username = layout.findViewById(R.id.username);
            dataTime = layout.findViewById(R.id.time);
            mapView = layout.findViewById(R.id.mapGeoloc);
            Log.d("INF", mapView.getVisibility()+" - visibility");
            // && and VISIBILITY
            if (mapView != null) {
                Log.d("INF", "MapView is not null");
                // Initialise the MapView
                mapView.onCreate(null);
                // Set the map ready callback to receive the GoogleMap object
                mapView.getMapAsync(this);
            }
        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            MapsInitializer.initialize(activityContext);
            map = googleMap;
            Log.d("INF", "getting googleMap object");
            if (hasGeo) {
                setMapLocation(geolocation);
            }
        }

        private void setMapLocation(LatLng geolocation) {
            if (map == null) {
                Log.d("INF", "map is null");
                return;
            }

            Log.d("INF", "map is NOT null");
            if (geolocation == null) {
                Log.d("INF", "geolocation is null");
                return;
            }
            Log.d("INF", geolocation.latitude + ":" + geolocation.longitude);
            // Add a marker for this item and set the camera
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(geolocation, 16f));
            map.addMarker(new MarkerOptions().position(geolocation));

            // Set the map type back to normal.
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }

        private void bindMap(int pos) {
            String geolocation = mData.get(pos).geo;
            Log.d("INF", "binding map...");
            String[] latlng = geolocation.split(":");
            double latitude = Double.parseDouble(latlng[0]);
            double longitude = Double.parseDouble(latlng[1]);
            LatLng item = new LatLng(latitude, longitude);
            Log.d("INF", "setting map location...");
            this.geolocation = item;
            setMapLocation(this.geolocation);
            messageText.setText("I am here:");
        }
    }
}

