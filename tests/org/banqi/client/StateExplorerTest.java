package org.banqi.client;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
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
  private static final String C = "C";
  private final List<String> playerIds = Lists.newArrayList(rId, bId);
  private final Color turnOfColor = Color.R;

  public static final Map<String, Object> getEmptyBoardState() {
    Map<String, Object> emptyBoardState = new HashMap<String, Object>();
    for (int i = 0; i < 32; i++) {
      String cell = C + i;
      String emptyCell = "eemp";
      emptyBoardState.put(cell, emptyCell);
    }
    return emptyBoardState;
  }

  /*
   * 
   * Test movePiece.
   * 
   * {C0 |   |   |   |   |   |   |   }
   * {   |C9 |   |   |   |   |   |   }
   * {   |   |   |   |   |   |   |   }
   * {C24|C25|   |   |   |   |   |C31}
   */

  /*
   * Can not move because the square is already occupied by a same color face-up
   * piece.
   */
  @Test
  public void testMovePieceA() {
    Map<String, Object> gameApiState = getEmptyBoardState();
    gameApiState.put("C0", "rgen");
    gameApiState.put("C9", "radv");
    gameApiState.put("C24", null);
    gameApiState.put("C25", "bgen");
    gameApiState.put("C31", "rsol");
    gameApiState.put("O", ImmutableList.of());

    BanqiState state = banqiLogic.gameApiStateToBanqiState(gameApiState,
        turnOfColor, playerIds);

    Set<Position> possibleStartPositions = new HashSet<Position>();
    possibleStartPositions.add(new Position(1, 1));
    possibleStartPositions.add(new Position(2, 2));
    possibleStartPositions.add(new Position(4, 8));
    possibleStartPositions.add(new Position(4, 1));
    assertEquals(stateExplorer.getPossibleStartPositions(state),
        possibleStartPositions);
  }

  /*
   * 
   * Test movePiece.
   * 
   * {C0 |C1 |   |   |   |   |   |   }
   * {C8 |   |   |   |   |   |   |   }
   * {C16|   |   |   |   |   |   |   }
   * {C24|C25|   |   |   |   |   |   }
   */

  /*
   * Can not move because the square is already occupied by a same color face-up
   * piece.
   */
  @Test
  public void testMovePieceB() {
    Map<String, Object> gameApiState = getEmptyBoardState();
    gameApiState.put("C0", "rgen");
    gameApiState.put("C1", "bsol");
    gameApiState.put("C8", "bsol");
    gameApiState.put("C16", "bsol");
    gameApiState.put("C24", "radv");
    gameApiState.put("C25", "bsol");
    gameApiState.put("O", ImmutableList.of());

    BanqiState state = banqiLogic.gameApiStateToBanqiState(gameApiState,
        turnOfColor, playerIds);

    Set<Position> possibleStartPositions = new HashSet<Position>();
    possibleStartPositions.add(new Position(4, 1));
    assertEquals(stateExplorer.getPossibleStartPositions(state),
        possibleStartPositions);

    // List<Integer> t = stateExplorer.convertFromPosToIndex(
    // stateExplorer.getPossibleStartPositions(state));
    // System.out.print("testCapture:");
    // System.out.println(stateExplorer.getPossibleStartPositions(state));
    // System.out.println(t);
  }

  /*
   * 
   * Test Cannon move.
   * 
   * {   |C1 |   |   |   |   |   |   }
   * {   |C9 |   |   |   |   |   |   }
   * {   |   |   |   |   |   |   |   }
   * {   |   |   |   |   |   |   |C31}
   */

  @Test
  public void testCannonMoveA() {
    Map<String, Object> gameApiState = getEmptyBoardState();
    gameApiState.put("C9", "rgen");
    gameApiState.put("C1", "badv");
    gameApiState.put("C31", "bsol");
    gameApiState.put("O", ImmutableList.of());

    BanqiState state = banqiLogic.gameApiStateToBanqiState(gameApiState,
        turnOfColor, playerIds);

    Set<Position> possibleStartPositions = new HashSet<Position>();
    possibleStartPositions.add(new Position(2, 2));
    assertEquals(stateExplorer.getPossibleStartPositions(state),
        possibleStartPositions);

    // List<Integer> t = stateExplorer.convertFromPosToIndex(
    // stateExplorer.getPossibleStartPositions(state));
    // System.out.print("testCannon:");
    // System.out.println(stateExplorer.getPossibleStartPositions(state));
    // System.out.println(t);
  }
  
  /*
   * 
   * Test Cannon move.
   * 
   * {   |C1 |   |   |   |   |   |   }
   * {   |   |   |   |   |   |   |   }
   * {   |   |   |   |   |   |   |   }
   * {   |   |   |   |   |   |   |C31}
   */

  @Test
  public void testCannonMoveB() {
    Map<String, Object> gameApiState = getEmptyBoardState();
    gameApiState.put("C1", "rcan");
    gameApiState.put("C31", "bsol");
    gameApiState.put("O", ImmutableList.of());

    BanqiState state = banqiLogic.gameApiStateToBanqiState(gameApiState,
        turnOfColor, playerIds);

    Set<Position> possibleStartPositions = new HashSet<Position>();
    possibleStartPositions.add(new Position(1, 2));
    assertEquals(stateExplorer.getPossibleStartPositions(state),
        possibleStartPositions);

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
   * {C0 |C1 |C2 |C3 |C4 |C5 |C6 |C7 }
   * {C8 |   |C10|   |   |   |   |   }
   * {C16|   |   |   |   |   |   |   }
   * {C24|   |   |   |   |   |   |   }
   */

  @Test
  public void testRedCannonCanNotCapture() {
    Map<String, Object> gameApiState = getEmptyBoardState();
    gameApiState.put("C0", "rcan");
    gameApiState.put("C1", "bsol");
    gameApiState.put("C2", null);
    gameApiState.put("C3", "bsol");
    gameApiState.put("C4", null);
    gameApiState.put("C5", null);
    gameApiState.put("C6", null);
    gameApiState.put("C7", null);
    gameApiState.put("C8", null);
    gameApiState.put("C10", null);
    gameApiState.put("C16", null);
    gameApiState.put("C24", null);
    gameApiState.put("O", ImmutableList.of());

    BanqiState state = banqiLogic.gameApiStateToBanqiState(gameApiState,
        turnOfColor, playerIds);

    Set<Position> possibleStartPositions = new HashSet<Position>();
    possibleStartPositions.add(new Position(1, 3));
    possibleStartPositions.add(new Position(1, 5));
    possibleStartPositions.add(new Position(1, 6));
    possibleStartPositions.add(new Position(1, 7));
    possibleStartPositions.add(new Position(1, 8));
    possibleStartPositions.add(new Position(2, 1));
    possibleStartPositions.add(new Position(2, 3));
    possibleStartPositions.add(new Position(3, 1));
    possibleStartPositions.add(new Position(4, 1));
    assertEquals(stateExplorer.getPossibleStartPositions(state),
        possibleStartPositions);

    // List<Integer> t = stateExplorer.convertFromPosToIndex(
    // stateExplorer.getPossibleStartPositions(state));
    // System.out.print("testRedCannonCanNotCapture:");
    // System.out.println(stateExplorer.getPossibleStartPositions(state));
    // System.out.println(t);
  }

  @Test
  public void testRedCannonCanCapture() {
    Map<String, Object> gameApiState = getEmptyBoardState();
    gameApiState.put("C0", "rcan");
    gameApiState.put("C1", "bsol");
    gameApiState.put("C2", "bcan");
    gameApiState.put("C3", null);
    gameApiState.put("C4", null);
    gameApiState.put("C5", null);
    gameApiState.put("C6", null);
    gameApiState.put("C7", null);
    gameApiState.put("C8", null);
    gameApiState.put("C10", null);
    gameApiState.put("C16", null);
    gameApiState.put("C24", null);
    gameApiState.put("O", ImmutableList.of());

    BanqiState state = banqiLogic.gameApiStateToBanqiState(gameApiState,
        turnOfColor, playerIds);

    Set<Position> possibleStartPositions = new HashSet<Position>();
    possibleStartPositions.add(new Position(1, 1));
    possibleStartPositions.add(new Position(1, 4));
    possibleStartPositions.add(new Position(1, 5));
    possibleStartPositions.add(new Position(1, 6));
    possibleStartPositions.add(new Position(1, 7));
    possibleStartPositions.add(new Position(1, 8));
    possibleStartPositions.add(new Position(2, 1));
    possibleStartPositions.add(new Position(2, 3));
    possibleStartPositions.add(new Position(3, 1));
    possibleStartPositions.add(new Position(4, 1));
    assertEquals(stateExplorer.getPossibleStartPositions(state),
        possibleStartPositions);

    // List<Integer> t = stateExplorer.convertFromPosToIndex(
    // stateExplorer.getPossibleStartPositions(state));
    // System.out.print("testRedCannonCanCapture:");
    // System.out.println(stateExplorer.getPossibleStartPositions(state));
    // System.out.println(t);
  }

  /*
   * 
   * Test cannon capture.
   * 
   * {C0 |C1 |C2 |C3 |C4 |C5 |C6 |C7 }
   * {C8 |C9 |C10|C11|   |   |   |   }
   * {C16|C17|C18|   |   |   |   |   }
   * {C24|   |   |   |   |   |   |   }
   */

  @Test
  public void newTest() {
    Map<String, Object> gameApiState = getEmptyBoardState();
    gameApiState.put("C0", "rgen");
    gameApiState.put("C1", "radv");
    gameApiState.put("C2", null);
    gameApiState.put("C7", "rcha");
    gameApiState.put("C8", "rcha");
    gameApiState.put("C9", "rele");
    gameApiState.put("C10", null);
    gameApiState.put("C16", null);
    gameApiState.put("C17", null);
    gameApiState.put("C18", null);
    gameApiState.put("C16", null);
    gameApiState.put("C24", null);
    gameApiState.put("O", ImmutableList.of());

    BanqiState state = banqiLogic.gameApiStateToBanqiState(gameApiState,
        turnOfColor, playerIds);

    Set<Position> possibleStartPositions = new HashSet<Position>();
    possibleStartPositions.add(new Position(1, 8));
    possibleStartPositions.add(new Position(1, 3));
    possibleStartPositions.add(new Position(2, 3));
    possibleStartPositions.add(new Position(3, 1));
    possibleStartPositions.add(new Position(3, 2));
    possibleStartPositions.add(new Position(3, 3));
    possibleStartPositions.add(new Position(4, 1));
    assertEquals(stateExplorer.getPossibleStartPositions(state),
        possibleStartPositions);

    // List<Integer> t = stateExplorer.convertFromPosToIndex(
    // stateExplorer.getPossibleStartPositions(state));
    // System.out.print("test:");
    // System.out.println(stateExplorer.getPossibleStartPositions(state));
    // System.out.println(t);
  }
}
