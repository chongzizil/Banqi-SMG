package org.banqi.client;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

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
    assertEquals(verifyMove.getLastMovePlayerId(), verifyDone.getHackerPlayerId());
  }
  
  private static final String PLAYER_ID = "playerId";
  /* The entries used in the cheat game are:
   *   movePiece, turnPiece, capturePiece, P0...P31, S0...P31 
   * When we send operations on these keys, it will always be in the above order.
   */
  //A move has the form: [fromCoordinate, toCoordinate]
  private static final String MOVEPIECE = "movePiece";
  //A turnOver has the form: [Coordinate]
  private static final String TURNPIECE = "turnPiece";
  //A capture has the form: [fromCoordinate, toCoordinate]
  private static final String CAPTUREPIECE = "capturePiece";
  private final String rId = "7";
  private final String bId = "47";
  private final Map<String, Object> rInfo =
      ImmutableMap.<String, Object>of(PLAYER_ID, rId);
  private final Map<String, Object> bInfo =
      ImmutableMap.<String, Object>of(PLAYER_ID, bId);
  private final List<Map<String, Object>> playersInfo =
      ImmutableList.of(rInfo, bInfo);
  private final Map<String, Object> emptyState = ImmutableMap.<String, Object>of();
  private final Map<String, Object> nonEmptyState =
      ImmutableMap.<String, Object>of("k", "v");
  
  private VerifyMove move(
      String lastMovePlayerId, Map<String, Object> lastState, List<Operation> lastMove) {
    return new VerifyMove(playersInfo, emptyState,
        lastState, lastMove, lastMovePlayerId, ImmutableMap.<String, Integer>of());
  }
  
  @Test
  public void testInitialMove() {
    assertMoveOk(move(rId, emptyState, banqiLogic.getMoveInitial(ImmutableList.of(rId, bId))));
  }
  
  @Test
  public void testInitialMoveByWrongPlayer() {
    assertHacker(move(bId, emptyState, banqiLogic.getMoveInitial(ImmutableList.of(rId, bId))));
  }
  
  @Test
  public void testInitialMoveFromNonEmptyState() {
    assertHacker(move(rId, nonEmptyState, banqiLogic.getMoveInitial(ImmutableList.of(rId, bId))));
  }
  
  @Test
  public void testInitialMoveWithExtraOperation() {
    List<Operation> initialOperations = banqiLogic.getMoveInitial(ImmutableList.of(rId, bId));
    initialOperations.add(new Set(TURNPIECE, ImmutableList.of("S0")));
    assertHacker(move(rId, emptyState, initialOperations));
  }
  
  /*
   * 
   * Test turnPiece.
   * P31 on the board mainly to prevent from ending the game.
   * 
   * {P0  |    |    |    |    |    |    |    }
   * 
   * {    |    |    |    |    |    |    |    }
   * 
   * {    |    |    |    |    |    |    |    }
   * 
   * {    |    |    |    |    |    |    |P31 } 
   *                                     
   * 
   **/
  
  @Test
  public void testRedTurnOverAPiece() {
    Map<String, Object> state = new HashMap<String, Object>();
    state.put("P0", null);
    state.put("P31", "bsol");
    state.put("S0", "P0");
    state.put("S31", "P31");
  
    //The order of operations: turn, movePiece, turnPiece, capturePiece, P0...P31, S0...P31 
    List<Operation> operations = ImmutableList.<Operation>of(
      new SetTurn(bId),
      new Set(TURNPIECE, "S0"),
      new SetVisibility("P0"));
    
    assertMoveOk(move(rId, state, operations));
  }

  @Test
  public void testWrongPlayerTurnOverAPiece() {
    Map<String, Object> state = new HashMap<String, Object>();
    state.put("P0", null);
    state.put("P31", "bsol");
    state.put("S0", "P0");
    state.put("S31", "P31");
  
    //The order of operations: turn, movePiece, turnPiece, capturePiece, P0...P31, S0...P31 
    List<Operation> operations = ImmutableList.<Operation>of(
      new SetTurn(bId),
      new Set(TURNPIECE, "S0"),
      new SetVisibility("P0"));

    assertHacker(move(bId, state, operations));
  }
  
  /*
   * Can not turn up piece P0 because it's already facing up.
   */
  @Test
  public void testRedTurnOverAFaceUpPiece() {
    Map<String, Object> state = ImmutableMap.<String, Object>builder()
        .put("P0",  "rgen")
        .put("P31", "bsol")
        .put("S0", "P0")
        .put("S31", "P31")
        .build();
        
    //The order of operations: turn, movePiece, turnPiece, capturePiece, P0...P31, S0...P31 
    List<Operation> operations = ImmutableList.<Operation>of(
      new SetTurn(bId),
      new Set(TURNPIECE, "S0"),
      new SetVisibility("P0"));
    
    assertHacker(move(rId, state, operations));
  }
  
  /*
   * can not turn because there's no piece on the square.
   */
  @Test
  public void testNoPieceForRedToTurnOver() {
    Map<String, Object> state = new HashMap<String, Object>();
    state.put("P30", "rsol");
    state.put("P31", "bsol");
    state.put("S0", null);
    state.put("S30", "P30");
    state.put("S31", "P31");
   
    
    List<Operation> operations = ImmutableList.<Operation>of(
      new SetTurn(bId),
      new Set(TURNPIECE, "S0"),
      new SetVisibility(null));
    
    assertHacker(move(rId, state, operations));
  }

  /*
   * 
   * Test movePiece.
   * 
   * {P1 |   |   |   |   |   |   |   }
   * {   |P0 |   |   |   |   |   |   }
   * {   |   |   |   |   |   |   |   }
   * {   |   |   |   |   |   |   |P31} 
   * 
   * 
   **/
  
  @Test
  public void testRedMoveP0() {
    Map<String, Object> state = new HashMap<String, Object>();
    state.put("S1", null);
    state.put("S17", null);
    state.put("S8", null);
    state.put("S10", null);
    state.put("S9", "P0");
    state.put("S31", "P31");
    state.put("P0",  "rgen");
    state.put("P31", "bsol");

    List<Operation> operationsUp = ImmutableList.<Operation>of(
      new SetTurn(bId),
      new Set(MOVEPIECE, ImmutableList.of("S9", "S1")),
      new Set("S9", null),
      new Set("S1", "P0"));
    
    List<Operation> operationsDown = ImmutableList.<Operation>of(
      new SetTurn(bId),
      new Set(MOVEPIECE, ImmutableList.of("S9", "S17")),
      new Set("S9", null),
      new Set("S17", "P0"));
    
    List<Operation> operationsLeft = ImmutableList.<Operation>of(
      new SetTurn(bId),
      new Set(MOVEPIECE, ImmutableList.of("S9", "S8")),
      new Set("S9", null),
      new Set("S8", "P0"));
    
    List<Operation> operationsRight = ImmutableList.<Operation>of(
      new SetTurn(bId),
      new Set(MOVEPIECE, ImmutableList.of("S9", "S10")),
      new Set("S9", null),
      new Set("S10", "P0"));

    assertMoveOk(move(rId, state, operationsUp));
    assertMoveOk(move(rId, state, operationsDown));
    assertMoveOk(move(rId, state, operationsLeft));
    assertMoveOk(move(rId, state, operationsRight));
  }
  
  @Test
  public void testWrongPlayerMove() {
    Map<String, Object> state = new HashMap<String, Object>();
    state.put("S1", null);
    state.put("S9", "P0");
    state.put("S31", "P31");
    state.put("P0",  "rgen");
    state.put("P31", "bsol");

    List<Operation> operations = ImmutableList.<Operation>of(
      new SetTurn(bId),
      new Set(MOVEPIECE, ImmutableList.of("S9", "S1")),
      new Set("S9", null),
      new Set("S1", "P0"));

    assertHacker(move(bId, state, operations));
  }
  
  /*
   * Can not move two square in one turn
   */
  @Test
  public void testIllegalRedMoveP1() {
    Map<String, Object> state = new HashMap<String, Object>();
    state.put("S0", "P1");
    state.put("S31", "P31");
    state.put("P1",  "rgen");
    state.put("P31", "bsol");

    List<Operation> operations = ImmutableList.<Operation>of(
      new SetTurn(bId),
      new Set(MOVEPIECE, ImmutableList.of("S0", "S2")),
      new Set("S0", null),
      new Set("S2", "P1"));

    assertHacker(move(rId, state, operations));
  }

  /*
   * 
   * Test movePiece.
   * 
   * {   |P1 |   |   |   |   |   |   }
   * {P3 |P0 |   |   |   |   |   |   }
   * {   |P2 |   |   |   |   |   |   }
   * {   |   |   |   |   |   |   |P31} 
   * 
   * 
   **/
  
  /*
   * Can not move because the square is already occupied by a same color face-up piece.
   */
  @Test
  public void testIllegalRedMoveToAFaceUpPiece() {
    Map<String, Object> state = ImmutableMap.<String, Object>builder()
      
      .put("S9",  "P0")
      .put("S1",  "P1")
      .put("S31", "P31")
      .put("P0",  "rgen")
      .put("P1",  "radv")
      .put("P31", "bsol")
      .build();

    List<Operation> operations = ImmutableList.<Operation>of(
      new SetTurn(bId),
      new Set(MOVEPIECE, ImmutableList.of("S9", "S1")),
      new Set("S9", null),
      new Set("S1", "P0"));

    assertHacker(move(rId, state, operations));
  }
  
  /*
   * Can not move because the square is already occupied by a face-down piece.
   */
  @Test
  public void testIllegalRedMoveToAFaceDownPiece() {
    Map<String, Object> state = new HashMap<String, Object>();
    state.put("S9",  "P0");
    state.put("S8",  "P3");
    state.put("S31", "P31");
    state.put("P0",  "rgen");
    state.put("P3",  null);
    state.put("P31", "bsol");

    List<Operation> operations = ImmutableList.<Operation>of(
      new SetTurn(bId),
      new Set(MOVEPIECE, ImmutableList.of("S9", "S8")),
      new Set("S9", null),
      new Set("S8", "P0"));

    assertHacker(move(rId, state, operations));
  }
  
  /*
   * 
   * Test capture.
   * 
   * {   |P1 |   |   |   |   |   |   } 7
   * {P3 |P0 |P4 |   |   |   |   |   } 15
   * {   |P2 |   |   |   |   |   |   } 23
   * {   |   |   |   |   |   |   |P31} 31
   * 
   * 
   **/
  
  @Test
  public void testRedGeneralCaptureAdvisor() {
    Map<String, Object> state = ImmutableMap.<String, Object>builder()
        
        .put("S9",  "P0")
        .put("S1",  "P1")
        .put("S31", "P31")
        .put("P0",  "rgen")
        .put("P1",  "badv")
        .put("P31", "bsol")
        .build();

    List<Operation> operations = ImmutableList.<Operation>of(
      new SetTurn(bId),
      new Set(CAPTUREPIECE, ImmutableList.of("S9", "S1")),
      new Set("S9", null),
      new Set("S1", "P0"));

    assertMoveOk(move(rId, state, operations));
  }
  
  @Test
  public void testRedGeneralCaptureChariot() {
    Map<String, Object> state = ImmutableMap.<String, Object>builder()
        
        .put("S9",  "P0")
        .put("S8",  "P3")
        .put("S31", "P31")
        .put("P0",  "rgen")
        .put("P3",  "bcha")
        .put("P31", "bsol")
        .build();

    List<Operation> operations = ImmutableList.<Operation>of(
      new SetTurn(bId),
      new Set(CAPTUREPIECE, ImmutableList.of("S9", "S8")),
      new Set("S9", null),
      new Set("S8", "P0"));

    assertMoveOk(move(rId, state, operations));
  }
  
  @Test
  public void testRedGeneralCaptureCannon() {
    Map<String, Object> state = ImmutableMap.<String, Object>builder()
        
        .put("S9",  "P0")
        .put("S17",  "P2")
        .put("S31", "P31")
        .put("P0",  "rgen")
        .put("P2",  "bcan")
        .put("P31", "bsol")
        .build();

    List<Operation> operations = ImmutableList.<Operation>of(
      new SetTurn(bId),
      new Set(CAPTUREPIECE, ImmutableList.of("S9", "S17")),
      new Set("S9", null),
      new Set("S17", "P0"));

    assertMoveOk(move(rId, state, operations));
  }
  
  @Test
  public void testRedGeneralCaptureGeneral() {
    Map<String, Object> state = ImmutableMap.<String, Object>builder()
        
        .put("S9",  "P0")
        .put("S10", "P4")
        .put("S31", "P31")
        .put("P0",  "rgen")
        .put("P4",  "bgen")
        .put("P31", "bsol")
        .build();

    List<Operation> operations = ImmutableList.<Operation>of(
      new SetTurn(bId),
      new Set(CAPTUREPIECE, ImmutableList.of("S9", "S10")),
      new Set("S9", null),
      new Set("S10", "P0"));

    assertMoveOk(move(rId, state, operations));
  }
  
  /*
   * Can not capture a piece of the same color as the player's.
   */
  @Test
  public void testIllegalRedGeneralCaptureRedPiece() {
    Map<String, Object> state = ImmutableMap.<String, Object>builder()
        
        .put("S9",  "P0")
        .put("S1",  "P1")
        .put("S31", "P31")
        .put("P0",  "rgen")
        .put("P1",  "radv")
        .put("P31", "bsol")
        .build();

    List<Operation> operations = ImmutableList.<Operation>of(
      new SetTurn(bId),
      new Set(CAPTUREPIECE, ImmutableList.of("S9", "S1")),
      new Set("S9", null),
      new Set("S1", "P0"));

    assertHacker(move(bId, state, operations));
  }
  
  @Test
  public void testWrongPlayerCaptureAdvisor() {
    Map<String, Object> state = ImmutableMap.<String, Object>builder()
        
        .put("S9",  "P0")
        .put("S1",  "P1")
        .put("S31", "P31")
        .put("P0",  "rgen")
        .put("P1",  "badv")
        .put("P31", "bsol")
        .build();

    List<Operation> operations = ImmutableList.<Operation>of(
      new SetTurn(bId),
      new Set(CAPTUREPIECE, ImmutableList.of("S9", "S1")),
      new Set("S9", null),
      new Set("S1", "P0"));

    assertHacker(move(bId, state, operations));
  }

  @Test
  public void testRedSoldierCaptureGeneral() {
    Map<String, Object> state = ImmutableMap.<String, Object>builder()
        
        .put("S9",  "P0")
        .put("S1",  "P1")
        .put("S31", "P31")
        .put("P0",  "rsol")
        .put("P1",  "bgen")
        .put("P31", "bsol")
        .build();

    List<Operation> operations = ImmutableList.<Operation>of(
      new SetTurn(bId),
      new Set(CAPTUREPIECE, ImmutableList.of("S9", "S1")),
      new Set("S9", null),
      new Set("S1", "P0"));

    assertMoveOk(move(rId, state, operations));
  }
  
  /*
   * Can not capture because the rank of the advisor is lower than the general.
   */
  @Test
  public void testIllegalRedAdvisorCaptureGeneral() {
    Map<String, Object> state = ImmutableMap.<String, Object>builder()
        
        .put("S9",  "P0")
        .put("S1",  "P1")
        .put("S31", "P31")
        .put("P0",  "radv")
        .put("P1",  "bgen")
        .put("P31", "bsol")
        .build();

    List<Operation> operations = ImmutableList.<Operation>of(
      new SetTurn(bId),
      new Set(CAPTUREPIECE, ImmutableList.of("S9", "S1")),
      new Set("S9", null),
      new Set("S1", "P0"));

    assertHacker(move(rId, state, operations));
  }
  
  /*
   * Can not capture because the rank of the chariot is lower than the advisor.
   */
  @Test
  public void testIllegalRedChariotCaptureAdvisor() {
    Map<String, Object> state = ImmutableMap.<String, Object>builder()
        
        .put("S9",  "P0")
        .put("S1",  "P1")
        .put("S31", "P31")
        .put("P0",  "rcha")
        .put("P1",  "badv")
        .put("P31", "bsol")
        .build();

    List<Operation> operations = ImmutableList.<Operation>of(
      new SetTurn(bId),
      new Set(CAPTUREPIECE, ImmutableList.of("S9", "S1")),
      new Set("S9", null),
      new Set("S1", "P0"));

    assertHacker(move(rId, state, operations));
  }
  
  /*
   * Can not capture because the rank of the horse is lower than the chariot.
   */
  @Test
  public void testIllegalRedHorseCaptureChariot() {
    Map<String, Object> state = ImmutableMap.<String, Object>builder()
        
        .put("S9",  "P0")
        .put("S1",  "P1")
        .put("S31", "P31")
        .put("P0",  "rhor")
        .put("P1",  "bcha")
        .put("P31", "bsol")
        .build();

    List<Operation> operations = ImmutableList.<Operation>of(
      new SetTurn(bId),
      new Set(CAPTUREPIECE, ImmutableList.of("S9", "S1")),
      new Set("S9", null),
      new Set("S1", "P0"));

    assertHacker(move(rId, state, operations));
  }
  
  /*
   * Can not capture because the rank of the soldier is lower than the cannon.
   */
  @Test
  public void testIllegalRedSoldierCaptureCannon() {
    Map<String, Object> state = ImmutableMap.<String, Object>builder()
        
        .put("S9",  "P0")
        .put("S1",  "P1")
        .put("S31", "P31")
        .put("P0",  "rsol")
        .put("P1",  "bcan")
        .put("P31", "bsol")
        .build();

    List<Operation> operations = ImmutableList.<Operation>of(
      new SetTurn(bId),
      new Set(CAPTUREPIECE, ImmutableList.of("S9", "S1")),
      new Set("S9", null),
      new Set("S1", "P0"));

    assertHacker(move(rId, state, operations));
  }
  
  /*
   * 
   * Test cannon capture.
   * 
   * {P0 |P1 |P2 |P3 |   |   |   |   }
   * {P4 |   |   |   |   |   |   |   }
   * {   |   |   |   |   |   |   |   }
   * {P5 |   |   |   |   |   |   |P31} 
   * 
   * 
   **/
  
  @Test
  public void testRedCannonCaptureGeneralInSameRow() {
    Map<String, Object> state = ImmutableMap.<String, Object>builder()
        
        .put("S0",  "P0")
        .put("S1",  "P1")
        .put("S2",  "P2")
        .put("S31", "P31")
        .put("P0",  "rcan")
        .put("P1",  "bcan")
        .put("P2",  "bgen")
        .put("P31", "bsol")
        .build();

    List<Operation> operations = ImmutableList.<Operation>of(
      new SetTurn(bId),
      new Set(CAPTUREPIECE, ImmutableList.of("S0", "S2")),
      new Set("S0", null),
      new Set("S2", "P0"));

    assertMoveOk(move(rId, state, operations));
  }
  
  @Test
  public void testRedCannonCaptureGeneralInSameColumn() {
    Map<String, Object> state = ImmutableMap.<String, Object>builder()
        
        .put("S0",  "P0")
        .put("S8",  "P4")
        .put("S24", "P5")
        .put("S31", "P31")
        .put("P0",  "rcan")
        .put("P4",  "bcan")
        .put("P5",  "bgen")
        .put("P31", "bsol")
        .build();

    List<Operation> operations = ImmutableList.<Operation>of(
      new SetTurn(bId),
      new Set(CAPTUREPIECE, ImmutableList.of("S0", "S24")),
      new Set("S0", null),
      new Set("S24", "P0"));

    assertMoveOk(move(rId, state, operations));
  }
  
  /*
   * Can not capture because there needs exactly 1 piece in between.
   */
  @Test
  public void testIllegalRedCannonCaptureWithNoPieceBetween() {
    Map<String, Object> state = ImmutableMap.<String, Object>builder()
        
        .put("S0",  "P0")
        .put("S1",  "P1")
        .put("S31", "P31")
        .put("P0",  "rcan")
        .put("P1",  "bcan")
        .put("P31", "bsol")
        .build();

    List<Operation> operations = ImmutableList.<Operation>of(
      new SetTurn(bId),
      new Set(CAPTUREPIECE, ImmutableList.of("S0", "S1")),
      new Set("S0", null),
      new Set("S1", "P0"));

    assertHacker(move(rId, state, operations));
  }
  
  /*
   * Can not capture because there needs exactly 1 piece in between.
   */
  @Test
  public void testIllegalRedCannonCaptureWithTooMuchPieceBetween() {
    Map<String, Object> state = ImmutableMap.<String, Object>builder()
        
        .put("S0",  "P0")
        .put("S1",  "P1")
        .put("S2",  "P5")
        .put("S3",  "P3")
        .put("S31", "P31")
        .put("P0",  "rcan")
        .put("P1",  "bcan")
        .put("P2",  "bsol")
        .put("P3",  "badv")
        .put("P31", "bsol")
        .build();

    List<Operation> operations = ImmutableList.<Operation>of(
      new SetTurn(bId),
      new Set(CAPTUREPIECE, ImmutableList.of("S0", "S3")),
      new Set("S0", null),
      new Set("S3", "P0"));

    assertHacker(move(rId, state, operations));
  }
  
  /*
   * If the opponent turn up the last facing-down piece on the board or capture one that
   * all the pieces left on the board have the same color, the game is then ended.
   * 
   */
  @Test
  public void testEndGame() {
    Map<String, Object> state = ImmutableMap.<String, Object>builder()
        .put("S0",  "P0")
        .put("S1",  "P1")
        .put("P0",  "rgen")
        .put("P1",  "rcan")
        .build();

    List<Operation> operations = ImmutableList.<Operation>of(
      new SetTurn(rId),
      new EndGame(rId));


    assertMoveOk(move(rId, state, operations));
  }
  
  /*
   * If all the pieces left on the board do not have the same color,
   * then the game is not end yet and hence any end game operation
   * is invalid.
   */
  @Test
  public void testIllegalEndGame() {
    Map<String, Object> state = ImmutableMap.<String, Object>builder()
        .put("S0",  "P0")
        .put("S1",  "P1")
        .put("S2",  "P2")
        .put("P0",  "rgen")
        .put("P1",  "rcan")
        .put("P2",  "bcan")
        .build();

    List<Operation> operations = ImmutableList.<Operation>of(
      new SetTurn(rId),
      new EndGame(rId));


    assertHacker(move(rId, state, operations));
  }
  
  /* 
   * The end game operation is made when all the pieces in the current state
   * have the same color, therefore previous test is inappropriate and discarded.
   **/
}
