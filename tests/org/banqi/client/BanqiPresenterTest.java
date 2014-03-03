package org.banqi.client;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.banqi.client.Piece.Kind;
import org.banqi.client.Piece.PieceColor;
import org.banqi.client.BanqiPresenter.View;
import org.banqi.client.GameApi.Container;
import org.banqi.client.GameApi.Operation;
import org.banqi.client.GameApi.SetTurn;
import org.banqi.client.GameApi.UpdateUI;
import org.banqi.client.GameApi.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/** Tests for {@link BanqiPresenter}.
 * Test plan:
 * There are several interesting states:
 * 1) empty state
 * 2) do a turn piece operation
 * 3) do a move piece operation
 * 4) do a capture piece operation
 * 5) game-over
 * 
 * There are several interesting yourPlayerId:
 * 1) white player
 * 2) black player
 * 3) viewer
 * 
 * For each one of these states and for each yourPlayerId,
 * I will test what methods the presenters calls on the view and container.
 * In addition I will also test the interactions between the presenter and view, i.e.,
 * the view can call one of these methods:
 * 1) pieceSelected
 * 2) squareSelected
 * 3) finishedSelection
 */
@RunWith(JUnit4.class)
public class BanqiPresenterTest {
  /** The class under test. */
  private BanqiPresenter banqiPresenter;
  private final BanqiLogic banqiLogic = new BanqiLogic();
  private View mockView;
  private Container mockContainer;

  private static final String PLAYER_ID = "playerId";
  /* 
   * The entries used in the banqi game are:
   *   turnPiece, movePiece, capturePiece, S0...S31, P0...P31
   */
  private static final String S = "S"; // Square i
  private static final String P = "P"; // Piece i
  private static final boolean ISVISIBLE = true;
  private static final String MOVEPIECE = "movePiece"; //A move has the form: [from, to]
  private static final String TURNPIECE = "turnPiece"; //A turn has the form: [coordinate]
  private static final String CAPTUREPIECE = "capturePiece"; //A capture has the form: [from, to]
  private final int viewerId = GameApi.VIEWER_ID;
  private final int rId = 7;
  private final int bId = 47;
  private final ImmutableList<Integer> playerIds = ImmutableList.of(rId, bId);
  private final ImmutableMap<String, Object> rInfo =
      ImmutableMap.<String, Object>of(PLAYER_ID, rId);
  private final ImmutableMap<String, Object> bInfo =
      ImmutableMap.<String, Object>of(PLAYER_ID, bId);
  private final ImmutableList<Map<String, Object>> playersInfo =
      ImmutableList.<Map<String, Object>>of(rInfo, bInfo);

  /* The interesting states that I'll test. */
  private final ImmutableMap<String, Object> emptyState = ImmutableMap.<String, Object>of();
  /* State for testing game over. */
  ImmutableMap<Integer, Integer> testGameOverBoardSetting = ImmutableMap.<Integer, Integer>builder()
      .put(0, 0)
      .build();
  private final Map<String, Object> testGameOverState = createState(testGameOverBoardSetting, true);
  /*
   * State for testing movePiece, turnPiece and capturePiece
   * Red:
   * Move P0 from S0 to S1.
   * Turn P0 from S0.
   * Capture P17:Black Adivisor by P0:Red General from S0 to S8.
   *
   * Black:
   * Move P31 from S31 to S30.
   * Turn P0 from S0.
   * Capture P3:Red Elephant by P17:Black Adivisor from S8 to S9.
   * 
   * {P0  |    |    |    |    |    |    |    }
   * rgen 
   * {P17 |P3  |    |    |    |    |    |    }
   * badv rele
   * {    |    |    |    |    |    |    |    }
   * 
   * {P24 |    |    |    |    |    |    |P31 } 
   * bhor                               bsol                                  
   **/
  ImmutableMap<Integer, Integer> testBoardSetting =
      ImmutableMap.<Integer, Integer>builder()
    .put(0, 0)
    .put(8, 17)
    .put(9, 3)
    .put(24, 24)
    .put(31, 31)
    .build();
  /* State for testing turnPiece, all pieces are facing-down */
  private final Map<String, Object> allFacingDownState = createState(
      testBoardSetting, !ISVISIBLE);
  /* State for testing movePiece and capturePiece, all pieces are facing-up */
  private final Map<String, Object> allFacingUpState = createState(
      testBoardSetting, ISVISIBLE);
  
  @Before
  public void runBefore() {
    mockView = Mockito.mock(View.class);
    mockContainer = Mockito.mock(Container.class);
    banqiPresenter = new BanqiPresenter(mockView, mockContainer);
    verify(mockView).setPresenter(banqiPresenter);
  }

  @After
  public void runAfter() {
    // This will ensure I didn't forget to declare any extra interaction the mocks have.
    verifyNoMoreInteractions(mockContainer);
    verifyNoMoreInteractions(mockView);
  }
  
  @Test
  public void testEmptyStateForR() {
    banqiPresenter.updateUI(createUpdateUI(rId, 0, emptyState));
    verify(mockContainer).sendMakeMove(banqiLogic.getMoveInitial(playerIds));
  }

  @Test
  public void testEmptyStateForB() {
    banqiPresenter.updateUI(createUpdateUI(bId, 0, emptyState));
  }

  @Test
  public void testEmptyStateForViewer() {
    banqiPresenter.updateUI(createUpdateUI(viewerId, 0, emptyState));
  }
  
  @Test
  public void testGameOverStateForR() {
    UpdateUI updateUI = createUpdateUI(rId, rId, testGameOverState);
    State banqiState =
        banqiLogic.gameApiStateToBanqiState(updateUI.getState(), Color.R, playerIds);
    banqiPresenter.updateUI(createUpdateUI(rId, rId, testGameOverState));
    
    verify(mockView).setPlayerState(banqiPresenter.getAllSquares(banqiState), getPieces());
    verify(mockContainer).sendMakeMove(banqiLogic.getEndGameOperation(banqiState));
  }

  @Test
  public void testGameOverStateForB() {
    UpdateUI updateUI = createUpdateUI(rId, rId, testGameOverState);
    State banqiState =
        banqiLogic.gameApiStateToBanqiState(updateUI.getState(), Color.B, playerIds); 
    banqiPresenter.updateUI(createUpdateUI(bId, bId, testGameOverState));
    
    verify(mockView).setPlayerState(banqiPresenter.getAllSquares(banqiState), getPieces());
    verify(mockContainer).sendMakeMove(banqiLogic.getEndGameOperation(banqiState));
  }

  @Test
  public void testGameOverStateForViewer() {
    UpdateUI updateUI = createUpdateUI(viewerId, rId, testGameOverState);
    State banqiState =
        banqiLogic.gameApiStateToBanqiState(updateUI.getState(), Color.R, playerIds);   
    banqiPresenter.updateUI(createUpdateUI(viewerId, rId, testGameOverState));
    
    verify(mockView).setViewerState(banqiPresenter.getAllSquares(banqiState), getPieces());
  }
  
  /* Tests for turning up a piece by R. */
  @Test
  public void testForRTurnUpAPiece() {
    UpdateUI updateUI = createUpdateUI(rId, rId, allFacingDownState);
    State banqiState =
        banqiLogic.gameApiStateToBanqiState(updateUI.getState(), Color.R, playerIds);
    banqiPresenter.updateUI(updateUI);
    banqiPresenter.pieceSelected(0);
    
    verify(mockView).setPlayerState(banqiPresenter.getAllSquares(banqiState), getInvisiblePieces());
    verify(mockView).chooseNextPieceOrSquare(new ArrayList<Integer>());
    verify(mockContainer).sendMakeMove(
        banqiLogic.getTurnPieceOperation(banqiState, new Set(TURNPIECE, "S0")));
  }
  
  /* Tests for turning up a piece by B. */
  @Test
  public void testForBTurnUpAPiece() {
    UpdateUI updateUI = createUpdateUI(bId, bId, allFacingDownState);
    State banqiState =
        banqiLogic.gameApiStateToBanqiState(updateUI.getState(), Color.B, playerIds);
    banqiPresenter.updateUI(updateUI);
    banqiPresenter.pieceSelected(0);
    
    verify(mockView).setPlayerState(banqiPresenter.getAllSquares(banqiState), getInvisiblePieces());
    verify(mockView).chooseNextPieceOrSquare(new ArrayList<Integer>());
    verify(mockContainer).sendMakeMove(
        banqiLogic.getTurnPieceOperation(banqiState, new Set(TURNPIECE, "S0")));
  }
  
  /* Tests for moving a piece by R. Move the piece from S0 to S1. */
  @Test
  public void testForRMoveAPiece() {
    UpdateUI updateUI = createUpdateUI(rId, rId, allFacingUpState);
    State banqiState =
        banqiLogic.gameApiStateToBanqiState(updateUI.getState(), Color.R, playerIds);
    banqiPresenter.updateUI(updateUI);
    banqiPresenter.pieceSelected(0);
    banqiPresenter.squareSelected(1);
    
    verify(mockView).setPlayerState(banqiPresenter.getAllSquares(banqiState), getPieces());
    verify(mockView).chooseNextPieceOrSquare(new ArrayList<Integer>());
    verify(mockView).chooseNextPieceOrSquare(Lists.newArrayList(0));
    verify(mockContainer).sendMakeMove(
        banqiLogic.getMovePieceOperation(
            banqiState, new Set(MOVEPIECE, ImmutableList.of("S0", "S1"))));
  }
  
  /* Tests for moving a piece by B. Move the piece from S31 to S30. */
  @Test
  public void testForBMoveAPiece() {
    UpdateUI updateUI = createUpdateUI(bId, bId, allFacingUpState);
    State banqiState =
        banqiLogic.gameApiStateToBanqiState(updateUI.getState(), Color.B, playerIds);
    banqiPresenter.updateUI(updateUI);
    banqiPresenter.pieceSelected(31);
    banqiPresenter.squareSelected(30);
    
    verify(mockView).setPlayerState(banqiPresenter.getAllSquares(banqiState), getPieces());
    verify(mockView).chooseNextPieceOrSquare(new ArrayList<Integer>());
    verify(mockView).chooseNextPieceOrSquare(Lists.newArrayList(31));
    verify(mockContainer).sendMakeMove(
        banqiLogic.getMovePieceOperation(
            banqiState, new Set(MOVEPIECE, ImmutableList.of("S31", "S30"))));
  }
  
  /* Tests for moving a piece by B and unselecting a piece once. */
  @Test
  public void testForBMoveAPieceAndUnselectAPiece() {
    UpdateUI updateUI = createUpdateUI(bId, bId, allFacingUpState);
    State banqiState =
        banqiLogic.gameApiStateToBanqiState(updateUI.getState(), Color.B, playerIds);
    banqiPresenter.updateUI(updateUI);
    banqiPresenter.pieceSelected(24);
    banqiPresenter.pieceSelected(24);
    banqiPresenter.pieceSelected(31);
    banqiPresenter.squareSelected(30);
    
    verify(mockView).setPlayerState(banqiPresenter.getAllSquares(banqiState), getPieces());
    verify(mockView, times(2)).chooseNextPieceOrSquare(new ArrayList<Integer>());
    verify(mockView).chooseNextPieceOrSquare(Lists.newArrayList(24));
    verify(mockView).chooseNextPieceOrSquare(Lists.newArrayList(31));
    verify(mockContainer).sendMakeMove(
        banqiLogic.getMovePieceOperation(
            banqiState, new Set(MOVEPIECE, ImmutableList.of("S31", "S30"))));
  }
  
  /* Tests for capturing a piece by R. */
  @Test
  public void testForRCaptureAPiece() {
    UpdateUI updateUI = createUpdateUI(rId, rId, allFacingUpState);
    State banqiState =
        banqiLogic.gameApiStateToBanqiState(updateUI.getState(), Color.R, playerIds);
    banqiPresenter.updateUI(updateUI);
    banqiPresenter.pieceSelected(0);
    banqiPresenter.pieceSelected(17);
    
    verify(mockView).setPlayerState(banqiPresenter.getAllSquares(banqiState), getPieces());
    verify(mockView).chooseNextPieceOrSquare(new ArrayList<Integer>());
    verify(mockView).chooseNextPieceOrSquare(Lists.newArrayList(0));
    verify(mockContainer).sendMakeMove(
        banqiLogic.getCapturePieceOperation(
            banqiState, new Set(CAPTUREPIECE, ImmutableList.of("S0", "S8"))));
  }

  /* Tests for capturing a piece by B. */
  @Test
  public void testForBCaptureAPiece() {
    UpdateUI updateUI = createUpdateUI(bId, bId, allFacingUpState);
    State banqiState =
        banqiLogic.gameApiStateToBanqiState(updateUI.getState(), Color.B, playerIds);
    banqiPresenter.updateUI(updateUI);
    banqiPresenter.pieceSelected(17);
    banqiPresenter.pieceSelected(3);
    
    verify(mockView).setPlayerState(banqiPresenter.getAllSquares(banqiState), getPieces());
    verify(mockView).chooseNextPieceOrSquare(new ArrayList<Integer>());
    verify(mockView).chooseNextPieceOrSquare(Lists.newArrayList(17));
    verify(mockContainer).sendMakeMove(
        banqiLogic.getCapturePieceOperation(
            banqiState, new Set(CAPTUREPIECE, ImmutableList.of("S8", "S9"))));
  }
  
  /* Tests for capturing a piece by B and unselect a piece once. */
  @Test
  public void testForBCaptureAPieceAndUnselectAPiece() {
    UpdateUI updateUI = createUpdateUI(bId, bId, allFacingUpState);
    State banqiState =
        banqiLogic.gameApiStateToBanqiState(updateUI.getState(), Color.B, playerIds);
    banqiPresenter.updateUI(updateUI);
    banqiPresenter.pieceSelected(0);
    banqiPresenter.pieceSelected(0);
    banqiPresenter.pieceSelected(17);
    banqiPresenter.pieceSelected(3);
    
    verify(mockView).setPlayerState(banqiPresenter.getAllSquares(banqiState), getPieces());
    verify(mockView, times(2)).chooseNextPieceOrSquare(new ArrayList<Integer>());
    verify(mockView).chooseNextPieceOrSquare(Lists.newArrayList(0));
    verify(mockView).chooseNextPieceOrSquare(Lists.newArrayList(17));
    verify(mockContainer).sendMakeMove(
        banqiLogic.getCapturePieceOperation(
            banqiState, new Set(CAPTUREPIECE, ImmutableList.of("S8", "S9"))));
  }
  
  /** Get all facing-up pieces. */
  private List<Piece> getPieces() {
    List<Piece> pieces = Lists.newArrayList();
    for (int pieceId = 0; pieceId < 32; pieceId++) {
      int colorId = pieceId / 16;
      PieceColor color = PieceColor.values()[colorId];
      int kindId = pieceId % 16 == 0 ? 0 //GENERAL
          : pieceId % 16 < 3 ? 1 //ADVISOR
          : pieceId % 16 < 5 ? 2 //ELEPHANT
          : pieceId % 16 < 7 ? 3 //CHARIOT
          : pieceId % 16 < 9 ? 4 //HORSE
          : pieceId % 16 < 11 ? 5 //CANNON
          : 6; //SOLDIER
      Kind kind = Kind.values()[kindId];
      pieces.add(new Piece(color, kind));
    }
    return pieces;
  }
  
  /** Get all facing-down pieces. */
  private List<Piece> getInvisiblePieces() {
    List<Piece> pieces = Lists.newArrayList();
    for (int i = 0; i < 32; i++) {
      pieces.add(null);
    }
    return pieces;
  }
  
  /** 
   * Create the state for testing.
   * @param banqiBoard a Map(squareCoordinate, pieceId) represents the board setting
   * @param isVisible whether all pieces are visible or not
   *  */
  private Map<String, Object> createState(
      ImmutableMap<Integer, Integer> banqiBoard, boolean isVisible) {
    Map<String, Object> state = Maps.newHashMap();
    
    
    int i = 0;
    if (!isVisible) { // Hide all the pieces.
      for (int pieceId = 0; pieceId < 32; pieceId++) {
        state.put(P + (i++), null);
      }
    } else { // Reveal all the pieces.
      for (Piece piece : getPieces()) {
      state.put(P + (i++),
          piece.getColor().getFirstLetterLowerCase()
          + piece.getKind().getFirstThreeLetterLowerCase());
      }
    }
    // Set the board according to @param banqiBoard
    for (int j = 0; j < 32; j++) {
      if (banqiBoard.containsKey(j)) {
        state.put(S + j, P + banqiBoard.get(j));
      } else {
        state.put(S + j, null);
      }
    }
    return state;
  }

  
  private UpdateUI createUpdateUI(
      int yourPlayerId, int turnOfPlayerId, Map<String, Object> state) {
    // Our UI only looks at the current state
    // (we ignore: lastState, lastMovePlayerId, playerIdToNumberOfTokensInPot)
    return new UpdateUI(yourPlayerId, playersInfo, state,
        emptyState, // we ignore lastState
        ImmutableList.<Operation>of(new SetTurn(turnOfPlayerId)),
        0,
        ImmutableMap.<Integer, Integer>of());
  }
}

