package tech.fedorov.fedchatclient;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

import tech.fedorov.fedchatclient.Memory.FileHandler;
import tech.fedorov.fedchatclient.Servers.Server;

public class StartActivity extends AppCompatActivity {
    private ArrayList<Server> servers;
    private FileHandler fileHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Bundle arguments = getIntent().getExtras();
        servers = (ArrayList<Server>) arguments.get("servers");
        fileHandler = new FileHandler(this);
        // Getting data from input fields
        TextInputEditText inputName = (TextInputEditText) findViewById(R.id.start_name);
        TextInputEditText inputIP = (TextInputEditText) findViewById(R.id.start_ip);
        TextInputEditText inputPort = (TextInputEditText) findViewById(R.id.start_port);

        // Create MainActivity intent
        Intent serverListIntent = new Intent(this, ServerListActivity.class);

        // Transfer data to MainActivity
        Button saveButton = (Button) findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = String.valueOf(inputName.getText());
                String ip = String.valueOf(inputIP.getText());
                String port = String.valueOf(inputPort.getText());
                if (validate(name,ip,port)) {
                    // Transfer Strings to MainActivity
                    servers.add(new Server(name, ip, port));
                    fileHandler.writeObjectToPrivateFile("servers", servers);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "ERROR! Check input fields!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean validate(String name, String ip, String port) {
        if (name.length() > 50 || name.length() == 0) {
            return false;
        }
        if (!isValidIP(ip)) {
            return false;
        }
        try {
            int valPort = Integer.parseInt(port);
        } catch (Exception e) {
            return false;
        }
        if (Integer.parseInt(port) < 0 || Integer.parseInt(port) > 65535) {
            return false;
        }
        return true;
    }

    private boolean isValidIP(String ip) {
        String PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";
        return ip.matches(PATTERN);
    }
}
