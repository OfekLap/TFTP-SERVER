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
            byte[] x = { 0, 7, 111, 102, 101, 107, 0, 0, 2, 116, 111, 109, 101, 114, 0, 0, 3, 0, 15, 0, 1, 109, 121, 32,
                    110, 97, 109, 101, 32, 105, 115, 32, 116, 111, 109, 101, 114 };
            out.write(x);
            out.flush();
            byte[] y = { 0, 2, 111, 102, 101, 107, 0, 0, 3, 0,
                    14, 0, 1, 109, 121, 32, 110, 97, 109, 101, 32, 105, 115, 32, 111, 102, 101, 107, 0, 6 };
            out.write(y);
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
