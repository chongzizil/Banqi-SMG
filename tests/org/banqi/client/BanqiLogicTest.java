package org.banqi.client;

import static com.google.common.base.Preconditions.checkArgument;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.banqi.client.GameApi.Shuffle;
import org.banqi.client.GameApi.Operation;
import org.banqi.client.GameApi.Set;
import org.banqi.client.GameApi.SetVisibility;
import org.banqi.client.GameApi.VerifyMove;
import org.banqi.client.GameApi.VerifyMoveDone;
import org.banqi.client.GameApi.EndGame;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


@RunWith(JUnit4.class)
public class BanqiLogicTest {
  
  private void assertMoveOk(VerifyMove verifyMove) {
    VerifyMoveDone verifyDone = new BanqiLogic().verify(verifyMove);
    assertEquals(new VerifyMoveDone(), verifyDone);
  }

  private void assertHacker(VerifyMove verifyMove) {
    VerifyMoveDone verifyDone = new BanqiLogic().verify(verifyMove);
    assertEquals(new VerifyMoveDone(verifyMove.getLastMovePlayerId(), "Hacker found"), verifyDone);
  }
  
  private final int rId = 7;
  private final int bId = 47;
  private static final String PLAYER_ID = "playerId";
  private static final String TURN = "turn"; // turn of which player (either R or B)
  private static final String R = "R"; // Red player
  private static final String B = "B"; // Black player
  private static final String S = "S"; // Board coordinate key (S0 ... S31)
  private static final String P = "P"; // Piece key (P0 ... P31)
  private static final List<Integer> INVISIBLE = new ArrayList<Integer>();
  private static final String MOVEPIECE = "movePiece"; // a move has the form: [from, to]
  private static final String TURNPIECE = "turnPiece"; // a turnOver has the form: [coordinate]
  private static final String CAPTUREPIECE = "capturePiece"; // a capture has the form: [from, to]
  private final Map<String, Object> rInfo = ImmutableMap.<String, Object>of(PLAYER_ID, rId);
  private final Map<String, Object> bInfo = ImmutableMap.<String, Object>of(PLAYER_ID, bId);
  private final List<Map<String, Object>> playersInfo = ImmutableList.of(rInfo, bInfo);
  private final Map<String, Object> emptyState = ImmutableMap.<String, Object>of();
  private final Map<String, Object> nonEmptyState = ImmutableMap.<String, Object>of("k", "v");
  
  private VerifyMove move(
      int lastMovePlayerId, Map<String, Object> lastState, List<Operation> lastMove) {
    return new VerifyMove(rId, playersInfo, emptyState, lastState, lastMove, lastMovePlayerId);
  }
  
  private List<String> getPieces() {
    List<String> keys = Lists.newArrayList();
    for (int i = 0; i < 32; i++) {
      keys.add(P + i);
    }
    return keys;
  }
  
  private String pieceIdToString(int pieceId) {
    checkArgument(pieceId < 32 && pieceId >= 0);
    String pieceColorAndKind = pieceId < 16 ? "R" : "B"; //Red and Black
    pieceId %= 16;
    pieceColorAndKind += pieceId == 0 ? "gen" //GENERAL
      : pieceId < 3 ? "adv" //ADVISOR
      : pieceId < 5 ? "ele" //ELEPHANT
      : pieceId < 7 ? "cha" //CHARIOT
      : pieceId < 11 ? "hor" //HORSE
      : pieceId < 13 ? "can" : "sol"; //CANNON and SOLDIER
    return pieceColorAndKind;
  }
  
  List<Operation> getInitialMove() {
    List<Operation> operations = Lists.newArrayList();
    //The order of operations: turn, movePiece, turnPiece, capturePiece, P0...P31, S0...P31
    operations.add(new Set(TURN, R));
    //Set the pieces
    for (int i = 0; i < 32; i++) {
      operations.add(new Set(P + i, pieceIdToString(i)));
    }  
    //Set the board
    for (int i = 0; i < 32; i++) {
      operations.add(new Set(S + i, P + i));
    }
    //Shuffle the pieces
    operations.add(new Shuffle(getPieces()));
    // Set visibility
    for (int i = 0; i < 32; i++) {
      operations.add(new SetVisibility(P + i, INVISIBLE));
    }
    return operations;
  }
  
  @Test
  public void testInitialMove() {
    assertMoveOk(move(rId, emptyState, getInitialMove()));
  }
  
  @Test
  public void testInitialMoveByWrongPlayer() {
    assertHacker(move(bId, emptyState, getInitialMove()));
  }
  
  @Test
  public void testInitialMoveFromNonEmptyState() {
    assertHacker(move(rId, nonEmptyState, getInitialMove()));
  }
  
  @Test
  public void testInitialMoveWithExtraOperation() {
    List<Operation> initialOperations = getInitialMove();
    initialOperations.add(new Set(TURNPIECE, ImmutableList.of("11")));
    assertHacker(move(rId, emptyState, initialOperations));
  }
  
  /*
   * 
   * Test turnPiece.
   * 
   * {P0 |   |   |   |   |   |   |   }
   * {   |   |   |   |   |   |   |   }
   * {   |   |   |   |   |   |   |   }
   * {   |   |   |   |   |   |   |P31} 
   * 
   * 
   **/
  
  @Test
  public void testRedTurnOverAPiece() {
    Map<String, Object> state = new HashMap<String, Object>();
      state.put(TURN, R);
      state.put(S + 0, P + 0);
      //Need at least one opponent's piece on board, otherwise the game is end.
      state.put(S + 31, P + 31);
      state.put(P + 0, null);
      state.put(P + 31, "bsol");
    
    //The order of operations: turn, movePiece, turnPiece, capturePiece, P0...P31, S0...P31 
    List<Operation> operations = ImmutableList.<Operation>of(
      new Set(TURN, B),
      new Set(TURNPIECE, S + 0),
      new SetVisibility(P + 0));
    
    assertMoveOk(move(rId, state, operations));
  }

  @Test
  public void testWrongPlayerTurnOverAPiece() {
    Map<String, Object> state = new HashMap<String, Object>();
    state.put(TURN, R);
    state.put(S + 0, P + 0);
    //Need at least one opponent's piece on board, otherwise the game is end.
    state.put(S + 31, P + 31);
    state.put(P + 0, null);
    state.put(P + 31, "bsol");
      
    //The order of operations: turn, movePiece, turnPiece, capturePiece, P0...P31, S0...P31 
    List<Operation> operations = ImmutableList.<Operation>of(
      new Set(TURN, B),
      new Set(TURNPIECE, S + 0),
      new SetVisibility(P + 0));

    assertHacker(move(bId, state, operations));
  }
  
  /*
   * Can not turn up a already face-up piece.
   */
  @Test
  public void testRedTurnOverAFaceUpPiece() {
    Map<String, Object> state = ImmutableMap.<String, Object>builder()
      .put(TURN, R)
      .put(S + 0, P + 0)
      //Need at least one opponent's piece on board, otherwise the game is end.
      .put(S + 31, P + 31)
      .put(P + 0,  "rsol")
      .put(P + 31, "bsol")
      .build();
        
      //The order of operations: turn, movePiece, turnPiece, capturePiece, P0...P31, S0...P31 
    List<Operation> operations = ImmutableList.<Operation>of(
      new Set(TURN, B),
      new Set(TURNPIECE, S + 0),
      new SetVisibility(P + 0));
    
    assertHacker(move(rId, state, operations));
  }
  
  /*
   * can not turn because there's no piece on the square.
   */
  @Test
  public void testNoPieceForRedToTurnOver() {
    Map<String, Object> state = new HashMap<String, Object>();
    state.put(TURN, R);
    /* 
     * if the value of a square is null, it indicates there no piece on the square.
     */
   state.put(S + 0, null);
   //Need at least one opponent's piece on board, otherwise the game is end.
   state.put(S + 30, P + 15);
   state.put(S + 31, P + 31);
   state.put(P + 15, "rsol");
   state.put(P + 31, "bsol");
    
    List<Operation> operations = ImmutableList.<Operation>of(
      new Set(TURN, B),
      new Set(TURNPIECE, S + 0),
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
    state.put(TURN, R);
    state.put(S + 1, null);
    state.put(S + 17, null);
    state.put(S + 8, null);
    state.put(S + 10, null);
    state.put(S + 9, P + 0);
    state.put(S + 31, P + 31);
    state.put(P + 0,  "rgen");
    state.put(P + 31, "bsol");

    List<Operation> operationsUp = ImmutableList.<Operation>of(
      new Set(TURN, B),
      new Set(MOVEPIECE, ImmutableList.of(S + 9, S + 1)));
    
    List<Operation> operationsDown = ImmutableList.<Operation>of(
      new Set(TURN, B),
      new Set(MOVEPIECE, ImmutableList.of(S + 9, S + 17)));
    
    List<Operation> operationsLeft = ImmutableList.<Operation>of(
      new Set(TURN, B),
      new Set(MOVEPIECE, ImmutableList.of(S + 9, S + 8)));
    
    List<Operation> operationsRight = ImmutableList.<Operation>of(
      new Set(TURN, B),
      new Set(MOVEPIECE, ImmutableList.of(S + 9, S + 10)));

    assertMoveOk(move(rId, state, operationsUp));
    assertMoveOk(move(rId, state, operationsDown));
    assertMoveOk(move(rId, state, operationsLeft));
    assertMoveOk(move(rId, state, operationsRight));
  }
  
  @Test
  public void testWrongPlayerMove() {
    Map<String, Object> state = new HashMap<String, Object>();
    state.put(TURN, R);
    state.put(S + 1, null);
    state.put(S + 9, P + 0);
    state.put(S + 31, P + 31);
    state.put(P + 0,  "rgen");
    state.put(P + 31, "bsol");

    List<Operation> operations = ImmutableList.<Operation>of(
      new Set(TURN, B),
      new Set(MOVEPIECE, ImmutableList.of(S + 9, S + 1)));

    assertHacker(move(bId, state, operations));
  }
  
  /*
   * Can not move two square in one turn
   */
  @Test
  public void testIllegalRedMoveP1() {
    Map<String, Object> state = new HashMap<String, Object>();
    state.put(TURN, R);
    state.put(S + 0, P + 1);
    state.put(S + 31, P + 31);
    state.put(P + 1,  "rgen");
    state.put(P + 31, "bsol");

    List<Operation> operations = ImmutableList.<Operation>of(
      new Set(TURN, B),
      new Set(MOVEPIECE, ImmutableList.of(S + 0, S + 2)));

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
      .put(TURN, R)
      .put(S + 9,  P + 0)
      .put(S + 1,  P + 1)
      .put(S + 31, P + 31)
      .put(P + 0,  "rgen")
      .put(P + 1,  "radv")
      .put(P + 31, "bsol")
      .build();

    List<Operation> operations = ImmutableList.<Operation>of(
      new Set(TURN, B),
      new Set(MOVEPIECE, ImmutableList.of(S + 9, S + 1)));

    assertHacker(move(rId, state, operations));
  }
  
  /*
   * Can not move because the square is already occupied by a face-down piece.
   */
  @Test
  public void testIllegalRedMoveToAFaceDownPiece() {
    Map<String, Object> state = new HashMap<String, Object>();
    state.put(TURN, R);
    state.put(S + 9,  P + 0);
    state.put(S + 8,  P + 3);
    state.put(S + 31, P + 31);
    state.put(P + 0,  "rgen");
    state.put(P + 3,  null);
    state.put(P + 31, "bsol");

    List<Operation> operations = ImmutableList.<Operation>of(
      new Set(TURN, B),
      new Set(MOVEPIECE, ImmutableList.of(S + 9, S + 8)));

    assertHacker(move(rId, state, operations));
  }
  
  /*
   * 
   * Test capture.
   * 
   * {   |P1 |   |   |   |   |   |   }
   * {P3 |P0 |P4 |   |   |   |   |   }
   * {   |P2 |   |   |   |   |   |   }
   * {   |   |   |   |   |   |   |P31} 
   * 
   * 
   **/
  
  @Test
  public void testRedGeneralCaptureAdvisor() {
    Map<String, Object> state = ImmutableMap.<String, Object>builder()
        .put(TURN, R)
        .put(S + 9,  P + 0)
        .put(S + 1,  P + 1)
        .put(S + 31, P + 31)
        .put(P + 0,  "rgen")
        .put(P + 1,  "badv")
        .put(P + 31, "bsol")
        .build();

    List<Operation> operations = ImmutableList.<Operation>of(
      new Set(TURN, B),
      new Set(CAPTUREPIECE, ImmutableList.of(S + 9, S + 1)),
      new Set(S + 9, null),
      new Set(S + 1, P + 0));

    assertMoveOk(move(rId, state, operations));
  }
  
  @Test
  public void testRedGeneralCaptureChariot() {
    Map<String, Object> state = ImmutableMap.<String, Object>builder()
        .put(TURN, R)
        .put(S + 9,  P + 0)
        .put(S + 8,  P + 3)
        .put(S + 31, P + 31)
        .put(P + 0,  "rgen")
        .put(P + 3,  "bcha")
        .put(P + 31, "bsol")
        .build();

    List<Operation> operations = ImmutableList.<Operation>of(
      new Set(TURN, B),
      new Set(CAPTUREPIECE, ImmutableList.of(S + 9, S + 8)),
      new Set(S + 9, null),
      new Set(S + 8, P + 0));

    assertMoveOk(move(rId, state, operations));
  }
  
  @Test
  public void testRedGeneralCaptureCannon() {
    Map<String, Object> state = ImmutableMap.<String, Object>builder()
        .put(TURN, R)
        .put(S + 9,  P + 0)
        .put(S + 17,  P + 2)
        .put(S + 31, P + 31)
        .put(P + 0,  "rgen")
        .put(P + 2,  "bcan")
        .put(P + 31, "bsol")
        .build();

    List<Operation> operations = ImmutableList.<Operation>of(
      new Set(TURN, B),
      new Set(CAPTUREPIECE, ImmutableList.of(S + 9, S + 17)),
      new Set(S + 9, null),
      new Set(S + 17, P + 0));

    assertMoveOk(move(rId, state, operations));
  }
  
  @Test
  public void testRedGeneralCaptureGeneral() {
    Map<String, Object> state = ImmutableMap.<String, Object>builder()
        .put(TURN, R)
        .put(S + 9,  P + 0)
        .put(S + 10,  P + 4)
        .put(S + 31, P + 31)
        .put(P + 0,  "rgen")
        .put(P + 4,  "bgen")
        .put(P + 31, "bsol")
        .build();

    List<Operation> operations = ImmutableList.<Operation>of(
      new Set(TURN, B),
      new Set(CAPTUREPIECE, ImmutableList.of(S + 9, S + 10)),
      new Set(S + 9, null),
      new Set(S + 10, null));

    assertMoveOk(move(rId, state, operations));
  }
  
  /*
   * Can not capture a piece of the same color as the player's.
   */
  @Test
  public void testIllegalRedGeneralCaptureRedPiece() {
    Map<String, Object> state = ImmutableMap.<String, Object>builder()
        .put(TURN, R)
        .put(S + 9,  P + 0)
        .put(S + 1,  P + 1)
        .put(S + 31, P + 31)
        .put(P + 0,  "rgen")
        .put(P + 1,  "radv")
        .put(P + 31, "bsol")
        .build();

    List<Operation> operations = ImmutableList.<Operation>of(
      new Set(TURN, B),
      new Set(CAPTUREPIECE, ImmutableList.of(S + 9, S + 1)),
      new Set(S + 9, null),
      new Set(S + 1, P + 0));

    assertHacker(move(bId, state, operations));
  }
  
  @Test
  public void testWrongPlayerCaptureAdvisor() {
    Map<String, Object> state = ImmutableMap.<String, Object>builder()
        .put(TURN, R)
        .put(S + 9,  P + 0)
        .put(S + 1,  P + 1)
        .put(S + 31, P + 31)
        .put(P + 0,  "rgen")
        .put(P + 1,  "badv")
        .put(P + 31, "bsol")
        .build();

    List<Operation> operations = ImmutableList.<Operation>of(
      new Set(TURN, B),
      new Set(CAPTUREPIECE, ImmutableList.of(S + 9, S + 1)),
      new Set(S + 9, null),
      new Set(S + 1, P + 0));

    assertHacker(move(bId, state, operations));
  }

  @Test
  public void testRedSoldierCaptureGeneral() {
    Map<String, Object> state = ImmutableMap.<String, Object>builder()
        .put(TURN, R)
        .put(S + 9,  P + 0)
        .put(S + 1,  P + 1)
        .put(S + 31, P + 31)
        .put(P + 0,  "rsol")
        .put(P + 1,  "bgen")
        .put(P + 31, "bsol")
        .build();

    List<Operation> operations = ImmutableList.<Operation>of(
      new Set(TURN, B),
      new Set(CAPTUREPIECE, ImmutableList.of(S + 9, S + 1)),
      new Set(S + 9, null),
      new Set(S + 1, P + 0));

    assertMoveOk(move(rId, state, operations));
  }
  
  /*
   * Can not capture because the rank of the advisor is lower than the general.
   */
  @Test
  public void testIllegalRedAdvisorCaptureGeneral() {
    Map<String, Object> state = ImmutableMap.<String, Object>builder()
        .put(TURN, R)
        .put(S + 9,  P + 0)
        .put(S + 1,  P + 1)
        .put(S + 31, P + 31)
        .put(P + 0,  "radv")
        .put(P + 1,  "bgen")
        .put(P + 31, "bsol")
        .build();

    List<Operation> operations = ImmutableList.<Operation>of(
      new Set(TURN, B),
      new Set(CAPTUREPIECE, ImmutableList.of(S + 9, S + 1)),
      new Set(S + 9, null),
      new Set(S + 1, P + 0));

    assertHacker(move(rId, state, operations));
  }
  
  /*
   * Can not capture because the rank of the chariot is lower than the advisor.
   */
  @Test
  public void testIllegalRedChariotCaptureAdvisor() {
    Map<String, Object> state = ImmutableMap.<String, Object>builder()
        .put(TURN, R)
        .put(S + 9,  P + 0)
        .put(S + 1,  P + 1)
        .put(S + 31, P + 31)
        .put(P + 0,  "rcha")
        .put(P + 1,  "badv")
        .put(P + 31, "bsol")
        .build();

    List<Operation> operations = ImmutableList.<Operation>of(
      new Set(TURN, B),
      new Set(CAPTUREPIECE, ImmutableList.of(S + 9, S + 1)),
      new Set(S + 9, null),
      new Set(S + 1, P + 0));

    assertHacker(move(rId, state, operations));
  }
  
  /*
   * Can not capture because the rank of the horse is lower than the chariot.
   */
  @Test
  public void testIllegalRedHorseCaptureChariot() {
    Map<String, Object> state = ImmutableMap.<String, Object>builder()
        .put(TURN, R)
        .put(S + 9,  P + 0)
        .put(S + 1,  P + 1)
        .put(S + 31, P + 31)
        .put(P + 0,  "rhor")
        .put(P + 1,  "bcha")
        .put(P + 31, "bsol")
        .build();

    List<Operation> operations = ImmutableList.<Operation>of(
      new Set(TURN, B),
      new Set(CAPTUREPIECE, ImmutableList.of(S + 9, S + 1)),
      new Set(S + 9, null),
      new Set(S + 1, P + 0));

    assertHacker(move(rId, state, operations));
  }
  
  /*
   * Can not capture because the rank of the soldier is lower than the cannon.
   */
  @Test
  public void testIllegalRedSoldierCaptureCannon() {
    Map<String, Object> state = ImmutableMap.<String, Object>builder()
        .put(TURN, R)
        .put(S + 9,  P + 0)
        .put(S + 1,  P + 1)
        .put(S + 31, P + 31)
        .put(P + 0,  "rsol")
        .put(P + 1,  "bcan")
        .put(P + 31, "bsol")
        .build();

    List<Operation> operations = ImmutableList.<Operation>of(
      new Set(TURN, B),
      new Set(CAPTUREPIECE, ImmutableList.of(S + 9, S + 1)),
      new Set(S + 9, null),
      new Set(S + 1, P + 0));

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
        .put(TURN, R)
        .put(S + 0,  P + 0)
        .put(S + 1,  P + 1)
        .put(S + 2,  P + 2)
        .put(S + 31, P + 31)
        .put(P + 0,  "rcan")
        .put(P + 1,  "bcan")
        .put(P + 2,  "bgen")
        .put(P + 31, "bsol")
        .build();

    List<Operation> operations = ImmutableList.<Operation>of(
      new Set(TURN, B),
      new Set(CAPTUREPIECE, ImmutableList.of(S + 0, S + 2)),
      new Set(S + 0, null),
      new Set(S + 2, P + 0));

    assertMoveOk(move(rId, state, operations));
  }
  
  @Test
  public void testRedCannonCaptureGeneralInSameColumn() {
    Map<String, Object> state = ImmutableMap.<String, Object>builder()
        .put(TURN, R)
        .put(S + 0,  P + 0)
        .put(S + 8,  P + 4)
        .put(S + 24,  P + 5)
        .put(S + 31, P + 31)
        .put(P + 0,  "rcan")
        .put(P + 4,  "bcan")
        .put(P + 5,  "bgen")
        .put(P + 31, "bsol")
        .build();

    List<Operation> operations = ImmutableList.<Operation>of(
      new Set(TURN, B),
      new Set(CAPTUREPIECE, ImmutableList.of(S + 0, S + 24)),
      new Set(S + 0, null),
      new Set(S + 24, P + 0));

    assertMoveOk(move(rId, state, operations));
  }
  
  /*
   * Can not capture because there needs exactly 1 piece in between.
   */
  @Test
  public void testIllegalRedCannonCaptureWithNoPieceBetween() {
    Map<String, Object> state = ImmutableMap.<String, Object>builder()
        .put(TURN, R)
        .put(S + 0,  P + 0)
        .put(S + 1,  P + 1)
        .put(S + 31, P + 31)
        .put(P + 0,  "rcan")
        .put(P + 1,  "bcan")
        .put(P + 31, "bsol")
        .build();

    List<Operation> operations = ImmutableList.<Operation>of(
      new Set(TURN, B),
      new Set(CAPTUREPIECE, ImmutableList.of(S + 0, S + 1)),
      new Set(S + 0, null),
      new Set(S + 1, P + 0));

    assertHacker(move(rId, state, operations));
  }
  
  /*
   * Can not capture because there needs exactly 1 piece in between.
   */
  @Test
  public void testIllegalRedCannonCaptureWithTooMuchPieceBetween() {
    Map<String, Object> state = ImmutableMap.<String, Object>builder()
        .put(TURN, R)
        .put(S + 0,  P + 0)
        .put(S + 1,  P + 1)
        .put(S + 2,  P + 5)
        .put(S + 3,  P + 3)
        .put(S + 31, P + 31)
        .put(P + 0,  "rcan")
        .put(P + 1,  "bcan")
        .put(P + 2,  "bsol")
        .put(P + 3,  "badv")
        .put(P + 31, "bsol")
        .build();

    List<Operation> operations = ImmutableList.<Operation>of(
      new Set(TURN, B),
      new Set(CAPTUREPIECE, ImmutableList.of(S + 0, S + 3)),
      new Set(S + 0, null),
      new Set(S + 3, P + 0));

    assertHacker(move(rId, state, operations));
  }
  
  /*
   * End the game by capturing all the opponent's pieces.
   */
  @Test
  public void testEndGame() {
    Map<String, Object> state = ImmutableMap.<String, Object>builder()
        .put(TURN, R)
        .put(S + 0,  P + 0)
        .put(S + 1,  P + 1)
        .put(P + 0,  "rgen")
        .put(P + 1,  "bcan")
        .build();

    List<Operation> operations = ImmutableList.<Operation>of(
      new Set(TURN, R),
      new Set(CAPTUREPIECE, ImmutableList.of(S + 0, S + 1)),
      new Set(S + 0, null),
      new Set(S + 1, P + 0),
      new EndGame(rId));


    assertMoveOk(move(rId, state, operations));
  }
}
