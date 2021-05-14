package tech.fedorov.fedchatclient.Memory;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class FileHandler {
    private Context context;

    public FileHandler(Context context) {
        this.context = context;
    }

    public void writeObjectToPrivateFile(String filename, Object obj) {
        FileOutputStream fileOut = null;
        try {
            fileOut = context.openFileOutput(filename, Context.MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        ObjectOutputStream objectOut = null;
        try {
            objectOut = new ObjectOutputStream(fileOut);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            objectOut.writeObject(obj);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            objectOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public  Object readObjectFromPrivateFile(String filename) {
        FileInputStream flis = null;
        try {
            flis = context.openFileInput(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ObjectInputStream objis = null;
        try {
            objis = new ObjectInputStream(flis);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Object object = null;
        try {
            object = objis.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return object;
    }

    public static boolean findInArray(String str, String[] strs) {
        for (int i = 0; i < strs.length; i++) {
            if (str.equals(strs[i])) {
                return true;
            }
        }
        return false;
    }
}
