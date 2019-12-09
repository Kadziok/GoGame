package client;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;

public class Board extends JPanel {
    private int size;
    private int x, y;
    private Color color = Color.black;
    private ArrayList<Stone> stones = new ArrayList<>();

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int j = 770/size;
        int p = j/2;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(2));
        g2.setPaint(Color.BLACK);
        for(int i = 0; i < size; i++){
            g2.drawLine(p, i*j+p, (size-1)*j+p, i*j+p);
            g2.drawLine(i*j+p, p, i*j+p, (size-1)*j +p);
        }
        g2.setPaint(color);
        g2.fill(new Ellipse2D.Float(y*j+p/2, x*j+p/2,p, p));

        synchronized (stones) {
            for (Stone s : stones) {
                g2.setPaint(s.getColor());
                g2.fill(new Ellipse2D.Float(s.getY() * j + p / 2, s.getX() * j + p / 2, p, p));
            }
        }

    }
    public Board(int size){
        super();
        this.size = size;
        setBackground(new Color(182, 155, 76));
        repaint();
    }

    public void show(String s){

        this.x = Integer.parseInt(s.substring(0, s.indexOf('_')));
        this.y = Integer.parseInt(s.substring(s.indexOf('_') + 1));
        //System.out.println("X = " + x + "| Y = " + y);
        repaint();
    }
    public void put(int i, int l, int c){
        synchronized (stones) {
            stones.add(new Stone(i, l, c));
        }
    }

    public void setColor(int color) {
        this.color = color==1 ? Color.black : Color.white;
    }

    public void remove(int i, int l){
        synchronized (stones) {
            Iterator itr = stones.iterator();
            while (itr.hasNext()) {
                Stone x = (Stone) itr.next();
                if (x.getX() == i && x.getY() == l)
                    itr.remove();
            }
        }
    }

    private class Stone{
        private int x, y;
        Color color;
        public Stone(int x, int y, int color){
            this.x = x;
            this.y = y;
            this.color = color==1 ? Color.black : Color.white;
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
            return color;
        }
    }
}
