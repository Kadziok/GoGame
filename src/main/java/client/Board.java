package client;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;

public class Board extends JPanel {
    private int size;
    private int x, y;
    private int color = 1;
    public ArrayList<Stone> stones = new ArrayList<>();
    private ArrayList<Chain> chains = new ArrayList<>();
    private ArrayList<Stone> territory = new ArrayList<>();
    private boolean endgame = false;
    private static Color colors[] = {
            Color.black, Color.black, Color.white,
            new Color(200, 0, 0),
            new Color(0,200,0),
            new Color(98, 98, 98)
    };

    @Override
    protected synchronized void paintComponent(Graphics g) {
        super.paintComponent(g);
        int j = 770 / size;
        int p = j / 2;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(2));
        g2.setPaint(Color.BLACK);
        for (int i = 0; i < size; i++) {
            g2.drawLine(p, i * j + p, (size - 1) * j + p, i * j + p);
            g2.drawLine(i * j + p, p, i * j + p, (size - 1) * j + p);
        }


        if(!endgame) {
            for (Stone s : stones) {
                g2.setPaint(s.getColor());
                g2.fill(new Ellipse2D.Float(s.getY() * j + p / 2, s.getX() * j + p / 2, p, p));
            }
        }
        else {
            for (Chain ch : chains) {
                for (Stone s : ch.getStones()) {
                    g2.setPaint(colors[ch.getState()]);
                    g2.fill(new Ellipse2D.Float(s.getY() * j + p / 2, s.getX() * j + p / 2, p, p));
                    g2.setPaint(s.getColor());
                    g2.fill(new Ellipse2D.Float(s.getY() * j + p / 2 + p / 4,
                            s.getX() * j + p / 2 + p / 4, p / 2, p / 2));
                }
            }
            for(Stone s : territory){
                g2.setPaint(colors[s.getColorNum()]);
                g2.fill(new Rectangle2D.Float(s.getY() * j + p / 2 + p / 4,
                        s.getX() * j + p / 2 + p / 4, p / 2, p / 2));
            }
        }


        if (!endgame) {
            g2.setPaint(colors[color]);
            g2.fill(new Ellipse2D.Float(y * j + p / 2, x * j + p / 2, p, p));
        } else {
            g2.setPaint(new Color(232, 249, 22));
            g2.fill(new Ellipse2D.Float(y * j + p / 2 + p / 4, x * j + p / 2 + p / 4, p / 2, p / 2));
        }

    }
    public Board(int size){
        super();
        this.size = size;
        setBackground(new Color(182, 155, 76));
        repaint();
    }

    public void show(@org.jetbrains.annotations.NotNull String s){
        this.x = Integer.parseInt(s.substring(0, s.indexOf('_')));
        this.y = Integer.parseInt(s.substring(s.indexOf('_') + 1));
        repaint();
    }
    public void put(int i, int l, int c){
        synchronized (stones) {
            stones.add(new Stone(i, l, c));
        }
    }

    public void setColor(int color) {
        this.color = color;
    }

    public synchronized void remove(int i, int l){
        synchronized (stones) {
            Iterator itr = stones.iterator();
            while (itr.hasNext()) {
                Stone x = (Stone) itr.next();
                if (x.getX() == i && x.getY() == l)
                    itr.remove();
            }
        }
    }
    public synchronized void addChain(ArrayList<Stone> chain){
        chains.add(new Chain(chain));
    }

    public void setEndgame(boolean x){
        endgame = x;
        if(!endgame)
            chains.clear();
    }

    public boolean isEndgame() {
        return endgame;
    }

    public void clicked(int i, int l){
        for(Chain ch : chains){
            if(ch.contains(i, l)) {
                ch.changeState();
                repaint();
                return;
            }
        }
        for(Stone s : territory){
            if(s.getX() == i && s.getY() == l)
                s.changeColor();
        }
    }

    public boolean allSet(){
        synchronized (this) {
            for (Chain ch : chains) {
                if(!ch.changed())
                    return false;
            }
            for (Stone s : territory){
                if(!s.changed)
                    return false;
            }
            return true;
        }
    }

    public String getStates(){
        String s = "_";
        synchronized (this) {
            for (Chain ch : chains) {
                s += ch.getId();
            }
        }
        s += "T_";
        for(Stone st : territory){
            s += st.getColorNum();
        }
        return s;
    }

    public synchronized void setTerritory(String s){
        territory = new ArrayList<>();
        System.out.println(s);
        System.out.println("INDEX: " + s.indexOf('_'));
        s = s.substring(s.indexOf('_') + 1);
        System.out.println(s);
        while(s.length() > 0){
            int i = Integer.parseInt(s.substring(0, s.indexOf('_')));
            s = s.substring(s.indexOf('_') + 1);
            int l = Integer.parseInt(s.substring(0, s.indexOf('_')));
            s = s.substring(s.indexOf('_') + 1);
            territory.add(new Stone(i, l,1));
            territory.get(territory.size()-1).changeColor();
        }
    }

    public static class Stone{
        private int x, y;
        int color;
        private boolean changed = false;
        public Stone(int x, int y, int color){
            this.x = x;
            this.y = y;
            this.color = color;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public Color getColor() {
            return colors[color];
        }
        public int getColorNum(){
            return color;
        }
        public void changeColor(){
            changed = true;
            if(color == 0 || color == 1)
                color = 2;
            else
                color = 1;
        }
    }


    private class Chain{
        private ArrayList<Stone> stones;
        private int color, state;
        private int[] id;
        private boolean changed = false;
        public Chain(ArrayList<Stone> stones){
            this.stones = stones;
            color = stones.get(0).getColorNum();
            state = color;
            int minx = size;
            for(Stone s : stones){
                if(s.getX() < minx)
                    minx = s.getX();
            }
            int miny = size;
            for(Stone s : stones){
                if(s.getX() == minx && s.getY() < miny)
                    miny = s.getY();
            }
            id = new int[]{minx, miny};
        }

        public ArrayList<Stone> getStones(){
            return stones;
        }

        public int getIdX(){
            return id[0];
        }
        public int getIdY(){
            return id[1];
        }
        public void changeState(){
            changed = true;
            if(state <  3 || state == 4)
                state = 3;
            else
                state = 4;
        }
        public int getState(){
            return state;
        }

        public boolean contains(int i, int l) {
            for(Stone s : stones){
                if(s.getX() == i && s.getY() == l){
                    return true;
                }
            }
            return false;
        }
        public String getId(){
            return  id[0] + "_" + id[1] + "_C"
                    + (stones.get(0).getColorNum() <= 1 ? "B" : "W")
                    + "N" + stones.size() + "_"
                    + (state == 3 ? "D" : "A") + "_";
        }
        public boolean changed(){
            return changed;
        }
    }
}
