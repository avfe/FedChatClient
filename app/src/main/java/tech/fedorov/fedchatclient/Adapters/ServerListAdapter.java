package tech.fedorov.fedchatclient.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import tech.fedorov.fedchatclient.R;
import tech.fedorov.fedchatclient.ServerListActivity;
import tech.fedorov.fedchatclient.Servers.Server;

public class ServerListAdapter extends RecyclerView.Adapter<ServerListAdapter.ViewHolder>{
    private ArrayList<Server> mData;

    private LayoutInflater mInflater;

    // data is passed into the constructor
    public ServerListAdapter(Context context, ArrayList<Server> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ServerListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.server_item_cardview, parent, false);
        return new ServerListAdapter.ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ServerListAdapter.ViewHolder holder, int position) {
        Server serv = mData.get(position);
        String adress = serv.getAddress();
        String name = serv.getName();
        holder.serverAddress.setText(adress);
        holder.serverName.setText(name);
        ServerListAdapter context = ServerListAdapter.this;
        holder.removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mData.remove(position);
                context.notifyDataSetChanged();
            }
        });
        holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // переход в чат с указанными настройками
            }
        });
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView serverAddress;
        TextView serverName;
        ImageButton removeButton;
        ConstraintLayout constraintLayout;
        ViewHolder(View itemView) {
            super(itemView);
            serverAddress = itemView.findViewById(R.id.ServerAdress);
            serverName = itemView.findViewById(R.id.ServerName);
            removeButton = itemView.findViewById(R.id.remove_button);
            constraintLayout = itemView.findViewById(R.id.server_item_field);
        }
    }
}
