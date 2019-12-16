import client.Board;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class ClientSideTests {
    @Test
    public void putStonesTest(){
        Board b = new Board(3);

        b.put(0,0, 0);
        b.put(1,1, 1);
        b.put(0,1, 0);
        b.put(1,0, 1);
        assertEquals(b.stones.size(), 4);
    }

    @Test
    public void setTest(){
        Board b = new Board(3);
        ArrayList<Board.Stone> chain = new ArrayList<>();
        chain.add(new Board.Stone(0, 0, 1));
        chain.add(new Board.Stone(0, 1, 1));
        chain.add(new Board.Stone(1, 1, 1));
        b.addChain(chain);
        assertFalse(b.allSet());
        b.clicked(0,0);
        b.clicked(1,0);
        b.clicked(2,0);
        b.clicked(2,1);
        b.clicked(2,2);
        b.clicked(1,2);
        b.clicked(0,2);
        assertTrue(b.allSet());
        //System.out.println(b.getStates());
    }

    @Test
    public void statesTest(){
        Board b = new Board(3);
        b.setEndgame(true);
        ArrayList<Board.Stone> chain = new ArrayList<>();
        chain.add(new Board.Stone(0, 0, 1));
        chain.add(new Board.Stone(0, 1, 1));
        chain.add(new Board.Stone(1, 1, 1));
        b.addChain(chain);
        chain.clear();
        chain.add(new Board.Stone(2, 2, 2));
        chain.add(new Board.Stone(2, 1, 2));
        chain.add(new Board.Stone(1, 2, 2));
        b.addChain(chain);
        assertFalse(b.allSet());
        b.setTerritory("TERRITORY_0_2_1_0_2_0_");
        b.clicked(0,0);
        b.clicked(0,0);
        b.clicked(0,0);
        b.clicked(2,2);
        b.clicked(2,2);
        b.clicked(2,0);
        b.clicked(1,0);
        b.clicked(0,2);
        assertTrue(b.allSet());
        assertEquals(b.getStates(),"_0_0_CWN3_A_1_2_CWN3_A_T_111");
        System.out.println("STATES: " + b.getStates());
    }
}
