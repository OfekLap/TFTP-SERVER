package bgu.spl.net.impl.tftp;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class TftpClient {
    // TODO: implement the main logic of the client, when using a thread per client
    // the main logic goes here
    public static void main(String[] args) {
        if (args.length == 0) {
            args = new String[] { "localhost", "hello" };
        }

        if (args.length < 2) {
            System.out.println("you must supply two arguments: host, message");
            System.exit(1);
        }

        try (Socket sock = new Socket(args[0], 7777);
                BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                BufferedOutputStream out = new BufferedOutputStream(sock.getOutputStream())) {
            byte[] x = { 0, 6, 0, 7, 8, 1, 2, 4, 5, 0, 0, 8, 7, 7, 9, 9, 0, 0, 9, 1, 4, 4, 4, 0, 0, 10 };
            out.write(x);
            out.flush();
            System.out.println("Bytes sent to server");
            // out.newLine();
            // System.out.println("awaiting response");
            // String line = in.readLine();
            // System.out.println("message from server: " + line);
        } catch (IOException e) {
            // Handle the IOException here, e.g., print an error message
            e.printStackTrace();
        }
    }

}
