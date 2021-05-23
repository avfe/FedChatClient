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
                byte validateCode = validate(name, ip, port);
                if (validateCode == 0) {
                    // Transfer Strings to MainActivity
                    servers.add(new Server(name, ip, port));
                    fileHandler.writeObjectToPrivateFile("servers", servers);
                    finish();
                } else if (validateCode == 1) {
                    Toast.makeText(getApplicationContext(), "ERROR!\nCheck name input field!\nMaximum name length is 50 characters.", Toast.LENGTH_LONG).show();
                } else if (validateCode == 2) {
                    Toast.makeText(getApplicationContext(), "ERROR!\nCheck ip input field!", Toast.LENGTH_SHORT).show();
                } else if (validateCode == 3) {
                    Toast.makeText(getApplicationContext(), "ERROR!\nCheck port input field!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "An unexpected error, what did you do?!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    /**
     * 0 - success code
     * 1 - name length error code
     * 2 - ip format error
     * 3 - port format error
     */
    private byte validate(String name, String ip, String port) {
        if (name.length() > 50 || name.length() == 0) {
            return 1;
        }
        if (!isValidIP(ip)) {
            return 2;
        }
        try {
            int valPort = Integer.parseInt(port);
        } catch (Exception e) {
            return 3;
        }
        if (Integer.parseInt(port) < 0 || Integer.parseInt(port) > 65535) {
            return 3;
        }
        return 0;
    }

    private boolean isValidIP(String ip) {
        String PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";
        return ip.matches(PATTERN);
    }
}
