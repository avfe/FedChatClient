package tech.fedorov.fedchatclient.Messages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Message implements Serializable {
    String dateTime;
    String textMessage;
    String senderUsername;

    public Message(String textMessage, String username) {
        this.textMessage = textMessage;
        this.senderUsername = username;
    }
    public String getDateTime() {
        return dateTime;
    }

    public String getTextMessage() {
        return textMessage;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public void setTextMessage(String textMessage) {
        this.textMessage = textMessage;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
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
