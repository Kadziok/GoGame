package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

public class SwingClient implements ActionListener, ClientGUI {
    private JPanel board;
    private JPanel mainPanel;
    private JButton pass;
    private JButton surrender;
    private JTextArea chat;
    private JTextField chatInput;
    private JPanel chatPlace;
    private JButton[][] btns;
    private int size = 0;
    private Board bd;

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

        frame.setResizable(false);
        frame.setVisible(true);


    }

    public void printH() {
        System.out.println("H1: " + btns[0][0].getY());
        System.out.println("H2: " + btns[1][0].getY());
    }

    public void fillBoard(int n) {
        bd = new Board(n);
        BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                cursorImg, new Point(0, 0), "blank cursor");
        bd.setCursor(blankCursor);
        board.setLayout(new GridLayout(1,1));
        //bd.setPreferredSize(new Dimension(300, 300));
        board.add(bd);
        bd.setLayout(new GridLayout(n, n));
        btns = new JButton[n][n];

        pass.setText("Zrezygnuj z ruchu");
        surrender.setText("Poddaj się");

        chat = new JTextArea(10, 25);
        JScrollPane scrollPane = new JScrollPane(chat);
        chatPlace.setLayout(new GridLayout(1, 1));
        chatPlace.add(scrollPane);
        chat.setEditable(false);
        chat.setLineWrap(true);
        chatInput.addActionListener(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Connection.send("CHAT_" + ((JTextField)e.getSource()).getText());
                ((JTextField)e.getSource()).setText("");
            }
        });
        for (int i = 0; i < n; i++) {
            for (int l = 0; l < n; l++) {
                btns[i][l] = new JButton();
                btns[i][l].addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent evt) {
                        bd.show(((JButton)evt.getSource()).getName());
                    }
                });
                btns[i][l].setName(i + "_" + l);
                btns[i][l].setBorder(BorderFactory.createEmptyBorder());
                btns[i][l].setContentAreaFilled(false);
                btns[i][l].setMargin(new Insets(30, 2, 0, 0));
                bd.add(btns[i][l]);
                btns[i][l].addActionListener(this);
                btns[i][l].setText("");
            }
        }

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JButton b = (JButton) e.getSource();
        String msg = "SET_" + b.getName();
        System.out.println("Sent: " + msg);
        printH();
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
            bd.put(i, l, msg.startsWith("B") ? 1:2);
        }
        else if(msg.startsWith("REMOVE")){
            msg = msg.substring(msg.indexOf('_')+1);
            while(msg.length() > 0){
                int i = Integer.parseInt(msg.substring(0, msg.indexOf('_')));
                msg  = msg.substring(msg.indexOf('_')+1);
                int l = Integer.parseInt(msg.substring(0, msg.indexOf('_')));
                msg  = msg.substring(msg.indexOf('_')+1);
                bd.remove(i, l);
            }
        }
        else if(msg.startsWith("COLOR")){
            bd.setColor(msg.contains("1") ? 1:2);
        }
        else if(msg.startsWith("CHAT_")){
            chat.append("\n" + msg.substring(msg.indexOf("_")+1));
        }

    }
}
