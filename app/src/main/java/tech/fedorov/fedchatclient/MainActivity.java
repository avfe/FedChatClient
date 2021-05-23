package tech.fedorov.fedchatclient;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class MainActivity extends AppCompatActivity {
    MessageListAdapter adapter;
    ArrayList<Message> messages = new ArrayList<>();
    ClientConnection clientConnection;
    RecyclerView recyclerView;
    String server_ip;
    String server_port;
    ImageButton sendButton;
    ImageButton attachButton;
    ImageButton goBackButton;
    EditText userMessage;
    Handler handler;
    String username;
    Bundle arguments;
    TextView dotsConnecting;
    ImageView isConnected;
    ImageView connectionFailed;
    Thread connectAnimation;
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
        if (savedInstanceState != null && savedInstanceState.containsKey("messages")) {
            messages = (ArrayList<Message>) savedInstanceState.getSerializable("messages");
        }

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
        private PrintWriter outMessage;
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
                String finalMessage = username + ":" + "I have entered the chat!";
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        outMessage.println(finalMessage);
                    }
                }).start();
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
                        String finalMessage = username + ":" + message;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                // publickey:шифрованное сообщение
                                outMessage.println(finalMessage);
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
                        Log.d("InMes", "i am not dead");
                        // Read it
                        String inMes = inMessage.nextLine();
                        String usrnm, txt;
                        if (inMes.indexOf(":") != -1) {
                            String[] msgLines = inMes.split(":");
                            usrnm = msgLines[0];
                            txt = msgLines[1];
                        } else {
                            usrnm = username;
                            txt = inMes;
                        }
                        if (usrnm.equals(username) && txt.equals("I have entered the chat!")) {
                            continue;
                        }
                        Date currentTime = Calendar.getInstance().getTime();
                        String hourMinute = getHourMinute(currentTime);
                        // Display it
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                messages.add(new Message(txt, usrnm, hourMinute));
                                // Display message
                                adapter.notifyDataSetChanged();
                                // Scroll down
                                recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
                            }
                        });
                    }
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
                Log.d("InMes", "i am dead");
                e.printStackTrace();
            }
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
        PopupMenu attachMenu = new PopupMenu(this, v);
        attachMenu.inflate(R.menu.attach_menu);
        attachMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.geolocation_item:
                        Toast.makeText(getApplicationContext(),
                                "Вы выбрали geolocation",
                                Toast.LENGTH_SHORT).show();
                        return true;
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
        // соединение с сервером обрываем
        clientConnection.interrupt();
        clientConnection = null;
        connectAnimation.interrupt();
        connectAnimation = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
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
    }
}