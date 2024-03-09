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
            byte[] x = { 0, 7, 72, 69, 76, 76, 79, 0, 0, 2, 32, 79, 70, 69, 0, 0, 3, 75, 32, 65, 78, 68, 32, 84, 79, 77,
                    69, 82, 0 };

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
