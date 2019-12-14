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
        private boolean botMode;
        private Bot bot;

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
                            size = Integer.parseInt(begin.substring(begin.indexOf("_") + 1,
                                    begin.indexOf("M")));
                            break;
                        } catch (Exception ex) {
                            System.out.println("INCORRECT SIZE");
                        }
                    }
                }

                synchronized (games) {
                    if(begin.charAt(begin.indexOf("M")+1) == '1'){
                        botMode = true;
                        games.put(maxId, new Game(out, size));
                        myGame = maxId;
                        bot = new Bot(size);
                        color = "B";
                        maxId++;
                    }
                    else {
                        botMode = false;
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
                }
                if(!botMode) {
                    out.println("CHAT_Oczekiwanie na przeciwnika");

                    while (games.get(myGame).isWaiting()) {
                    }

                    out.println("CHAT_Przeciwnik dołaczył do gry");

                    partner = games.get(myGame).partner(out);

                    out.println("PARTNER FOUND");

                    out.println("COLOR_" + (color.equals("B") ? 1 : 2));

                    while (true) {
                        String input = in.nextLine();
                        if (!games.get(myGame).ended()) {
                            if (input.startsWith("CHAT_")) {
                                String temp = input.substring(0, 5);
                                temp = temp + (color.equals("B") ? "czarny: " : "biały: ");
                                temp = temp + input.substring(input.indexOf('_') + 1);
                                partner.println(temp);
                                out.println(temp);
                            } else if (input.startsWith("SET") && games.get(myGame).isMyMove(color)) {
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
                                        String w = "CHAT_WYNIK: czarny: " + games.get(myGame).points[0]
                                                + " | biały: " + games.get(myGame).points[1];
                                        partner.println(w);
                                        out.println(w);
                                        partner.println(msg);
                                        out.println(msg);
                                    }
                                }
                            } else if (input.startsWith("PASS") && games.get(myGame).isMyMove(color)) {
                                games.get(myGame).pass();
                                out.println("CHAT_Gracz zrezygnował z ruchu");
                                partner.println("CHAT_Gracz zrezygnował z ruchu");
                                if (games.get(myGame).ended()) {
                                    String s = "CHAT_Obaj gracze zrezygnowali z ruchu."
                                            + " Proszę wybrać, czy łańcuchy są żywe (zielone)"
                                            + ", czy martwe (czerwone)."
                                            + " Wybór należy zatwierdzić przyciskiem.";
                                    out.println(s);
                                    partner.println(s);
                                    String chains = games.get(myGame).chains();
                                    out.println(chains);
                                    partner.println(chains);
                                    String territory = games.get(myGame).territory();
                                    out.println(territory);
                                    partner.println(territory);
                                }

                            }
                        } else {
                            if (input.startsWith("RESTART")) {
                                games.get(myGame).restart();
                                out.println("RESTART");
                                partner.println("RESTART");
                            }
                            if (input.startsWith("STATES")) {
                                games.get(myGame).setStates(out, input);
                                if (games.get(myGame).statesSet()) {
                                    if (games.get(myGame).statesEqual()) {
                                        out.println("CHAT_Udało się ustalić zgodne odpowiedzi");
                                        partner.println("CHAT_Udało się ustalić zgodne odpowiedzi");

                                        int[] w = games.get(myGame).finalScore();
                                        if (w != null) {
                                            String score;
                                            if (w[0] < w[1])
                                                score = "Wygrywa biały.";
                                            else if (w[1] < w[0])
                                                score = "Wygrywa czarny.";
                                            else
                                                score = "Remis";
                                            out.println("CHAT_KONIEC GRY: " + score);
                                            partner.println("CHAT_KONIEC GRY: " + score);
                                        } else
                                            System.out.println("---NULL SCORE ARRAY---");

                                    } else {
                                        out.println("CHAT_Odpowiedzi graczy różnią się. Spróbujcie ponownie");
                                        partner.println("CHAT_Odpowiedzi graczy różnią się. Spróbujcie ponownie");
                                    }
                                }
                            }
                        }
                    }
                }else{
                    out.println("COLOR_" + (color.equals("B") ? 1 : 2));
                    while (true) {
                        String input = in.nextLine();
                        if (!games.get(myGame).ended()) {
                            if (input.startsWith("CHAT_")) {
                                String temp = input.substring(0, 5);
                                temp = temp + (color.equals("B") ? "czarny: " : "biały: ");
                                temp = temp + input.substring(input.indexOf('_') + 1);
                                out.println(temp);
                            } else if (input.startsWith("SET") && games.get(myGame).isMyMove(color)) {
                                String temp = input;
                                input = input.substring(input.indexOf('_') + 1);
                                int i = Integer.parseInt(input.substring(0, input.indexOf('_')));
                                input = input.substring(input.indexOf('_') + 1);
                                int l = Integer.parseInt(input.substring(0));
                                if (games.get(myGame).moved(i, l)) {
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
                                        String w = "CHAT_WYNIK: czarny: " + games.get(myGame).points[0]
                                                + " | biały: " + games.get(myGame).points[1];
                                        out.println(w);
                                        out.println(msg);
                                    }
                                    for(String m : bot.move(games.get(myGame)))
                                        out.println(m);

                                }
                            } else if (input.startsWith("PASS") && games.get(myGame).isMyMove(color)) {
                                games.get(myGame).pass();
                                games.get(myGame).pass();
                                if (games.get(myGame).ended()) {
                                    String s = "CHAT_Obaj gracze zrezygnowali z ruchu."
                                            + " Proszę wybrać, czy łańcuchy są żywe (zielone)"
                                            + ", czy martwe (czerwone)."
                                            + " Wybór należy zatwierdzić przyciskiem.";
                                    out.println(s);
                                    String chains = games.get(myGame).chains();
                                    out.println(chains);
                                    String territory = games.get(myGame).territory();
                                    out.println(territory);
                                }

                            }
                        } else {
                            if (input.startsWith("RESTART")) {
                                games.get(myGame).restart();
                                out.println("RESTART");
                            }
                            if (input.startsWith("STATES")) {
                                games.get(myGame).setStates(out, input);
                                games.get(myGame).duplicateState();
                                if (games.get(myGame).statesSet()) {
                                    if (games.get(myGame).statesEqual()) {

                                        int[] w = games.get(myGame).finalScore();
                                        if (w != null) {
                                            String score;
                                            if (w[0] < w[1])
                                                score = "Wygrywa biały.";
                                            else if (w[1] < w[0])
                                                score = "Wygrywa czarny.";
                                            else
                                                score = "Remis";
                                            out.println("CHAT_KONIEC GRY: " + score);
                                        } else
                                            System.out.println("---NULL SCORE ARRAY---");

                                    } else {
                                        out.println("CHAT_Odpowiedzi graczy różnią się. Spróbujcie ponownie");
                                    }
                                }
                            }
                        }
                    }
                }

            } catch (Exception e) {
                out.println("CHAT_Przeciwnik opuścił grę. Utwórz nową grę, by zagrać z przeciwnikiem");
                games.remove(myGame);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {

                }
            }
        }
    }
}
