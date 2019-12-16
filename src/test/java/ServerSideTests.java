import org.junit.Test;
import server.Game;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import static org.junit.Assert.*;

public class ServerSideTests {
    @Test
    public void joinTest(){
        PrintWriter p1 = null, p2 = null;

        try {
            p1 = new PrintWriter(new File("t.txt"));
            p2 = new PrintWriter(new File("s.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Game g = new Game(p1, 10);
        g.join(p2);
        assertEquals(p1, g.partner(p2));
        assertEquals(p2, g.partner(p1));
    }

    @Test
    public void breathsTest(){
        PrintWriter p1 = null, p2 = null;

        try {
            p1 = new PrintWriter(new File("t.txt"));
            p2 = new PrintWriter(new File("s.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Game g = new Game(p1, 10);
        g.join(p2);

        assertTrue(g.isMyMove("B"));
        assertTrue(g.moved(0,0));
        assertTrue(g.moved(1,0));
        assertTrue(g.moved(0,1));
        assertEquals(2, g.breaths(0, 0));
    }

    @Test
    public void removeTest(){
        PrintWriter p1 = null, p2 = null;

        try {
            p1 = new PrintWriter(new File("t.txt"));
            p2 = new PrintWriter(new File("s.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Game g = new Game(p1, 10);
        g.join(p2);

        g.moved(0,0);
        g.moved(1,1);
        g.moved(0,1);
        g.moved(0,2);
        assertEquals(g.remove(0,0), "0_0_0_1_");
    }

    @Test
    public void  passTest(){
        PrintWriter p1 = null, p2 = null;

        try {
            p1 = new PrintWriter(new File("t.txt"));
            p2 = new PrintWriter(new File("s.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Game g = new Game(p1, 10);
        g.join(p2);
        g.pass();
        assertFalse(g.ended());
        g.pass();
        assertTrue(g.ended());
    }

    @Test
    public void chainsTest(){
        PrintWriter p1 = null, p2 = null;

        try {
            p1 = new PrintWriter(new File("t.txt"));
            p2 = new PrintWriter(new File("s.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Game g = new Game(p1, 10);
        g.join(p2);

        g.moved(5,5);
        g.moved(5,6);
        g.moved(4,5);
        g.moved(4,6);
        g.moved(4,4);
        g.moved(0,0);
        g.moved(5,7);
        assertEquals(g.chains(), "CHAINS_C1_4_4_4_5_5_5_C1_5_7_C2_0_0_C2_4_6_5_6_");
    }

    @Test
    public void moveTest(){
        PrintWriter p1 = null, p2 = null;

        try {
            p1 = new PrintWriter(new File("t.txt"));
            p2 = new PrintWriter(new File("s.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Game g = new Game(p1, 10);
        g.join(p2);

        g.moved(0,1);
        g.moved(5,6);
        assertTrue(g.moved(1,0));
        assertFalse(g.moved(0, 0));
    }

    @Test
    public void territoryTest(){
        PrintWriter p1 = null, p2 = null;

        try {
            p1 = new PrintWriter(new File("t.txt"));
            p2 = new PrintWriter(new File("s.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Game g = new Game(p1, 3);
        g.join(p2);

        g.moved(0,0);
        g.moved(2,2);
        g.moved(1,1);
        assertEquals(g.territory(),"TERRITORY_0_1_0_2_1_0_1_2_2_0_2_1_");
    }

    @Test
    public void endgameTest() {
        PrintWriter p1 = null, p2 = null;

        try {
            p1 = new PrintWriter(new File("t.txt"));
            p2 = new PrintWriter(new File("s.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Game g = new Game(p1, 3);
        g.join(p2);

        g.setStates(p1, "ABC");
        assertFalse(g.statesEqual());
        g.setStates(p2, "DEF");
        assertFalse(g.statesEqual());
        g.setStates(p2, "ABC");
        assertTrue(g.statesEqual());
        g.setStates(p1, "XYZ");
        assertFalse(g.statesEqual());
        g.duplicateState();
        assertTrue(g.statesEqual());
        g.restart();
        assertFalse(g.statesEqual());
    }

    @Test
    public void scoreTest(){
        PrintWriter p1 = null, p2 = null;

        try {
            p1 = new PrintWriter(new File("t.txt"));
            p2 = new PrintWriter(new File("s.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Game g = new Game(p1, 3);
        g.join(p2);

        assertNull(g.finalScore());

        g.moved(1,1);
        g.moved(0,2);
        g.moved(2,1);
        g.moved(2,2);
        g.moved(1,2);
        //3 czarne martwe, białe 1 martwy, 1 żywy, po 2 terytorium
        g.setStates(p1, "STATES_1_1_CBN3_D_0_2_CWN1_A_2_2_CWN1_D_T_1122");
        g.setStates(p2, "STATES_1_1_CBN3_D_0_2_CWN1_A_2_2_CWN1_D_T_1122");
        int[] s = g.finalScore();
        assertEquals(s[0], 0);
        assertEquals(s[1],-1);
    }
}
