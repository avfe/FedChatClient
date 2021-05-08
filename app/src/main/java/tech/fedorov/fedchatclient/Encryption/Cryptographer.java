package tech.fedorov.fedchatclient.Encryption;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import tech.fedorov.fedchatclient.Memory.FileHandler;
import tech.fedorov.fedchatclient.Messages.Message;

public class Cryptographer {
    private KeyPair keyPair;
    private PrivateKey privateKey;
    private static PublicKey publicKey;
    private FileHandler fileHandler;
    private List<PublicKey> interlocutorsKeyList;

    Cryptographer(ArrayList<PublicKey> interlocutorsKeyList) {
        // если есть ключи, то работаем дальше
        // если нет ключей, то создаем и попросим ассистента отправить публичный ключ на сервер
        KeyPair keyPair = getKeyPair();
        // сохраним ключи собеседников чата
        this.interlocutorsKeyList = interlocutorsKeyList;
    }

    public KeyPair getKeyPair() {
        KeyPairGenerator generator = null;
        try {
            generator = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        generator.initialize(4096);
        KeyPair keyPair = generator.generateKeyPair();
        return keyPair;
    }

    public static PublicKey getPublic() {
        return publicKey;
    }

    public PrivateKey getPrivate() {
        return privateKey;
    }

    public void setKeyPair(KeyPair keyPair) {
        this.keyPair = keyPair;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    private byte[] encrypt(Message message, PublicKey pub) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }

        try {
            cipher.init(Cipher.ENCRYPT_MODE, pub);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        byte[] data = null;
        try {
            // Сериализация в байты
            data = cipher.doFinal(Message.serialize(message));
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    private Message decrypt(byte[] data, PrivateKey privateKey) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }

        try {
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        byte[] result = null;
        try {
            result = cipher.doFinal(data);
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        try {
            return (Message) Message.deserialize(result);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new Message("failed decryption", "system");
    }

    private boolean findInArray(String str, String[] strs) {
        for (int i = 0; i < strs.length; i++) {
            if (str.equals(strs[i])) {
                return true;
            }
        }
        return false;
    }
}
