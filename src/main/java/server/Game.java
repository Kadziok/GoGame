package server;

import java.io.PrintWriter;

public class Game {
    private PrintWriter[] player = new PrintWriter[2];
    private int[] colors = new int[2];
    private boolean waiting;
    private int size;
    private int[][] board;
    private String move;
    private boolean[][] checked;
    public int[] points = {0, 0};
    private boolean passed = false;
    private boolean end = false;
    private boolean botMode = false;
    private String[] states = {null, null};

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
        passed = false;
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
            points[c == 1 ? 1 : 0]++;
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

    public void pass(){
        if(move == "B") {
            move = "W";
        }
        else {
            move = "B";
        }
        if(passed)
            end = true;
        else {
            passed = true;
        }
    }

    public boolean ended(){
        return end;
    }

    public String chains(){
        String s = "CHAINS_";
        for(int i = 0; i < size; i++){
            for(int l = 0; l < size; l++){
                checked[i][l] = false;
            }
        }

        for(int i = 0; i < size; i++){
            for(int l = 0; l < size; l++){
                if(!checked[i][l] && board[i][l] == 1){
                    s = s + "C1_" + chain(i, l, 1);
                }
            }
        }

        for(int i = 0; i < size; i++){
            for(int l = 0; l < size; l++){
                checked[i][l] = false;
            }
        }

        for(int i = 0; i < size; i++){
            for(int l = 0; l < size; l++){
                if(!checked[i][l] && board[i][l] == 2){
                    s = s + "C2_" + chain(i, l, 2);
                }
            }
        }

        return s;
    }

    private String chain(int x, int y, int c){
        checked[x][y] = true;
        if(board[x][y] == c) {
            return x + "_" + y + "_"
                    + (x > 0 ? (checked[x - 1][y] ? "" : chain(x - 1, y, c)) : "")
                    + (x + 1 < size ? (checked[x + 1][y] ? "" : chain(x + 1, y, c)) : "")
                    + (y > 0 ? (checked[x][y - 1] ? "" : chain(x, y - 1, c)) : "")
                    + (y + 1 < size ? (checked[x][y + 1] ? "" : chain(x, y + 1, c)) : "");
        }
        else
            return "";
    }

    public void restart(){
        states = new String[]{null, null};
        end = false;
    }

    public void setStates(PrintWriter p, String states){
        if(player[0] == p){
            this.states[0] = states;
        }
        else
            this.states[1] = states;
    }

    public void duplicateState(){
        if(states[0] != null)
            states[1] = states[0];
        else
            states[0] = states[1];
    }

    public boolean statesSet(){
        if(states[0] != null && states[1] != null)
            return true;
        else
            return false;
    }

    public boolean statesEqual(){
        if(states[0].equals(states[1]))
            return true;
        else
            return false;
    }

    public String territory(){
        String s = "TERRITORY_";
        for(int i = 0; i < size; i++){
            for(int l = 0; l < size; l++){
                if(board[i][l] == 0)
                    s += i + "_" + l + "_";
            }
        }
        return s;
    }

    public int[] finalScore(){
        if(states[0] != null){
            String s = states[0];
            while(s.contains("C")){
                s = s.substring(s.indexOf("C"));
                if(s.charAt(s.indexOf("_")+1) == 'D'){
                    int i = Integer.parseInt(s.substring(3, s.indexOf("_")));
                    if(s.charAt(1) == 'B')
                        points[0] += i;
                    else
                        points[1] += i;
                }
                s = s.substring(s.indexOf("_"));
            }

            s = s.substring(s.indexOf('T')+1);
            int len = s.length();
            int bPoints = 0;
            while(s.contains("1")){
                bPoints++;
                if(s.endsWith("1"))
                    s = "";
                else
                    s = s.substring(s.indexOf("1")+1);
            }
            int wPoints = len - bPoints;
            points[0] -= wPoints;
            points[1] -= bPoints;

            return points;
        }
        return null;
    }
    public int[][] getBoard(){
        return board;
    }

}
