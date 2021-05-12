package tech.fedorov.fedchatclient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.material.textfield.TextInputEditText;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
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
    TextInputEditText userMessage;
    Handler handler;

    /*
        Проверяем наличие базы
        Проверяем ключи
        Создаем RecyclerView
        Соединяемся с сервером (анимируем)
        Если активность умерла - соединение с сервером обрываем
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // проверь есть ли массив сообщений
        // или обратись к базе данных (DBManager)
        messages.add(new Message("Connecting to server...", "INFO"));
        // Getting data from StartActivity
        Bundle arguments = getIntent().getExtras();
        String username = arguments.get("name").toString();
        server_ip = arguments.get("ip").toString();
        server_port = arguments.get("port").toString();

        // set up the RecyclerView
        recyclerView = findViewById(R.id.MessageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MessageListAdapter(this, messages);
        recyclerView.setAdapter(adapter);

        // Getting IDs
        sendButton = (ImageButton) findViewById(R.id.send_button);
        userMessage = (TextInputEditText) findViewById(R.id.user_message);

        /*
        Здесь будет реализация handler
        handler = new Handler() {
            @Override
            public void handleMessage(@NonNull android.os.Message msg) {
                // messages.add(message);

                // Display message
                adapter.notifyItemInserted(messages.size());
                // Scroll down
                recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
            }
        };
         */

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
                            // publickey:шифрованное сообщение
                            outMessage.println(finalMessage);
                        }
                    }).start();
                }
            });
            try {
                // Endless cycle
                while (!isInterrupted()) {
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
                                adapter.notifyDataSetChanged();
                                // Scroll down
                                recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
                            }
                        });
                    }
                }
                outMessage.flush();
                outMessage.close();
                inMessage.close();
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // соединение с сервером обрываем
        clientConnection.interrupt();
    }
}