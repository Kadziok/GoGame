package server;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class Bot {
    private final int size;
    private int[][] board;
    public Bot(int size){
        this.size = size;
    }

    public ArrayList<String> move(Game game){
        board = game.getBoard();
        ArrayList<String> msgs = new ArrayList<>();
        /*for(int i = 0; i < size; i++){
            for(int l = 0; l < size; l++){
                if(board[i][l] == 0 && opponentNear(i, l)){
                    if(game.moved(i, l)){
                        String s = "SET_" + i + "_"+ l + "_2";
                        msgs.add(s);
                        String msg = "REMOVE_";
                        if (i > 0 && game.breaths(i - 1, l) == 0) {
                            msg = msg + game.remove(i - 1, l);
                        }
                        if (i + 1 < size && game.breaths(i + 1, l) == 0) {
                            msg = msg + game.remove(i + 1, l);
                        }
                        if (l > 0 && game.breaths(i, l - 1) == 0) {
                            msg = msg + game.remove(i, l - 1);
                        }
                        if (l + 1 < size && game.breaths(i, l + 1) == 0) {
                            msg = msg + game.remove(i, l + 1);
                        }
                        if (msg.length() > "REMOVE_".length()) {
                            msgs.add(msg);
                        }
                        System.out.println(msg);
                        i = size;
                        l = size;
                    }
                }
            }
        }*/
        for(int n = 0; n < 1000; n++){
            int i = ThreadLocalRandom.current().nextInt(0, size);
            int l = ThreadLocalRandom.current().nextInt(0, size);
            if(board[i][l] == 0 && opponentNear(i, l)){
                if(game.moved(i, l)){
                    String s = "SET_" + i + "_"+ l + "_2";
                    msgs.add(s);
                    String msg = "REMOVE_";
                    if (i > 0 && game.breaths(i - 1, l) == 0) {
                        msg = msg + game.remove(i - 1, l);
                    }
                    if (i + 1 < size && game.breaths(i + 1, l) == 0) {
                        msg = msg + game.remove(i + 1, l);
                    }
                    if (l > 0 && game.breaths(i, l - 1) == 0) {
                        msg = msg + game.remove(i, l - 1);
                    }
                    if (l + 1 < size && game.breaths(i, l + 1) == 0) {
                        msg = msg + game.remove(i, l + 1);
                    }
                    if (msg.length() > "REMOVE_".length()) {
                        msgs.add(msg);
                    }
                    break;
                }
            }
        }
        if(msgs.isEmpty()) {
            game.pass();
            msgs.add("PASS");
        }
        return msgs;
    }

    private boolean opponentNear(int i, int l) {
        if (i > 0 && board[i - 1][l] != 0 ||
                i < size - 1 && board[i + 1][l] != 0 ||
                l > 0 && board[i][l - 1] != 0 ||
                l < size - 1 && board[i][l + 1] != 0)
            return true;
        else
            return false;
    }

}
