package tech.fedorov.fedchatclient;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ViewHolder>{
    private ArrayList<Message> mData;

    private LayoutInflater mInflater;

    // data is passed into the constructor
    MessageListAdapter(Context context, ArrayList<Message> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.message_item, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Message msg = mData.get(position);
        String text = msg.getTextMessage();
        String name = msg.getSenderUsername();
        holder.messageText.setText(text);
        holder.username.setText(name);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView username;

        ViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message);
            username = itemView.findViewById(R.id.username);
        }
    }
}

