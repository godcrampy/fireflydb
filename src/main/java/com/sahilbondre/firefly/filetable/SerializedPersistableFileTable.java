package com.sahilbondre.firefly.filetable;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SerializedPersistableFileTable implements PersistableFileTable, Serializable {

    private static final Kryo kryo = new Kryo();

    private final Map<String, FilePointer> table;

    public SerializedPersistableFileTable() {
        kryo.register(SerializedPersistableFileTable.class);
        kryo.register(HashMap.class);
        kryo.register(FilePointer.class);
        this.table = new HashMap<>();
    }

    public static SerializedPersistableFileTable fromEmpty() {
        return new SerializedPersistableFileTable();
    }

    public static SerializedPersistableFileTable fromFile(String filePath) throws FileNotFoundException, KryoException {
        kryo.register(SerializedPersistableFileTable.class);
        kryo.register(HashMap.class);
        kryo.register(FilePointer.class);
        try (Input input = new Input(new FileInputStream(filePath))) {
            return kryo.readObject(input, SerializedPersistableFileTable.class);
        } catch (KryoException e) {
            throw new InvalidFileTableException("Failed to load FileTable from disk: " + e.getMessage());
        }
    }

    @Override
    public void put(byte[] key, FilePointer value) {
        if (key != null && value != null) {
            table.put(new String(key), value);
        }
    }

    @Override
    public FilePointer get(byte[] key) {
        if (key != null) {
            return table.get(new String(key));
        }
        return null;
    }

    @Override
    public void saveToDisk(String filePath) throws FileNotFoundException {
        Output output = new Output(new FileOutputStream(filePath));
        kryo.writeObject(output, this);
        output.close();
    }
}
