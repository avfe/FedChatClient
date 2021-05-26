package tech.fedorov.fedchatclient.Messages;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


public class Message implements Serializable {

    @SerializedName("username")
    @Expose
    public String username;
    @SerializedName("text")
    @Expose
    public String text;
    @SerializedName("time")
    @Expose
    public String time;
    @SerializedName("geo")
    @Expose
    public String geo;
    // geo = "Lat:Lng"
    public Message(String textMessage, String username) {
        this.text = textMessage;
        this.username = username;
    }

    public Message(String textMessage, String username, String dateTime) {
        this.text = textMessage;
        this.username = username;
        this.time = dateTime;
    }

    public Message(String textMessage, String username, String dateTime, String geo) {
        this.text = textMessage;
        this.username = username;
        this.time = dateTime;
        this.geo = geo;
    }

    public static byte[] serialize(Object obj) throws IOException {
        try(ByteArrayOutputStream b = new ByteArrayOutputStream()){
            try(ObjectOutputStream o = new ObjectOutputStream(b)){
                o.writeObject(obj);
            }
            return b.toByteArray();
        }
    }

    public static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        try(ByteArrayInputStream b = new ByteArrayInputStream(bytes)){
            try(ObjectInputStream o = new ObjectInputStream(b)){
                return o.readObject();
            }
        }
    }
}

