package tech.fedorov.fedchatclient;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

import tech.fedorov.fedchatclient.Adapters.MessageListAdapter;
import tech.fedorov.fedchatclient.Messages.Message;
import tech.fedorov.fedchatclient.Utils.PermissionUtils;

public class MainActivity extends AppCompatActivity {
    private MessageListAdapter adapter;
    private ArrayList<Message> messages = new ArrayList<>();
    public ClientConnection clientConnection;
    private RecyclerView recyclerView;
    private String server_ip;
    private String server_port;
    private ImageButton sendButton;
    private ImageButton attachButton;
    private ImageButton goBackButton;
    private EditText userMessage;
    private String username;
    private Bundle arguments;
    private TextView dotsConnecting;
    private ImageView isConnected;
    private ImageView connectionFailed;
    private Thread connectAnimation;
    private Gson gson;
    private boolean firstConnection = true;
    private PopupMenu attachMenu;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private boolean locationPermissionGranted;
    private Location lastKnownLocation;
    private LatLng defaultLatLng = new LatLng(55.74356948607958, 37.68156059562104);

    private RecyclerView.RecyclerListener mRecycleListener = new RecyclerView.RecyclerListener() {

        @Override
        public void onViewRecycled(RecyclerView.ViewHolder holder) {
            MessageListAdapter.ViewHolder mapHolder = (MessageListAdapter.ViewHolder) holder;
            if (mapHolder != null && mapHolder.map != null) {
                // Clear the map and free up resources by changing the map type to none.
                // Also reset the map when it gets reattached to layout, so the previous map would
                // not be displayed.
                mapHolder.map.clear();
                mapHolder.map.setMapType(GoogleMap.MAP_TYPE_NONE);
            }
        }
    };
    private int countPaused = 0;
    private final String KEY_MESSAGES = "messages";
    private final String KEY_FIRSTCONNECTION = "firstConnection";
    private final String KEY_COUNTPAUSED = "countPaused";

    /**
     *   Проверяем наличие базы
     *   Проверяем ключи
     *   Создаем RecyclerView
     *   Соединяемся с сервером (анимируем)
     *   Если активность умерла - соединение с сервером обрываем
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_MESSAGES)) {
            messages = (ArrayList<Message>) savedInstanceState.getSerializable(KEY_MESSAGES);
        }
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_FIRSTCONNECTION)) {
            firstConnection = (boolean) savedInstanceState.getBoolean(KEY_FIRSTCONNECTION);
        }
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_COUNTPAUSED)) {
            firstConnection = (boolean) savedInstanceState.getBoolean(KEY_COUNTPAUSED);
        }

        gson = new Gson();
        // Getting data from StartActivity
        arguments = getIntent().getExtras();
        username = arguments.get("name").toString();
        server_ip = arguments.get("ip").toString();
        server_port = arguments.get("port").toString();

        // set up the RecyclerView
        recyclerView = findViewById(R.id.MessageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MessageListAdapter(this, messages);
        recyclerView.setAdapter(adapter);
        recyclerView.setRecyclerListener(mRecycleListener);

        // Getting IDs
        goBackButton = (ImageButton) findViewById(R.id.goBackButton);
        attachButton = (ImageButton) findViewById(R.id.attach_button);
        sendButton = (ImageButton) findViewById(R.id.send_button);
        userMessage = (EditText) findViewById(R.id.user_message);
        dotsConnecting = (TextView) findViewById(R.id.dots_connecting);
        isConnected = (ImageView) findViewById(R.id.connected);
        connectionFailed = (ImageView) findViewById(R.id.connection_failed);

        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        startConnectAnimation();

        // Connecting to the server
        clientConnection = new ClientConnection(username);
        clientConnection.start();
    }

    class ClientConnection extends Thread {
        private String username;
        // Client's socket
        private Socket clientSocket;
        // Incoming message
        private Scanner inMessage;
        // Outgoing message
        public PrintWriter outMessage;
        // Get username
        public String getUsername() {
            return this.username;
        }

        ClientConnection(String username) {
            this.username = username;
        }

        @Override
        public void run() {
            try {
                // Connecting to the server
                clientSocket = new Socket(server_ip, Integer.parseInt(server_port));
                if (clientSocket.isConnected()) {
                    connectAnimation.interrupt();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dotsConnecting.setVisibility(View.INVISIBLE);
                            isConnected.setVisibility(View.VISIBLE);
                        }
                    });
                }
                inMessage = new Scanner(clientSocket.getInputStream());
                outMessage =
                        new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())),
                                true);
                if (firstConnection) {
                    Date currentTime = Calendar.getInstance().getTime();
                    String hourMinute = getHourMinute(currentTime);
                    Message tmpMsg = new Message("I have entered the chat!", username, hourMinute);
                    String JSONMessage = gson.toJson(tmpMsg);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            outMessage.println(JSONMessage);
                        }
                    }).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Send message
            sendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Read user's message
                    String message = String.valueOf(userMessage.getText());
                    userMessage.setText("");
                    // Send it to the server
                    if (!message.equals("")) {
                        Date currentTime = Calendar.getInstance().getTime();
                        String hourMinute = getHourMinute(currentTime);
                        Message tmpMsg = new Message(message, username, hourMinute);
                        String JSONMessage = gson.toJson(tmpMsg);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Log.i("INF", "send");

                                    outMessage.println(JSONMessage);
                                } catch (Exception e) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(MainActivity.this, "Server is not available",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                }
            });

            attachButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // getting Geolocation and send it
                    //Toast.makeText(MainActivity.this, "Coming soon...",
                    //        Toast.LENGTH_SHORT).show();
                    showAttachMenu(v);
                }
            });

            try {
                // Endless cycle
                while (!isInterrupted()) {
                    // If there is an incoming message
                    if (inMessage.hasNext()) {
                        Log.i("INF", "read");

                        // Read it
                        String inMes = inMessage.nextLine();
                        Log.d("INF", inMes);
                        Message inMessage = gson.fromJson(inMes, Message.class);
                        if (inMessage.username.equals(username) && inMessage.text.equals("I have entered the chat!")) {
                            continue;
                        }
                        Date currentTime = Calendar.getInstance().getTime();
                        String hourMinute = getHourMinute(currentTime);
                        // Display it
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (inMessage.geo != null) {
                                    messages.add(new Message(inMessage.text, inMessage.username, inMessage.time, inMessage.geo));
                                    Log.d("MESSAGE", "New message with geo");
                                } else {
                                    Log.d("MESSAGE", "New message without geo");
                                    messages.add(new Message(inMessage.text, inMessage.username, inMessage.time));
                                }
                                if (countPaused > 0) {
                                    messages.remove(messages.size()-1);
                                    countPaused--;
                                }
                                // Display message
                                adapter.notifyDataSetChanged();
                                // Scroll down
                                recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
                            }
                        });
                    } else {
                        Log.i("INF", "hasnt");
                    }
                    Thread.sleep(100);
                }
                Log.d("InMes", "i close thread");
                outMessage.flush();
                outMessage.close();
                inMessage.close();
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                Log.d("EXITING", "exiting while interrupt");
                Log.d("InMes", "i am dead");

                Log.d("INF", e.toString());
            }
        }

        public void send(String msg) {
            outMessage.println(msg);
        }
    }

    private void startConnectAnimation() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connectionFailed.setVisibility(View.INVISIBLE);
                dotsConnecting.setVisibility(View.VISIBLE);
            }
        });
        connectAnimation = new Thread(new Runnable() {
            @Override
            public void run() {
                int interation = 0;
                try {
                    while (!Thread.interrupted()) {
                        if (interation == 5) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dotsConnecting.setVisibility(View.INVISIBLE);
                                    connectionFailed.setVisibility(View.VISIBLE);
                                    Toast.makeText(getApplicationContext(),
                                            "Connection failed.\nPlease, try later.",
                                            Toast.LENGTH_SHORT).show();
                                    connectionFailed.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Toast.makeText(getApplicationContext(),
                                                    "Server is not available.\nPlease, try later.",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });
                            break;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dotsConnecting.setText("·");
                            }
                        });
                        Thread.sleep(500);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dotsConnecting.setText("··");
                            }
                        });
                        Thread.sleep(500);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dotsConnecting.setText("···");
                            }
                        });
                        Thread.sleep(500);
                        interation++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        connectAnimation.start();
    }

    private String getHourMinute(Date currentTime) {
        String hour = String.valueOf(currentTime.getHours());
        String minute = String.valueOf(currentTime.getMinutes());
        if (Integer.parseInt(hour) < 10) {
            hour = "0" + hour;
        }
        if (Integer.parseInt(minute) < 10) {
            minute = "0" + minute;
        }
        String hourMinute = hour + ":" + minute;
        return hourMinute;
    }

    private void showAttachMenu(View v) {
        ClientConnection clientConnect = clientConnection;
        attachMenu = new PopupMenu(this, v);
        attachMenu.inflate(R.menu.attach_menu);
        attachMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.geolocation_item: {
                        enableMyLocation();
                        if (locationPermissionGranted) {
                            Date currentTime = Calendar.getInstance().getTime();
                            String hourMinute = getHourMinute(currentTime);
                            LatLng tmplng = getDeviceLocation();
                            if (tmplng == null) {
                                tmplng = new LatLng(55.74356948607958, 37.68156059562104);
                            }
                            String geoLoc = tmplng.latitude + ":" + tmplng.longitude;
                            Message tmpMsg = new Message("I am here:", username, hourMinute, geoLoc);
                            String JSONMessage = gson.toJson(tmpMsg);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    clientConnect.send(JSONMessage);
                                }
                            }).start();
                            return true;
                        }
                    }
                    default:
                        return false;
                }
            }
        });
        attachMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {

            }
        });
        attachMenu.show();
    }

    private void showGeolocation() {

    }

    @Override
    protected void onPause() {
        super.onPause();
        countPaused += 1;
        // соединение с сервером обрываем
        clientConnection.interrupt();
        clientConnection = null;
        connectAnimation.interrupt();
        connectAnimation = null;
        firstConnection = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        sendButton.setOnClickListener(null);
        if (clientConnection == null) {
            startConnectAnimation();
            clientConnection = new ClientConnection(username);
            clientConnection.start();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("messages", messages);
        outState.putBoolean("firstConnection", firstConnection);
        outState.putInt("countPaused", countPaused);
    }

    private void enableMyLocation() {
        // [START maps_check_location_permission]
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        }
        // [END maps_check_location_permission]
    }

    private LatLng getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        LatLng[] anyPlace = new LatLng[1];
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                anyPlace[0] = new LatLng(lastKnownLocation.getLatitude(),
                                        lastKnownLocation.getLongitude());
                            }
                        } else {
                            anyPlace[0] = new LatLng(55.74356948607958, 37.68156059562104);
                        }
                    }
                });
                return anyPlace[0];
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
            return new LatLng(55.74356948607958, 37.68156059562104);
        }
        return new LatLng(55.74356948607958, 37.68156059562104);
    }
}