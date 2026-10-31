package client;

import chess.*;

public class ClientMain {
    public static void main(String[] args) {
        var port = 8081;
        if (args.length == 1) {
            port = Integer.parseInt(args[0]);
        }

        var client = new ChessClient(port);
        client.run();
    }
}
