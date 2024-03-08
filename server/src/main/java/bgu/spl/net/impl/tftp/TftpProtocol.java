package bgu.spl.net.impl.tftp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.srv.BlockingConnectionHandler;
import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;
import bgu.spl.net.srv.ConnectionsImpl;
import bgu.spl.net.impl.tftp.TftpEncoderDecoder;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.io.FileWriter;

class serverInfo {
    static ConcurrentHashMap<Integer, String> id_logins = new ConcurrentHashMap<Integer, String>();
}

public class TftpProtocol implements BidiMessagingProtocol<byte[]> {

    private int connectionId;
    private boolean shouldTerminate = false;
    private Connections<byte[]> connections;
    private static final String path = "c:/Users/ofekl/Downloads/Skeleton (1)/Skeleton/server/Flies";
    private String[] errorStrings;

    @Override
    public void start(int connectionId, Connections<byte[]> connections) {
        // TODO implement this
        this.connectionId = connectionId;
        this.connections = connections;
        errorStrings[1] = "File not found - RRQ DELRQ of non-existing file.";
        errorStrings[2] = "Access violation - File cannot be written, read or deleted.";
        errorStrings[3] = "Disk full or allocation exceeded - No room in disk.";
        errorStrings[4] = "Illegal TFTP operation - Unknown Opcode.";
        errorStrings[5] = "File already exists - File name exists on WRQ.";
        errorStrings[6] = "User not logged in - Any opcode received before Login completes.";
        errorStrings[7] = "User already logged in - Login username already connected.";

    }

    @Override
    public void process(byte[] message) {
        // TODO implement this

        // StringBuilder sb = new StringBuilder();
        // sb.append("process got: {");
        // for (int i = 0; i < message.length; i++) {
        // sb.append(message[i]);
        // if (i != message.length - 1) {
        // sb.append(",");
        // }
        // }
        // sb.append("}");
        // System.out.println(sb.toString());
        int opcode = message[1];
        if (opcode == 1) { // read
            if (read(deleteOpcode(message), message.length - 2)) {
                ACK(0);
            } else {
                sendError(1);
            }
        } else if (opcode == 2) {// write
            if (write(deleteOpcode(message), message.length - 2)) {
                ACK(0);
            } else {
                sendError(5);
            }
        } else if (opcode == 3) {
            // data, to be completed
        } else if (opcode == 4) {
            ACK(deleteOpcode(message));
        }

        else if (opcode == 6)

        {

        } else if (opcode == 7) {
            String name = byteToString(deleteOpcode(message));
            if (logIn(connectionId, name)) {
                ACK(0);
            } else {
                sendError(7);
            }
        } else if (opcode == 8) {
            if (delete(byteToString(deleteOpcode(message)))) {
                ACK(0);
            } else {
                sendError(1);
            }

        } else if (opcode == 9) {

        } else if (opcode == 10) {
            disconnect(connectionId);
        }
    }

    @Override
    public boolean shouldTerminate() {
        // TODO implement this
        return shouldTerminate;
    }

    public byte[] deleteOpcode(byte[] message) {
        return Arrays.copyOfRange(message, 2, message.length);
    }

    public String byteToString(byte[] message) {
        return new String(message, StandardCharsets.UTF_8).substring(0, message.length - 1);
    }

    public byte[] stringToBytes(String str) {
        byte[] bytes = new byte[str.length() + 1];
        for (int i = 0; i < str.length(); i++) {
            bytes[i] = (byte) str.charAt(i);
        }
        bytes[str.length()] = 0;
        return bytes;
    }

    public boolean logIn(int Id, String name) {
        if (serverInfo.id_logins.get(Id).equals("")) {
            serverInfo.id_logins.put(Id, name);
            return true;
        } else {
            return false;
        }
    }

    private boolean write(byte[] data, int length) {
        try {

            File file = new File(path, byteToString(data));

            // Write the received bytes to the file
            try (FileOutputStream out = new FileOutputStream(file, true)) {
                out.write(data, 0, length);
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean read(byte[] data, int length) {
        // Convert the file name bytes to a string
        String fileName = byteToString(deleteOpcode(data));

        File file = new File(path, fileName);

        if (file.exists()) {
            // Read the file content
            try {
                byte[] fileContent = Files.readAllBytes(file.toPath());
                connections.send(connectionId, fileContent);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            // File does not exist, send error
            sendError(1); // File not found
            return false;
        }
    }

    public boolean delete(String fileName) {
        File file = new File(path, fileName);
        if (file.exists()) {
            if (file.delete()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static void deleteFile(String fileName) {
        // Construct the file path
        File file = new File(path, fileName);

        // Check if the file exists
        if (file.exists()) {
            // Attempt to delete the file
            if (file.delete()) {
                System.out.println("File " + fileName + " deleted successfully.");
            } else {
                System.err.println("Failed to delete file " + fileName);
            }
        } else {
            System.err.println("File " + fileName + " does not exist.");
        }
    }

    public boolean disconnect(int id) {
        if (serverInfo.id_logins.get(id).equals("")) {
            return false;
        } else {
            serverInfo.id_logins.remove(id);
            connections.disconnect(id);
            return true;
        }
    }

    public void sendError(int errorNum) {
        String error = errorStrings[errorNum];
        byte[] err = stringToBytes(error);
        connections.send(connectionId, err);
    }

    public void ACK(int blockNum) {
        byte[] a = new byte[1];
        a[0] = (byte) blockNum;
        connections.send(connectionId, a);
    }

}
