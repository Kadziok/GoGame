package server;

import java.io.PrintWriter;

public class Game {
    private PrintWriter[] player = new PrintWriter[2];
    private int[] colors = new int[2];
    private boolean waiting;
    private int size;
    private int[][] board;
    private String move;
    boolean[][] checked;

    public Game(PrintWriter out, int size){
        player[0] = out;
        this.size = size;
        colors[0] = 1;
        move = "B";
        waiting = true;
        board = new int[size][size];
        checked = new boolean[size][size];

        for(int i = 0; i < size; i++){
            for(int l = 0; l < size; l++){
                board[i][l] = 0;
            }
        }

    }

    public void join(PrintWriter out){
        player[1] = out;
        colors[1] = 2;
        waiting = false;
    }

    public PrintWriter partner(PrintWriter out){
        if(out == player[0])
            return player[1];
        else
            return player[0];
    }

    public boolean moved(int x, int y){
        if(board[x][y] != 0) {
            System.out.println("---NO MOVE SAVED---");
            return false;
        }
        int temp = (move == "B") ? 1:2;
        board[x][y] = temp;;
        if(breaths(x, y) == 0) {
            if (!((x > 0 && breathsH(x - 1, y, temp) == 0) ||
                    (x + 1 < size && breathsH(x + 1, y, temp) == 0) ||
                    (y > 0 && breathsH(x, y - 1, temp) == 0) ||
                    (y + 1 < size && breathsH(x, y + 1, temp) == 0))) {
                board[x][y] = 0;
                return false;
            }

        }

        if(move == "B") {
            move = "W";
            board[x][y] = 1;
        }
        else {
            move = "B";
            board[x][y] = 2;
        }
        return true;
    }

    public boolean isMyMove(String c){
        if(c.equals(move))
            return true;
        else
            return false;
    }

    public int getSize(){
        return size;
    }

    public boolean isWaiting() {
        return waiting;
    }

    public int breaths(int x, int y){
        if(board[x][y] == 0)
            return -1;

        for(int i = 0; i < size; i++){
            for(int l = 0; l < size; l++){
                checked[i][l] = false;
            }
        }
        synchronized (board) {
            return breaths(x, y, board[x][y]);
        }
    }

    private int breaths(int x, int y, int c) {
        /*if (x < 0 || x >= size || y < 0 || y >= size) {
            return 0;
        }*/
        checked[x][y] = true;
        if (board[x][y] == 0) {
            return 1;
        }
        if (board[x][y] != c) {
            return 0;
        }
        if (board[x][y] == c)
            return (x > 0 ? (checked[x - 1][y] ? 0 : breaths(x - 1, y, c)) : 0)
                    + (x + 1 < size ? (checked[x + 1][y] ? 0 : breaths(x + 1, y, c)) : 0)
                    + (y > 0 ? (checked[x][y - 1] ? 0 : breaths(x, y - 1, c)) : 0)
                    + (y + 1 < size ? (checked[x][y + 1] ? 0 : breaths(x, y + 1, c)) : 0);
        return 0;

    }

    public void undo(int x, int y){
        if(board[x][y] == 0)
            return;
        board[x][y] = 0;
        if(move == "B") {
            move = "W";
            board[x][y] = 1;
        }
        else {
            move = "B";
            board[x][y] = 2;
        }
    }

    public String remove(int x, int y) {
        //String msg = "REMOVE_";
        for(int i = 0; i < size; i++){
            for(int l = 0; l < size; l++){
                checked[i][l] = false;
            }
        }
        System.out.println("COLOR: " + board[x][y]);
        synchronized (board){
            return rm(x, y, move == "B" ? 1:2);
        }
    }

    private String rm(int x, int y, int c){
        if(checked[x][y])
            return "";
        checked[x][y] = true;

        if(board[x][y] == c) {
            board[x][y] = 0;
            return x + "_" + y + "_"
                    + (x > 0 ? (checked[x - 1][y] ? "" : rm(x - 1, y, c)) : "")
                    + (x + 1 < size ? (checked[x + 1][y] ? "" : rm(x + 1, y, c)) : "")
                    + (y > 0 ? (checked[x][y - 1] ? "" : rm(x, y - 1, c)) : "")
                    + (y + 1 < size ? (checked[x][y + 1] ? "" : rm(x, y + 1, c)) : "");
        }
        else
            return "";
    }

    public boolean isFree(int x, int y){
        if(board[x][y] == 0)
            return true;
        else
            return false;
    }
    private int breathsH(int x, int y, int c){
        if(board[x][y] == c)
            return 1;
        else
            return breaths(x, y);
    }

}
