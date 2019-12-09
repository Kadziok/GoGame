package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Executors;

public class GoServer {

    private static HashMap<Integer, Game> games = new HashMap<>();
    private static int maxId = 0;


    public static void main(String[] args) throws Exception {
        System.out.println("The chat server is running...");
        var pool = Executors.newFixedThreadPool(500);
        try (var listener = new ServerSocket(59007)) {
            while (true) {
                pool.execute(new GoServer.Handler(listener.accept()));
            }
        }
    }

    private static class Handler implements Runnable {
        private String begin;
        private Socket socket;
        private Scanner in;
        private PrintWriter out;
        private PrintWriter partner = null;
        private String color;
        private int size;
        private int myGame;

        public Handler(Socket socket) {

            this.socket = socket;
        }

        public void run() {
            try {
                in = new Scanner(socket.getInputStream());
                out = new PrintWriter(socket.getOutputStream(), true);

                out.println("CONNECTED");

                while (true) {
                    out.println("SUBMITSIZE");
                    begin = in.nextLine();
                    System.out.println(begin);
                    if (begin == null) {
                        return;
                    }
                    if (begin.startsWith("SIZE")) {
                        try {
                            size = Integer.parseInt(begin.substring(begin.indexOf("_") + 1));
                            break;
                        } catch (Exception ex) {
                            System.out.println("INCORRECT SIZE");
                        }
                    }
                }

                synchronized (games) {
                    boolean found = false;
                    for (int id : games.keySet()) {
                        if (games.get(id).getSize() == size && games.get(id).isWaiting()) {
                            games.get(id).join(out);
                            myGame = id;
                            color = "W";
                            found = true;
                        }
                    }
                    if (!found) {
                        games.put(maxId, new Game(out, size));
                        myGame = maxId;
                        color = "B";
                        maxId++;
                    }
                }

                while(games.get(myGame).isWaiting()){}

                partner = games.get(myGame).partner(out);

                out.println("PARTNER FOUND");
                out.println("COLOR_" + (color.equals("B") ? 1:2));
                System.out.println("COLOR_" + (color.equals("B") ? 1:2));
                while (true) {
                    String input = in.nextLine();
                    if (input.toLowerCase().startsWith("/quit")) {
                        return;
                    }
                    System.out.println(input);
                    /*if(input.startsWith("SET") && games.get(myGame).isMyMove(color)) {
                        //input = input.toString();
                        String temp = input;
                        input  = input.substring(input.indexOf('_')+1);
                        int i = Integer.parseInt(input.substring(0, input.indexOf('_')));
                        input  = input.substring(input.indexOf('_')+1);
                        int l = Integer.parseInt(input.substring(0));
                        if(games.get(myGame).isFree(i, l)) {
                            if (games.get(myGame).moved(i, l) && games.get(myGame).breaths(i, l) != 0) {
                                partner.println(temp + "_" + color);
                                out.println(temp + "_" + color);
                                String msg = "REMOVE_";
                                if (i > 0 && games.get(myGame).breaths(i - 1, l) == 0) {
                                    msg = msg + games.get(myGame).remove(i - 1, l);
                                }
                                if (i + 1 < size && games.get(myGame).breaths(i + 1, l) == 0) {
                                    msg = msg + games.get(myGame).remove(i + 1, l);
                                }
                                if (l > 0 && games.get(myGame).breaths(i, l - 1) == 0) {
                                    msg = msg + games.get(myGame).remove(i, l - 1);
                                }
                                if (l + 1 < size && games.get(myGame).breaths(i, l + 1) == 0) {
                                    msg = msg + games.get(myGame).remove(i, l + 1);
                                }
                                if (msg.length() > "REMOVE_".length()) {
                                    partner.println(msg);
                                    out.println(msg);
                                }
                                System.out.println("BREATHS: " + games.get(myGame).breaths(i, l));
                            } else {
                                System.out.println("---UNDO---");
                                games.get(myGame).undo(i, l);
                            }
                        }

                    }*/
                    if(input.startsWith("CHAT_")){
                        String temp = input.substring(0, 5);
                        temp = temp + (color.equals("B") ? "czarny: " : "biały: ");
                        temp = temp + input.substring(input.indexOf('_')+1);
                        partner.println(temp);
                        out.println(temp);
                    }
                    else if(input.startsWith("SET") && games.get(myGame).isMyMove(color)) {
                        String temp = input;
                        input = input.substring(input.indexOf('_') + 1);
                        int i = Integer.parseInt(input.substring(0, input.indexOf('_')));
                        input = input.substring(input.indexOf('_') + 1);
                        int l = Integer.parseInt(input.substring(0));
                        if (games.get(myGame).moved(i, l)) {
                            partner.println(temp + "_" + color);
                            out.println(temp + "_" + color);
                            String msg = "REMOVE_";
                            if (i > 0 && games.get(myGame).breaths(i - 1, l) == 0) {
                                msg = msg + games.get(myGame).remove(i - 1, l);
                            }
                            if (i + 1 < size && games.get(myGame).breaths(i + 1, l) == 0) {
                                msg = msg + games.get(myGame).remove(i + 1, l);
                            }
                            if (l > 0 && games.get(myGame).breaths(i, l - 1) == 0) {
                                msg = msg + games.get(myGame).remove(i, l - 1);
                            }
                            if (l + 1 < size && games.get(myGame).breaths(i, l + 1) == 0) {
                                msg = msg + games.get(myGame).remove(i, l + 1);
                            }
                            if (msg.length() > "REMOVE_".length()) {
                                partner.println(msg);
                                out.println(msg);
                            }
                            System.out.println("BREATHS: " + games.get(myGame).breaths(i, l));
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("PLAYER LEFT");
                System.out.println(e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
