package tech.fedorov.fedchatclient;

public class Message {
    String dateTime;
    String textMessage;
    String senderUsername;

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
}