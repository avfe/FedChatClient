package tech.fedorov.fedchatclient;

import androidx.appcompat.app.AppCompatActivity;

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
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Getting data from StartActivity
        Bundle arguments = getIntent().getExtras();
        String username = arguments.get("name").toString();
        String server_ip = arguments.get("ip").toString();
        String server_port = arguments.get("port").toString();

        // Getting IDs
        Button sendButton = (Button) findViewById(R.id.send_button);
        TextView chatField = (TextView) findViewById(R.id.chat_field);
        TextInputEditText userMessage = (TextInputEditText) findViewById(R.id.user_message);
        ScrollView scrollbar = (ScrollView) findViewById(R.id.scrollbar);

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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            chatField.append("\nCONNECTED\n");
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
                        message = username + ": " + message;
                        userMessage.setText("");
                        // Send it to the server
                        String finalMessage = message;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                outMessage.println(finalMessage);
                            }
                        }).start();
                        // Scrolling to the bottom
                        scrollbar.smoothScrollTo(scrollbar.getScrollX(), chatField.getBottom());
                    }
                });

                try {
                    // Endless cycle
                    while (true) {
                        // If there is an incoming message
                        if (inMessage.hasNext()) {
                            // Read it
                            String inMes = inMessage.nextLine();
                            // Display it
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    chatField.append(inMes + "\n");
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