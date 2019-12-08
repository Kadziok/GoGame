package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

public class SwingClient implements ActionListener, ClientGUI {
    private JPanel board;
    private JPanel menu;
    private JPanel mainPanel;
    private JButton[][] btns;
    private int size = 0;

    public static void main(String[] args) {
        JFrame frame = new JFrame("GO");
        var panel = new SwingClient();

        while(true) {
            String dim = JOptionPane.showInputDialog("Please input mark for test 1: ");
            try{
                panel.size = Integer.parseInt(dim);
                break;
            }
            catch (Exception ex){

            }
        }

        new Thread(() -> {
            try {
                Connection.init("127.0.0.1", panel, panel.size);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        //Connection.setSize(i);
        //frame.setUndecorated(true);
        frame.setContentPane(panel.mainPanel);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        panel.fillBoard(panel.size);

        frame.setVisible(true);

        //frame.pack();


    }

    private void printH() {
        System.out.println(btns[0][0].getY());
        System.out.println(btns[1][0].getY());
    }

    public void fillBoard(int n) {
        /*board.setLayout(new GridLayout(n, n));
        JButton temp = new JButton();
        board.add(temp);
        for(int i = 1; i < n*n; i++) {
            board.add(new JButton());
        }
        System.out.println(temp.getHeight());*/
        //board.removeAll();
        board.setLayout(new GridLayout(n, n));
        btns = new JButton[n][n];
        /*BufferedImage buttonIcon = null;
        try {
            //buttonIcon = resize(ImageIO.read(new File("pole.png")), (int)board.getHeight()/n
            //                    , (int)board.getHeight()/n);
            int h = (int) board.getHeight() / n;
            buttonIcon = ImageIO.read(new File("pole.png"));
            //buttonIcon = resize(buttonIcon, h, h);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        for (int i = 0; i < n; i++) {
            for (int l = 0; l < n; l++) {
                //btns[i][l] = new JButton(new ImageIcon(buttonIcon));
                btns[i][l] = new JButton();
                btns[i][l].setName(i + "_" + l);
                //btns[i][l].setBorder(BorderFactory.createEmptyBorder());
                btns[i][l].setContentAreaFilled(false);
                //btns[i][l].setMargin(new Insets(0, 0, 0, 0));
                board.add(btns[i][l]);
                btns[i][l].addActionListener(this);
                btns[i][l].setText("");
            }
        }

        /*for (int i = 0; i < n; i++) {
            for (int l = 0; l < n; l++) {
                Image dimg = buttonIcon.getScaledInstance(80,
                        80,
                        Image.SCALE_SMOOTH);
                btns[i][l].setIcon(new ImageIcon(dimg));
            }
        }*/
    }

    private static BufferedImage resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        //Graphics2D g2d = dimg.createGraphics();
        //g2d.drawImage(tmp, 0, 0, null);
        //g2d.dispose();

        return dimg;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JButton b = (JButton) e.getSource();
        String msg = "SET_" + b.getName();
        System.out.println("Sent: " + msg);
        synchronized (this) {
            Connection.send(msg);
        }
    }

    @Override
    public void action(String msg) {
        if(msg.startsWith("SET")){
            System.out.println("Received: " + msg);
            System.out.println(msg.length());
            msg  = msg.substring(msg.indexOf('_')+1);
            System.out.println(msg);
            int i = Integer.parseInt(msg.substring(0, msg.indexOf('_')));
            System.out.println(i);
            msg  = msg.substring(msg.indexOf('_')+1);
            int l = Integer.parseInt(msg.substring(0, msg.indexOf('_')));
            System.out.println(l);
            msg  = msg.substring(msg.indexOf('_')+1);
            btns[i][l].setText(msg);
        }
        if(msg.startsWith("REMOVE")){
            msg = msg.substring(msg.indexOf('_')+1);
            while(msg.length() > 0){
                int i = Integer.parseInt(msg.substring(0, msg.indexOf('_')));
                msg  = msg.substring(msg.indexOf('_')+1);
                int l = Integer.parseInt(msg.substring(0, msg.indexOf('_')));
                msg  = msg.substring(msg.indexOf('_')+1);
                btns[i][l].setText("");
            }
        }
    }

}
