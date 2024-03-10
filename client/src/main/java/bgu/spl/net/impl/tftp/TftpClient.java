package bgu.spl.net.impl.tftp;

import java.net.Socket;

import java.io.*;

public class TftpClient {

    public static byte[] convertToPacket(byte[] message, String opcode) {
        if (opcode.equals("LOGRQ")) {
            byte[] ans = new byte[message.length + 3];
            ans[0] = 0;
            ans[1] = 7;
            ans[ans.length - 1] = 0;
            int index = 2;
            for (byte b : message) {
                ans[index] = b;
                index++;
            }
            return ans;
        } else if (opcode.equals("DELRQ")) {
            byte[] ans = new byte[message.length + 3];
            ans[0] = 0;
            ans[1] = 8;
            ans[ans.length - 1] = 0;
            int index = 2;
            for (byte b : message) {
                ans[index] = b;
                index++;
            }
            return ans;
        } else if (opcode.equals("RRQ")) {
            byte[] ans = new byte[message.length + 3];
            ans[0] = 0;
            ans[1] = 1;
            ans[ans.length - 1] = 0;
            int index = 2;
            for (byte b : message) {
                ans[index] = b;
                index++;
            }
            return ans;
        } else if (opcode.equals("WRQ")) {
            byte[] ans = new byte[message.length + 3];
            ans[0] = 0;
            ans[1] = 2;
            ans[ans.length - 1] = 0;
            int index = 2;
            for (byte b : message) {
                ans[index] = b;
                index++;
            }
            return ans;
        } else if (opcode.equals("DIRQ")) {
            byte[] ans = new byte[2];
            ans[0] = 0;
            ans[1] = 6;
            return ans;
        } else if (opcode.equals("DISC")) {
            byte[] ans = new byte[2];
            ans[0] = 0;
            ans[1] = 10;
            return ans;
        } else {
            return null;
        }

    }

    public static void main(String[] args) {
        String host = "127.0.0.1"; // Local host
        int port = 7777; // Port number

        try (Socket socket = new Socket(host, port);
                BufferedReader keyboardReader = new BufferedReader(new InputStreamReader(System.in));
                OutputStream out = socket.getOutputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("Connected to server.");

            // Start a thread to listen for keyboard input
            Thread keyboardThread = new Thread(() -> {
                try {
                    while (true) {
                        System.out.print("Enter message (or 'exit' to quit): ");
                        String userInput = keyboardReader.readLine();
                        if (userInput.equalsIgnoreCase("exit")) {
                            break; // Exit the loop if the user types 'exit'
                        }
                        String[] parts = userInput.split(" ", 2);
                        String firstPart = parts[0];
                        String secondPart = "";
                        if (parts.length > 1) {
                            secondPart = parts[1];
                        }
                        byte[] messageBytes = secondPart.getBytes();
                        byte[] packet = convertToPacket(messageBytes, firstPart);
                        if (packet == null) {
                            System.out.println("unknown command, please try again");
                        } else {
                            out.write(packet);
                            out.write('\n'); // Write a newline character to indicate the end of the message
                            out.flush();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            keyboardThread.start();

            // Read messages from the server
            String response;
            while ((response = in.readLine()) != null) {
                System.out.println("Server response: " + response);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
