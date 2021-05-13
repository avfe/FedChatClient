package tech.fedorov.fedchatclient;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import tech.fedorov.fedchatclient.Adapters.MessageListAdapter;
import tech.fedorov.fedchatclient.Adapters.ServerListAdapter;
import tech.fedorov.fedchatclient.Servers.Server;

public class ServerListActivity extends AppCompatActivity {
    private ServerListAdapter adapter;
    private RecyclerView recyclerView;
    private ArrayList<Server> servers;
    private TextView newChatButton;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serverslist);
        servers = new ArrayList<>();
        servers.add(new Server("Name", "10.100.200.9", "60606"));
        // проверяем в файлах, есть ли сервера.
        // set up the RecyclerView
        recyclerView = findViewById(R.id.ServerList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ServerListAdapter(this, servers);
        recyclerView.setAdapter(adapter);

        Intent startIntent = new Intent(this, StartActivity.class);
        newChatButton = (TextView) findViewById(R.id.newChatButton);
        newChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(startIntent);
            }
        });
    }
}
