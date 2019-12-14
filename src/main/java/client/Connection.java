package client;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;


public class Connection {

    private static Connection connection;
    private String serverAddress;
    private Scanner in;
    private PrintWriter out;
    private ClientGUI gui;
    private Socket socket;
    private int size;
    private boolean partner = false;
    private int botMode = 0;

    private Connection(String serverAddress, ClientGUI gui) {
        this.serverAddress = serverAddress;
        this.gui = gui;
    }

    private void run() throws IOException {
        try {
            socket = new Socket(serverAddress, 59007);
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println("STH");
            while(true) {
                if(size != 0) {
                    out.println("SIZE_" + size + "M" + botMode);
                    break;
                }
            }
            System.out.println("LEFT LOOP");

            while (in.hasNextLine()) {
                var line = in.nextLine();
                if(line.equals("PARTNER FOUND"))
                    partner = true;
                received(line.toString());
                /*synchronized (this) {
                    if (!msgs.isEmpty()) {
                        for (String s : msgs) {
                            out.println(s);
                            System.out.println("MSG SENT");
                        }
                        msgs.clear();

                    }
                }*/
                //out.println("TEST");
            }
        } finally {
            //frame.setVisible(false);
            //frame.dispose();
        }
    }

    public static void init(String serverAddress, ClientGUI gui, int size, int mode) throws Exception {
        if(connection == null)
            System.out.println("NULL C");
        Connection.connection = new Connection(serverAddress, gui);
        Connection.connection.size = size;
        Connection.connection.botMode = mode;

        connection.run();
    }
    private void received(String msg){
        gui.action(msg);
    }

    public static void send(String msg){
        connection.out.println(msg);
    }

    public static Connection getConnection(){
        return connection;
    }
    public static PrintWriter  getOut() {
        synchronized (Connection.class) {
            return connection.out;
        }
    }
    public static void setSize(int size){
        synchronized (Connection.class) {
            Connection.connection.size = size;
        }
    }

    public static boolean foundPartner(){
        return connection.partner;
    }

}