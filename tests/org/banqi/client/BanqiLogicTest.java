package org.banqi.client;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.game_api.GameApi.Operation;
import org.game_api.GameApi.Set;
import org.game_api.GameApi.SetVisibility;
import org.game_api.GameApi.VerifyMove;
import org.game_api.GameApi.VerifyMoveDone;
import org.game_api.GameApi.EndGame;
import org.game_api.GameApi.SetTurn;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class BanqiLogicTest {

  /** The object under test. */
  BanqiLogic banqiLogic = new BanqiLogic();

  private void assertMoveOk(VerifyMove verifyMove) {
    banqiLogic.checkMoveIsLegal(verifyMove);
  }

  private void assertHacker(VerifyMove verifyMove) {
    VerifyMoveDone verifyDone = banqiLogic.verify(verifyMove);
    assertEquals(verifyMove.getLastMovePlayerId(),
        verifyDone.getHackerPlayerId());
  }

  /*
   * The entries used in the banqi game are: turnPiece, movePiece, capturePiece,
   * C0...C31
   */
  private static final String C = "C"; // Cell i
  // A move has the form: [from, to]
  private static final String MOVEPIECE = "movePiece";
  // A turn has the form: [coordinate]
  private static final String TURNPIECE = "turnPiece";
  // A capture has the form: [from, to]
  private static final String CAPTUREPIECE = "capturePiece";
  
  private static final String PLAYER_ID = "playerId";
  private final String rId = "7";
  private final String bId = "47";
  private final Map<String, Object> rInfo = ImmutableMap
      .<String, Object> of(PLAYER_ID, rId);
  private final Map<String, Object> bInfo = ImmutableMap
      .<String, Object> of(PLAYER_ID, bId);
  private final List<Map<String, Object>> playersInfo = ImmutableList
      .of(rInfo, bInfo);
  
  /* The interesting states that I'll test. */
  private final ImmutableMap<String, Object> emptyState = ImmutableMap
      .<String, Object> of();
  private final Map<String, Object> nonEmptyState = ImmutableMap
      .<String, Object> of("k", "v");

  public static final Map<String, Object> getEmptyBoardState() {
    Map<String, Object> emptyBoardState = new HashMap<String, Object>();
    for (int i = 0; i < 32; i++) {
      String cell = C + i;
      String emptyCell = "eemp";
      emptyBoardState.put(cell, emptyCell);
    }
    emptyBoardState.put("O", ImmutableList.of());
    return emptyBoardState;
  }
  
  private VerifyMove move(String lastMovePlayerId,
      Map<String, Object> lastState, List<Operation> lastMove) {
    return new VerifyMove(playersInfo, emptyState, lastState, lastMove,
        lastMovePlayerId, ImmutableMap.<String, Integer> of());
  }

  @Test
  public void testInitialMove() {
    assertMoveOk(move(rId, emptyState,
        banqiLogic.getMoveInitial(ImmutableList.of(rId, bId))));
  }

  @Test
  public void testInitialMoveByWrongPlayer() {
    assertHacker(move(bId, emptyState,
        banqiLogic.getMoveInitial(ImmutableList.of(rId, bId))));
  }

  @Test
  public void testInitialMoveFromNonEmptyState() {
    assertHacker(move(rId, nonEmptyState,
        banqiLogic.getMoveInitial(ImmutableList.of(rId, bId))));
  }

  // @Test
  // public void testInitialMoveWithExtraOperation() {
  // List<Operation> initialOperations =
  // banqiLogic.getMoveInitial(ImmutableList.of(rId, bId));
  // initialOperations.add(new Set(TURNPIECE, ImmutableList.of("S0")));
  // assertHacker(move(rId, emptyState, initialOperations));
  // }

  /*
   * Test turnPiece: Turn C0's piece up.
   * The piece in C31 and C30 on the board mainly prevent the game from ending.
   * 
   * {C0 |   |   |   |   |   |   |   }
   * null
   * {   |   |   |   |   |   |   |   }
   * 
   * {   |   |   |   |   |   |   |   }
   * 
   * {   |   |   |   |   |   |C30|C31}
   *                         rsol|bsol
   */

  public Map<String, Object> getTestTurnState() {
    Map<String, Object> state = getEmptyBoardState();
    state.put("C0", null);
    state.put("C30", "rsol");
    state.put("C31", "bsol");
    state.put("O", ImmutableList.of());
    return state;
  }
  
  @Test
  public void testRedTurnPiece() {
    Map<String, Object> state = getTestTurnState();

    List<Operation> operations = ImmutableList.<Operation> of(
        new SetTurn(bId),
        new Set(TURNPIECE, "C0"),
        new SetVisibility("C0"));

    assertMoveOk(move(rId, state, operations));
  }

  @Test
  public void testWrongPlayerTurnPiece() {
    Map<String, Object> state = getTestTurnState();

    List<Operation> operations = ImmutableList.<Operation> of(
        new SetTurn(bId),
        new Set(TURNPIECE, "C0"),
        new SetVisibility("C0"));

    assertHacker(move(bId, state, operations));
  }

  @Test
  public void testRedTurnFaceUpPiece() {
    Map<String, Object> state = getTestTurnState();

    List<Operation> operations = ImmutableList.<Operation> of(
        new SetTurn(bId),
        new Set(TURNPIECE, "C30"),
        new SetVisibility("C30"));

    assertHacker(move(rId, state, operations));
  }

  @Test
  public void testRedTurnEmptyCell() {
    Map<String, Object> state = getTestTurnState();

    List<Operation> operations = ImmutableList.<Operation> of(
        new SetTurn(bId),
        new Set(TURNPIECE, "C29"),
        new SetVisibility("C29"));

    assertHacker(move(rId, state, operations));
  }

  /*
   * Test movePiece: move C0 and C31
   * 
   * {   |   |   |   |   |   |   |   }
   * 
   * {   |C9 |   |   |   |   |   |   }
   *     rsol
   * {   |   |   |   |   |   |   |C23}
   *                              bsol
   * {   |   |   |   |   |   |C30|C31}
   *                         null|bsol
   */

  public Map<String, Object> getTestMoveState() {
    Map<String, Object> state = getEmptyBoardState();
    state.put("C9",  "rsol");
    state.put("S23", "bsol");
    state.put("S30", null);
    state.put("S31", "bsol");
    state.put("O", ImmutableList.of());
    return state;
  }
  
  @Test
  public void testLegalRedMoveC0() {
    Map<String, Object> state =  getTestMoveState();

    List<Operation> operationsUp = ImmutableList.<Operation> of(
        new SetTurn(bId),
        new Set(MOVEPIECE, ImmutableList.of("C9", "C1")),
        new Set("C9", "eemp"),
        new Set("C1", "rsol"));
    
    List<Operation> operationsDown = ImmutableList.<Operation> of(
        new SetTurn(bId),
        new Set(MOVEPIECE, ImmutableList.of("C9", "C17")),
        new Set("C9", "eemp"),
        new Set("C17", "rsol"));
    
    List<Operation> operationsLeft = ImmutableList.<Operation> of(
        new SetTurn(bId),
        new Set(MOVEPIECE, ImmutableList.of("C9", "C8")),
        new Set("C9", "eemp"),
        new Set("C8", "rsol"));
    
    List<Operation> operationsRight = ImmutableList.<Operation> of(
        new SetTurn(bId),
        new Set(MOVEPIECE, ImmutableList.of("C9", "C10")),
        new Set("C9", "eemp"),
        new Set("C10", "rsol"));

    assertMoveOk(move(rId, state, operationsUp));
    assertMoveOk(move(rId, state, operationsDown));
    assertMoveOk(move(rId, state, operationsLeft));
    assertMoveOk(move(rId, state, operationsRight));
  }

  @Test
  public void testWrongPlayerMove() {
    Map<String, Object> state = getTestMoveState();

    List<Operation> operations = ImmutableList.<Operation> of(
        new SetTurn(bId),
        new Set(MOVEPIECE, ImmutableList.of("C9", "C1")),
        new Set("C9", "eemp"),
        new Set("C1", "rsol"));

    assertHacker(move(bId, state, operations));
  }

  /*
   * Can not move two square in one turn
   */
  @Test
  public void testIllegalRedMoveTwoCell() {
    Map<String, Object> state = getTestMoveState();

    List<Operation> operations = ImmutableList.<Operation> of(
        new SetTurn(bId),
        new Set(MOVEPIECE, ImmutableList.of("C9", "C11")),
        new Set("C9", "eemp"),
        new Set("C11", "rsol"));

    assertHacker(move(rId, state, operations));
  }
  
  @Test
  public void testIllegalBlackMoveToAllReadyOccupiedCell() {
    Map<String, Object> state = getTestMoveState();

    List<Operation> operationsUp = ImmutableList.<Operation> of(
        new SetTurn(rId),
        new Set(MOVEPIECE, ImmutableList.of("C31", "C23")),
        new Set("C31", "eemp"),
        new Set("C23", "bsol"));
    
    List<Operation> operationsLeft = ImmutableList.<Operation> of(
        new SetTurn(rId),
        new Set(MOVEPIECE, ImmutableList.of("C31", "C30")),
        new Set("C31", "eemp"),
        new Set("C30", "bsol"));

    assertHacker(move(bId, state, operationsUp));
    assertHacker(move(bId, state, operationsLeft));
  }
  
  /*
   * 
   * Test capture.
   * 
   * {   |C1 |   |   |   |   |   |   }
   *     b...
   * {C8 |C9 |C10|   |   |   |   |   }
   * bsol|rgen|null
   * {   |C17|   |   |   |   |   |   }
   *     rsol 
   * {   |   |   |   |   |   |   |   }
   */

  public Map<String, Object> getTestCaptureState() {
    Map<String, Object> state = getEmptyBoardState();
    state.put("C8",   "bsol");
    state.put("C9",   "rgen");
    state.put("C10",   null);
    state.put("C17",  "bgen");
    state.put("O", ImmutableList.of());
    return state;
  }
  
  @Test
  public void testRedGeneralCaptureBlackAdvisor() {
    Map<String, Object> state = getTestCaptureState();
    state.put("C1",   "badv");
    
    List<Operation> operations = ImmutableList.<Operation> of(
        new SetTurn(bId),
        new Set(CAPTUREPIECE, ImmutableList.of("C9", "C1")),
        new Set("C9", "eemp"),
        new Set("C1", "rgen"),
        new Set("O", ImmutableList.of("badv")));

    assertMoveOk(move(rId, state, operations));
  }

  @Test
  public void testRedGeneralCaptureBlackElephant() {
    Map<String, Object> state = getTestCaptureState();
    state.put("C1",   "bele");

    List<Operation> operations = ImmutableList.<Operation> of(
        new SetTurn(bId),
        new Set(CAPTUREPIECE, ImmutableList.of("C9", "C1")),
        new Set("C9", "eemp"),
        new Set("C1", "rgen"),
        new Set("O", ImmutableList.of("bele")));

    assertMoveOk(move(rId, state, operations));
  }
  
  @Test
  public void testRedGeneralCaptureBlackChariot() {
    Map<String, Object> state = getTestCaptureState();
    state.put("C1",   "bcha");

    List<Operation> operations = ImmutableList.<Operation> of(
        new SetTurn(bId),
        new Set(CAPTUREPIECE, ImmutableList.of("C9", "C1")),
        new Set("C9", "eemp"),
        new Set("C1", "rgen"),
        new Set("O", ImmutableList.of("bcha")));

    assertMoveOk(move(rId, state, operations));
  }
  
  @Test
  public void testRedGeneralCaptureBlackHorse() {
    Map<String, Object> state = getTestCaptureState();
    state.put("C1",   "bhor");

    List<Operation> operations = ImmutableList.<Operation> of(
        new SetTurn(bId),
        new Set(CAPTUREPIECE, ImmutableList.of("C9", "C1")),
        new Set("C9", "eemp"),
        new Set("C1", "rgen"),
        new Set("O", ImmutableList.of("bhor")));

    assertMoveOk(move(rId, state, operations));
  }

  @Test
  public void testRedGeneralCaptureBlackCannon() {
    Map<String, Object> state = getTestCaptureState();
    state.put("C1",   "bcan");

    List<Operation> operations = ImmutableList.<Operation> of(
        new SetTurn(bId),
        new Set(CAPTUREPIECE, ImmutableList.of("C9", "C1")),
        new Set("C9", "eemp"),
        new Set("C1", "rgen"),
        new Set("O", ImmutableList.of("bcan")));

    assertMoveOk(move(rId, state, operations));
  }

  @Test
  public void testRedGeneralCaptureBlackGeneral() {
    Map<String, Object> state = getTestCaptureState();
    state.put("C1",   "bgen");

    List<Operation> operations = ImmutableList.<Operation> of(
        new SetTurn(bId),
        new Set(CAPTUREPIECE, ImmutableList.of("C9", "C1")),
        new Set("C9", "eemp"),
        new Set("C1", "rgen"),
        new Set("O", ImmutableList.of("bgen")));

    assertMoveOk(move(rId, state, operations));
  }

  /*
   * Can not capture a piece of the same color as the player's.
   */
  @Test
  public void testIllegalRedGeneralCaptureRedSoldier() {
    Map<String, Object> state = getTestCaptureState();

    List<Operation> operations = ImmutableList.<Operation> of(
        new SetTurn(bId),
        new Set(CAPTUREPIECE, ImmutableList.of("C9", "C17")),
        new Set("C9", "eemp"),
        new Set("C17", "rgen"),
        new Set("O", ImmutableList.of("rsol")));

    assertHacker(move(rId, state, operations));
  }

  @Test
  public void testWrongPlayerCaptureBlackAdvisor() {
    Map<String, Object> state = getTestCaptureState();
    state.put("C1",   "badv");
    
    List<Operation> operations = ImmutableList.<Operation> of(
        new SetTurn(rId),
        new Set(CAPTUREPIECE, ImmutableList.of("C9", "C1")),
        new Set("C9", "eemp"),
        new Set("C1", "rgen"));

    assertHacker(move(bId, state, operations));
  }

  @Test
  public void testBlackSoldierCaptureRedGeneral() {
    Map<String, Object> state = getTestCaptureState();
    
    List<Operation> operations = ImmutableList.<Operation> of(
        new SetTurn(rId),
        new Set(CAPTUREPIECE, ImmutableList.of("C8", "C9")),
        new Set("C8", "eemp"),
        new Set("C9", "bsol"),
        new Set("O", ImmutableList.of("rgen")));

    assertMoveOk(move(bId, state, operations));
  }

  /*
   * Can not capture because the rank of the advisor is lower than the general.
   */
  @Test
  public void testIllegalBlackAdvisorCaptureRedGeneral() {
    Map<String, Object> state = getTestCaptureState();
    state.put("C1",   "badv");
    
    List<Operation> operations = ImmutableList.<Operation> of(
        new SetTurn(bId),
        new Set(CAPTUREPIECE, ImmutableList.of("C1", "C9")),
        new Set("C1", "eemp"),
        new Set("C9", "badv"));

    assertHacker(move(bId, state, operations));
  }

  /*
   * Can not capture because the rank of the chariot is lower than the advisor.
   */
  @Test
  public void testIllegalRedChariotCaptureBlackAdvisor() {
    Map<String, Object> state = getTestCaptureState();
    state.put("C1",   "badv");
    state.put("C9",   "rcha");
    
    List<Operation> operations = ImmutableList.<Operation> of(
        new SetTurn(bId),
        new Set(CAPTUREPIECE, ImmutableList.of("C1", "C9")),
        new Set("C1", "rcha"),
        new Set("C9", "eemp"));

    assertHacker(move(bId, state, operations));
  }

  /*
   * Can not capture because the rank of the horse is lower than the chariot.
   */
  @Test
  public void testIllegalRedHorseCaptureBlackChariot() {
    Map<String, Object> state = getTestCaptureState();
    state.put("C1",   "bcha");
    state.put("C9",   "rhor");
    
    List<Operation> operations = ImmutableList.<Operation> of(
        new SetTurn(bId),
        new Set(CAPTUREPIECE, ImmutableList.of("C1", "C9")),
        new Set("C1", "rhor"),
        new Set("C9", "eemp"));

    assertHacker(move(bId, state, operations));
  }

  /*
   * Can not capture because the rank of the soldier is lower than the cannon.
   */
  @Test
  public void testIllegalRedSoldierCaptureBlackCannon() {
    Map<String, Object> state = getTestCaptureState();
    state.put("C1",   "bcan");
    state.put("C9",   "rsol");
    
    List<Operation> operations = ImmutableList.<Operation> of(
        new SetTurn(bId),
        new Set(CAPTUREPIECE, ImmutableList.of("C1", "C9")),
        new Set("C1", "rsol"),
        new Set("C9", "eemp"));

    assertHacker(move(bId, state, operations));
  }

  /*
   * 
   * Test cannon capture.
   * 
   * {C0 |C1 |C2 |C3 |   |   |   |   }
   * rcan
   * {C8 |   |   |   |   |   |   |   }
   * 
   * {   |   |   |   |   |   |   |   }
   * 
   * {C24|   |   |   |   |   |   |C31}
   * 
   */

  public Map<String, Object> getTestCannonCaptureState() {
    Map<String, Object> state = getEmptyBoardState();
    state.put("C0",   "rcan");
    state.put("C1",   "bsol");
    state.put("C2",   "bsol");
    state.put("C3",   "bsol");
    state.put("C8",   null);
    state.put("C24",  "bsol");
    state.put("O", ImmutableList.of());
    return state;
  }
  
  @Test
  public void testRedCannonCaptureBlackSoldierInSameRow() {
    Map<String, Object> state = getTestCannonCaptureState();

    List<Operation> operations = ImmutableList.<Operation> of(
        new SetTurn(bId),
        new Set(CAPTUREPIECE, ImmutableList.of("C0", "C2")),
        new Set("C0", "eemp"),
        new Set("C2", "rcan"),
        new Set("O", ImmutableList.of("bsol")));

    assertMoveOk(move(rId, state, operations));
  }

  @Test
  public void testRedCannonCaptureBlackSoldierInSameColumn() {
    Map<String, Object> state = getTestCannonCaptureState();

    List<Operation> operations = ImmutableList.<Operation> of(
        new SetTurn(bId),
        new Set(CAPTUREPIECE, ImmutableList.of("C0", "C24")),
        new Set("C0", "eemp"),
        new Set("C24", "rcan"),
        new Set("O", ImmutableList.of("bsol")));

    assertMoveOk(move(rId, state, operations));
  }

  /*
   * Can not capture because there needs exactly 1 piece in between.
   */
  @Test
  public void testIllegalRedCannonCaptureWith0PieceBetween() {
    Map<String, Object> state = getTestCannonCaptureState();

    List<Operation> operations = ImmutableList.<Operation> of(
        new SetTurn(bId),
        new Set(CAPTUREPIECE, ImmutableList.of("C0", "C1")),
        new Set("C0", "eemp"),
        new Set("C1", "rcan"));

    assertHacker(move(rId, state, operations));
  }

  /*
   * Can not capture because there needs exactly 1 piece in between.
   */
  @Test
  public void testIllegalRedCannonCaptureWithTooMuchPieceBetween() {
    Map<String, Object> state = getTestCannonCaptureState();

    List<Operation> operations = ImmutableList.<Operation> of(
        new SetTurn(bId),
        new Set(CAPTUREPIECE, ImmutableList.of("C0", "C3")),
        new Set("C0", "eemp"),
        new Set("C3", "rcan"));

    assertHacker(move(rId, state, operations));
  }

  /*
   * If the opponent turn up the last facing-down piece on the board or capture
   * one that all the pieces left on the board have the same color, the game is
   * then ended.
   */
  @Test
  public void testEndGame() {
    Map<String, Object> state = getEmptyBoardState();
    state.put("C0",   "rcan");
    state.put("C1",   "rsol");

    List<Operation> operations = ImmutableList.<Operation> of(
        new SetTurn(bId),
        new EndGame(rId));

    assertMoveOk(move(rId, state, operations));
  }

  /*
   * If all the pieces left on the board do not have the same color, then the
   * game is not end yet and hence any end game operation is invalid.
   */
  @Test
  public void testIllegalEndGame() {
    Map<String, Object> state = getEmptyBoardState();
    state.put("C0",   "rcan");
    state.put("C1",   "bsol");

    List<Operation> operations = ImmutableList.<Operation> of(
        new SetTurn(bId),
        new EndGame(rId));

    assertHacker(move(rId, state, operations));
  }
}
