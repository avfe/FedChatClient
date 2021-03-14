package tech.fedorov.fedchatclient;

public class Message {
    String dateTime;
    String textMessage;
    String senderUsername;

    Message (String textMessage, String username) {
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
}
