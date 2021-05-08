package tech.fedorov.fedchatclient.Connection;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Scanner;

import tech.fedorov.fedchatclient.Encryption.Cryptographer;
import tech.fedorov.fedchatclient.Messages.Message;

public class ConnectionAssistant extends Thread {
    private String username;
    // Client's socket
    private Socket clientSocket;
    // Incoming message
    private Scanner inputStream;
    // Outgoing message
    private PrintWriter outputStream;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private boolean isConnected;
    // hashmap {PublicKey:EncryptedMessage}

    ConnectionAssistant(String username, String server_ip, String server_port) throws IOException {
        clientSocket = new Socket(server_ip, Integer.parseInt(server_port));
        if (clientSocket != null && clientSocket.isConnected()) {
            isConnected = true;
            this.username = username;
            inputStream = new Scanner(clientSocket.getInputStream());
            outputStream =
                    new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())),
                            true);
            objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
            objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        } else {
            isConnected = false;
        }
    }

    public boolean send(Message message) {
        // Cryptographer, please, encrypt!
        // отправляю на сервер hashmap состоящий из {PublicKey:EncryptedMessage}
        // этот HashMap беру из Cryptographer
        try {
            Thread send = new Thread(new Runnable() {
                @Override
                public void run() {
                    outputStream.println(message);
                }
            });
            send.start();
            send.interrupt();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            if (inputStream.hasNext()) {
                // Read it
                String inMes = inputStream.nextLine();
                // сохрани сообщение в базе
                // расшифруй сообщение
                if (inMes.equalsIgnoreCase("/getPublicKey")) {
                    sendPublicKey();
                }
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
                // Handler, display please!
            }
        }
        // если поток убивают, закрываем все потоки ввода\вывода и сокет
        outputStream.flush();
        outputStream.close();
        inputStream.close();
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // обмен ключами!

    public ArrayList<PublicKey> getPublicKeys() {
        ArrayList<PublicKey> pks = null;
        if (inputStream.hasNext()) {
            pks = (ArrayList<PublicKey>) getObject();
        }
        return pks;
    }

    public void sendPublicKey() {
        sendObject(Cryptographer.getPublic());
    }

    private Object getObject() {
        Object obj = null;
        try {
            obj = objectInputStream.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return obj;
    }

    private void sendObject(Object obj) {
        try {
            objectOutputStream.writeObject(obj);
            objectOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
