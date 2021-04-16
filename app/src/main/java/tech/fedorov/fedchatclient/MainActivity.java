package tech.fedorov.fedchatclient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {
    MessageListAdapter adapter;
    ArrayList<Message> messages = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        messages.add(new Message("Connecting to server...", "INFO"));
        // Getting data from StartActivity
        Bundle arguments = getIntent().getExtras();
        String username = arguments.get("name").toString();
        String server_ip = arguments.get("ip").toString();
        String server_port = arguments.get("port").toString();

        // set up the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.MessageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MessageListAdapter(this, messages);
        recyclerView.setAdapter(adapter);

        // Getting IDs
        Button sendButton = (Button) findViewById(R.id.send_button);
        TextInputEditText userMessage = (TextInputEditText) findViewById(R.id.user_message);

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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            messages.add(new Message("Сonnection established!", "SERVER:"));
                            // Display message
                            adapter.notifyItemInserted(messages.size());
                            // Scroll down
                            recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
                        }
                    });
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
                        String finalMessage = username + ":" + message;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                outMessage.println(finalMessage);
                            }
                        }).start();
                    }
                });

                try {
                    // Endless cycle
                    while (true) {
                        // If there is an incoming message
                        if (inMessage.hasNext()) {
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
                            // Display it
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    messages.add(new Message(txt, usrnm + ':'));
                                    // Display message
                                    adapter.notifyItemInserted(messages.size());
                                    // Scroll down
                                    recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // Connecting to the server
        ClientConnection clientConnection = new ClientConnection(username);
        clientConnection.start();
    }
}