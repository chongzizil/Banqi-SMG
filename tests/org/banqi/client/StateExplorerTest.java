package org.banqi.client;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class StateExplorerTest {
  
  /** The object under test. */
  StateExplorerImpl stateExplorer = new StateExplorerImpl();
  BanqiLogic banqiLogic = new BanqiLogic();
 
  private final String rId = "7";
  private final String bId = "47";
  private final List<String> playerIds = Lists.newArrayList(rId, bId);
  private final Color turnOfColor = Color.R;

  /*
   * 
   * Test movePiece.
   * 
   * {P0 |   |   |   |   |   |   |   }
   * {   |P9 |   |   |   |   |   |   }
   * {   |   |   |   |   |   |   |   }
   * {P23|P24|   |   |   |   |   |P31} 
   * 
   * 
   **/
  
  /*
   * Can not move because the square is already occupied by a same color face-up piece.
   */
  @Test
  public void movePiece() {
    Map<String, Object> gameApiState = new HashMap<String, Object>();
    gameApiState.put("S0",  "P0");
    gameApiState.put("S9",  "P9");
    gameApiState.put("S31", "P31");
    gameApiState.put("S23", "P23");
    gameApiState.put("S24", "P24");
    gameApiState.put("P0",  "rgen");
    gameApiState.put("P9",  "radv");
    gameApiState.put("P23", "bgen");
    gameApiState.put("P24", null);
    gameApiState.put("P31", "rsol");
    
    State state = banqiLogic.gameApiStateToBanqiState(gameApiState, turnOfColor, playerIds);
    
    Set<Position> possibleStartPositions = new HashSet<Position>();
    possibleStartPositions.add(new Position(1, 1));
    possibleStartPositions.add(new Position(2, 2));
    possibleStartPositions.add(new Position(4, 8));
    assertEquals(stateExplorer.getPossibleStartPositions(state), possibleStartPositions);
  }
  
  /*
   * 
   * Test movePiece.
   * 
   * {P0 |P1 |   |   |   |   |   |   }
   * {P8 |   |   |   |   |   |   |   }
   * {P16|   |   |   |   |   |   |   }
   * {P24|P25|   |   |   |   |   |   } 
   * 
   * 
   **/
  
  /*
   * Can not move because the square is already occupied by a same color face-up piece.
   */
  @Test
  public void testCapture() {
    Map<String, Object> gameApiState = ImmutableMap.<String, Object>builder()
        .put("S0",  "P0")
        .put("S1",  "P1")
        .put("S8",  "P8")
        .put("S16", "P16")
        .put("S24", "P24")
        .put("S25", "P25")
        .put("P0",  "rgen")
        .put("P1",  "bsol")
        .put("P8",  "bsol")
        .put("P16", "bsol")
        .put("P24", "radv")
        .put("P25", "bsol")
      .build();
    
    State state = banqiLogic.gameApiStateToBanqiState(gameApiState, turnOfColor, playerIds);
    
    Set<Position> possibleStartPositions = new HashSet<Position>();
    possibleStartPositions.add(new Position(4, 1));
    assertEquals(stateExplorer.getPossibleStartPositions(state), possibleStartPositions);
    
    List<Integer> t = stateExplorer.convertFromPosToIndex(
        stateExplorer.getPossibleStartPositions(state));
    System.out.print("testCapture:");
    System.out.println(stateExplorer.getPossibleStartPositions(state));
    System.out.println(t);
  }

  
  /*
   * 
   * Test Cannon.
   * 
   * {   |P1 |   |   |   |   |   |   } 7
   * {   |P9 |   |   |   |   |   |   } 15
   * {   |   |   |   |   |   |   |   } 23
   * {   |   |   |   |   |   |   |P31} 31
   * 
   * 
   **/
  
  @Test
  public void testCannon() {
    Map<String, Object> gameApiState = ImmutableMap.<String, Object>builder()
        .put("S9",  "P9")
        .put("S1",  "P1")
        .put("S31", "P31")
        .put("P9",  "rgen")
        .put("P1",  "badv")
        .put("P31", "bsol")
        .build();
    
    
    State state = banqiLogic.gameApiStateToBanqiState(gameApiState, turnOfColor, playerIds);
    
    Set<Position> possibleStartPositions = new HashSet<Position>();
    possibleStartPositions.add(new Position(2, 2));
    assertEquals(stateExplorer.getPossibleStartPositions(state), possibleStartPositions);
    
    List<Integer> t = stateExplorer.convertFromPosToIndex(
        stateExplorer.getPossibleStartPositions(state));
    System.out.print("testCannon:");
    System.out.println(stateExplorer.getPossibleStartPositions(state));
    System.out.println(t);
  }
  
  /*
   * 
   * Test cannon capture.
   * 
   * {P0 |P1 |P2 |P3 |P4 |P5 |P6 |P7 }
   * {P8 |   |P10|   |   |   |   |   }
   * {P16|   |   |   |   |   |   |   }
   * {P24|   |   |   |   |   |   |   } 
   * 
   * 
   **/
  
  @Test
  public void testRedCannonCanNotCapture() {
    Map<String, Object> gameApiState = new HashMap<String, Object>();
    gameApiState.put("S0",  "P0");
    gameApiState.put("S1",  "P1");
    gameApiState.put("S2",  "P2");
    gameApiState.put("S3",  "P3");
    gameApiState.put("S4",  "P4");
    gameApiState.put("S5",  "P5");
    gameApiState.put("S6",  "P6");
    gameApiState.put("S7",  "P7");
    gameApiState.put("S8",  "P8");
    gameApiState.put("S10",  "P10");
    gameApiState.put("S16", "P16");
    gameApiState.put("S24", "P24");
    gameApiState.put("P0",  "rcan");
    gameApiState.put("P1",  "bsol");
    gameApiState.put("P2",  null);
    gameApiState.put("P3",  "bsol");
    gameApiState.put("P4",  null);
    gameApiState.put("P5",  null);
    gameApiState.put("P6",  null);
    gameApiState.put("P7",  null);
    gameApiState.put("P8",  null);
    gameApiState.put("P10", null);
    gameApiState.put("P16", null);
    gameApiState.put("P24", null);

    State state = banqiLogic.gameApiStateToBanqiState(gameApiState, turnOfColor, playerIds);
    
    Set<Position> possibleStartPositions = new HashSet<Position>();
    assertEquals(stateExplorer.getPossibleStartPositions(state), possibleStartPositions);
    
    List<Integer> t = stateExplorer.convertFromPosToIndex(
        stateExplorer.getPossibleStartPositions(state));
    System.out.print("testRedCannonCanNotCapture:");
    System.out.println(stateExplorer.getPossibleStartPositions(state));
    System.out.println(t);
  }
  
  @Test
  public void testRedCannonCanCapture() {
    Map<String, Object> gameApiState = new HashMap<String, Object>();
    gameApiState.put("S0",  "P0");
    gameApiState.put("S1",  "P1");
    gameApiState.put("S2",  "P2");
    gameApiState.put("S3",  "P3");
    gameApiState.put("S4",  "P4");
    gameApiState.put("S5",  "P5");
    gameApiState.put("S6",  "P6");
    gameApiState.put("S7",  "P7");
    gameApiState.put("S8",  "P8");
    gameApiState.put("S10", "P10");
    gameApiState.put("S16", "P16");
    gameApiState.put("S24", "P24");
    gameApiState.put("P0",  "rcan");
    gameApiState.put("P1",  "bsol");
    gameApiState.put("P2",  "rcan");
    gameApiState.put("P3",  null);
    gameApiState.put("P4",  null);
    gameApiState.put("P5",  null);
    gameApiState.put("P6",  null);
    gameApiState.put("P7",  null);
    gameApiState.put("P8",  null);
    gameApiState.put("P10", null);
    gameApiState.put("P16", null);
    gameApiState.put("P24", null);

    State state = banqiLogic.gameApiStateToBanqiState(gameApiState, turnOfColor, playerIds);
    
    Set<Position> possibleStartPositions = new HashSet<Position>();
    possibleStartPositions.add(new Position(1, 1));
    //assertEquals(stateExplorer.getPossibleStartPositions(state), possibleStartPositions);
    
    List<Integer> t = stateExplorer.convertFromPosToIndex(
        stateExplorer.getPossibleStartPositions(state));
    System.out.print("testRedCannonCanCapture:");
    System.out.println(stateExplorer.getPossibleStartPositions(state));
    System.out.println(t);
  }
  
  /*
   * 
   * Test cannon capture.
   * 
   * {P0 |P1 |P2 |P3 |P4 |P5 |P6 |P7 }
   * {P8 |P9 |P10|P11|   |   |   |   }
   * {P16|P17|P18|   |   |   |   |   }
   * {P24|   |   |   |   |   |   |   } 
   * 
   * 
   **/
  
  @Test
  public void newTest() {
    Map<String, Object> gameApiState = new HashMap<String, Object>();
    gameApiState.put("S0",  "P0");
    gameApiState.put("S1",  "P1");
    gameApiState.put("S2",  "P2");
    gameApiState.put("S7",  "P7");
    gameApiState.put("S8",  "P8");
    gameApiState.put("S9",  "P9");
    gameApiState.put("S10", "P10");
    gameApiState.put("S16", "P16");
    gameApiState.put("S17", "P17");
    gameApiState.put("S18", "P18");
    gameApiState.put("P0",  "rgen");
    gameApiState.put("P1",  "radv");
    gameApiState.put("P2",  null);
    gameApiState.put("P7",  "rcha");
    gameApiState.put("P8",  "rcha");
    gameApiState.put("P9",  "rele");
    gameApiState.put("P10", null);
    gameApiState.put("P16", null);
    gameApiState.put("P17", null);
    gameApiState.put("P18", null);
    gameApiState.put("P16", null);
    gameApiState.put("P24", null);

    State state = banqiLogic.gameApiStateToBanqiState(gameApiState, turnOfColor, playerIds);
    
    Set<Position> possibleStartPositions = new HashSet<Position>();
    possibleStartPositions.add(new Position(1, 1));
    List<Integer> t = stateExplorer.convertFromPosToIndex(
        stateExplorer.getPossibleStartPositions(state));
    System.out.print("test:");
    System.out.println(stateExplorer.getPossibleStartPositions(state));
    System.out.println(t);
    
    //assertEquals(stateExplorer.getPossibleStartPositions(state), possibleStartPositions);
  }
  
  
  
}
