package bgu.spl.net.impl.tftp;

import java.io.File;
import java.io.FileNotFoundException;
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
import java.util.concurrent.ConcurrentLinkedDeque;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.io.FileWriter;

class serverInfo {
    static ConcurrentHashMap<Integer, String> id_logins = new ConcurrentHashMap<Integer, String>();
    static ConcurrentLinkedDeque<String> file_names = new ConcurrentLinkedDeque<String>();
}

public class TftpProtocol implements BidiMessagingProtocol<byte[]> {

    private int connectionId;
    private boolean shouldTerminate = false;
    private Connections<byte[]> connections;
    private static final String PATH = "c:/Users/ofekl/Downloads/Skeleton (1)/Skeleton/server/Flies";
    private String[] errorStrings;
    private String toSend;
    private String toSave;
    private byte[][] dataToSend;
    private byte[] dataToSave;

    @Override
    public void start(int connectionId, Connections<byte[]> connections) {
        // TODO implement this
        this.connectionId = connectionId;
        this.connections = connections;
        errorStrings = new String[8];
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
        ///////////////////// the server *SENDS* files to the client /READ
        if (opcode == 1) {
            if (!serverInfo.id_logins.contains(connectionId)) {
                sendError(6);
                return;
            }
            String msg = byteToString(deleteOpcode(message));
            if (!serverInfo.file_names.contains(msg)) {
                sendError(1);
                return;
            } else {
                byte[] fileBytes;
                try {
                    fileBytes = read(msg); // where we get the bytes of the file that was on the server
                    byte[][] toSend = prepareDataToSend(fileBytes);
                    dataToSend = toSend;
                    connections.send(connectionId, dataToSend[0]);
                    System.out.println("server send the data");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        ///////////////////// the server *GETS* file from the client /WRITE
        else if (opcode == 2) {
            String msg = byteToString(deleteOpcode(message));
            if (!serverInfo.id_logins.contains(connectionId)) {
                sendError(6);
                return;
            }
            if (serverInfo.file_names.contains(msg)) {
                sendError(5);
                return;
            }
            serverInfo.file_names.add(msg);
            toSave = msg;
            ACK(0);

            return;

        }
        ///////////////////// DATA
        else if (opcode == 3) {
            byte[] toSave = prepareDataToSave(message);
            dataToSave = mergeArrays(dataToSave, toSave);
            if (message.length < 518) {
                write(dataToSave, dataToSave.length, this.toSave);
            }
        }
        ///////////////////// ACK
        else if (opcode == 4) {
            short blockNum = byteToShort(Arrays.copyOfRange(message, 2, message.length));
            if (dataToSend.length > blockNum) {
                connections.send(connectionId, dataToSend[blockNum]);
            }
        }
        ///////////////////// DIRQ
        else if (opcode == 6) {
            byte[] files = convert(serverInfo.file_names);
            byte[][] data = prepareDataToSend(files);
            dataToSend = data;
            connections.send(connectionId, dataToSend[0]);

        }
        ///////////////////// LOGIN
        else if (opcode == 7) {
            String name = byteToString(deleteOpcode(message));
            if (logIn(connectionId, name)) {
                ACK(0);
                System.out.println("loged in");
            } else {
                sendError(7);
            }
        }
        ///////////////////// DELETE
        else if (opcode == 8) {
            if (delete(byteToString(deleteOpcode(message)))) {
                ACK(0);
                BCAST(deleteOpcode(message), 0);
            } else {
                sendError(1);
            }

        }
        ///////////////////// DISCONNECT
        else if (opcode == 10) {
            String login = serverInfo.id_logins.get(connectionId);
            if (login == null || login.equals("")) {
                sendError(6);
            } else {
                serverInfo.id_logins.put(connectionId, "");
                connections.disconnect(connectionId);
                ACK(0);
            }
        }
    }

    @Override
    public boolean shouldTerminate() {
        // TODO implement this
        return shouldTerminate;
    }

    public void BCAST(byte[] message, int deletedOrAdd) {
        byte[] bcast = new byte[message.length + 4];
        bcast[0] = 0;
        bcast[1] = 9;
        bcast[2] = (byte) deletedOrAdd;
        bcast[bcast.length - 1] = 0;
        for (Integer connectionId : serverInfo.id_logins.keySet()) {
            connections.send(connectionId, bcast);
        }
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
        if (serverInfo.id_logins.containsKey(Id)) {
            if (serverInfo.id_logins.get(Id).equals("")) {
                serverInfo.id_logins.put(Id, name);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean write(byte[] data, int length, String name) {
        try {

            File file = new File(PATH, name);

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

    public boolean delete(String fileName) {
        File file = new File(PATH, fileName);
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

    public static byte[] read(String filename) throws IOException {
        Path filePath = Paths.get(PATH, filename);
        File file = filePath.toFile();

        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + filename);
        }

        return Files.readAllBytes(filePath);
    }

    public Byte[] shortToByte(short a) {
        Byte[] a_bytes = new Byte[] { (byte) (a >> 8), (byte) (a & 0xff) };
        return a_bytes;
    }

    public short byteToShort(byte[] b) {
        short b_short = (short) (((short) b[0] & 0xFF) << 8 | (short) (b[1] & 0xFF));
        return b_short;
    }

    public byte[][] prepareDataToSend(byte[] data) {
        short numOfPackets = (short) (data.length / 512);
        short sizeOfLastPacket = (short) (data.length % 512);
        byte[][] ans = new byte[numOfPackets + 1][];
        short index = 6;
        for (short i = 0; i < numOfPackets; i++) {
            byte[] packet = new byte[512];
            packet[0] = 0;
            packet[1] = 3;
            Byte[] size = shortToByte((short) (512));
            packet[2] = size[0];
            packet[3] = size[1];
            Byte[] block = shortToByte(i);
            packet[4] = block[0];
            packet[5] = block[1];
            for (int j = 6; j < packet.length; j++) {
                packet[j] = data[index];
                index++;
            }
            ans[i] = packet;

        }
        byte[] packet = new byte[sizeOfLastPacket + 6];
        packet[0] = 0;
        packet[1] = 3;
        Byte[] size = shortToByte((short) (sizeOfLastPacket + 6));
        packet[2] = size[0];
        packet[3] = size[1];
        Byte[] block = shortToByte((short) (numOfPackets + 1));
        packet[4] = block[0];
        packet[5] = block[1];
        for (int i = 6; i < packet.length; i++) {
            packet[i] = data[index];
        }
        ans[numOfPackets + 1] = packet;
        return ans;
    }

    public byte[] prepareDataToSave(byte[] data) {
        byte[] newData = new byte[data.length - 6];
        for (int i = 0; i < newData.length; i++) {
            newData[i] = data[i + 6];
        }
        return newData;
    }

    public static byte[] convert(ConcurrentLinkedDeque<String> fileNames) {
        // Calculate the total length of the resulting byte array
        int totalLength = 0;
        for (String fileName : fileNames) {
            totalLength += fileName.length() + 1; // Add 1 for the null byte
        }

        // Create the byte array
        byte[] result = new byte[totalLength];
        int index = 0;

        // Fill the byte array with file names separated by null bytes
        for (String fileName : fileNames) {
            byte[] fileNameBytes = fileName.getBytes(StandardCharsets.UTF_8);
            System.arraycopy(fileNameBytes, 0, result, index, fileNameBytes.length);
            index += fileNameBytes.length;
            result[index++] = 0; // Add null byte separator
        }

        return result;
    }

    public static byte[] mergeArrays(byte[] array1, byte[] array2) {
        byte[] mergedArray = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, mergedArray, 0, array1.length);
        System.arraycopy(array2, 0, mergedArray, array1.length, array2.length);
        return mergedArray;
    }
}