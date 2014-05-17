package org.banqi.client;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.banqi.client.BanqiPresenter.View;
import org.game_api.GameApi;
import org.game_api.GameApi.Container;
import org.game_api.GameApi.Operation;
import org.game_api.GameApi.SetTurn;
import org.game_api.GameApi.UpdateUI;
import org.game_api.GameApi.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * Tests for {@link BanqiPresenter}. Test plan: There are several interesting
 * states: 1) empty state 2) do a turn piece operation 3) do a move piece
 * operation 4) do a capture piece operation 5) game-over
 * 
 * There are several interesting yourPlayerId: 1) white player 2) black player
 * 3) viewer
 * 
 * For each one of these states and for each yourPlayerId, I will test what
 * methods the presenters calls on the view and container. In addition I will
 * also test the interactions between the presenter and view, i.e., the view can
 * call one of these methods: 1) cellSelected
 */
@RunWith(JUnit4.class)
public class BanqiPresenterTest {
  /** The class under test. */
  private BanqiPresenter banqiPresenter;
  private final BanqiLogic banqiLogic = new BanqiLogic();
  private View mockView;
  private Container mockContainer;

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
  private final String viewerId = GameApi.VIEWER_ID;
  private final String rId = "7";
  private final String bId = "47";
  
  private final ImmutableList<String> playerIds = ImmutableList.of(rId, bId);
  private final ImmutableMap<String, Object> rInfo = ImmutableMap
      .<String, Object> of(PLAYER_ID, rId);
  private final ImmutableMap<String, Object> bInfo = ImmutableMap
      .<String, Object> of(PLAYER_ID, bId);
  private final ImmutableList<Map<String, Object>> playersInfo = ImmutableList
      .<Map<String, Object>> of(rInfo, bInfo);

  /* The interesting states that I'll test. */
  private final ImmutableMap<String, Object> emptyState = ImmutableMap
      .<String, Object> of();
  
  private final Map<String, Object> testGameOverState = getEndGameState();
  
  /*
   * State for testing movePiece, turnPiece and capturePiece
   * Red:
   * Move C0 to C1
   * Turn C0
   * Capture C8: Black Adivisor by C0: Red General
   * 
   * Black:
   * Move C31 to S30
   * Turn C0
   * Capture C9: Red Elephant by P8: Black Adivisor
   * 
   * {C0 |   |   |   |   |   |   |   }
   * rgen
   * {C8 |C9 |C10|   |   |   |   |   }
   * badv|rele|rcan
   * {   |   |   |   |   |   |   |   }
   * {C24|   |   |   |   |   |   |C31}
   * bhor                        bsol
   */
  
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
  
  public Map<String, Object> getEndGameState() {
    Map<String, Object> state = getEmptyBoardState();
    state.put("C0",  "rgen");
    return state;
  }
  
  public Map<String, Object> getAllFacingUpState() {
    Map<String, Object> state = getEmptyBoardState();
    state.put("C0",  "rgen");
    state.put("C8",  "badv");
    state.put("C9",  "rele");
    state.put("C10", "rcan");
    state.put("C24", "bhor");
    state.put("C31", "bsol");
    return state;
  }
  
  public Map<String, Object> getAllFacingDownState() {
    Map<String, Object> state = getEmptyBoardState();
    state.put("C0",  null);
    state.put("C8",  null);
    state.put("C9",  null);
    state.put("C10", null);
    state.put("C24", null);
    state.put("C31", null);
    return state;
  }
  
  /* State for testing turnPiece, all pieces are facing-down */
  private final Map<String, Object> allFacingUpState = getAllFacingUpState();
  /* State for testing movePiece and capturePiece, all pieces are facing-up */
  private final Map<String, Object> allFacingDownState = getAllFacingDownState();

  @Before
  public void runBefore() {
    mockView = Mockito.mock(View.class);
    mockContainer = Mockito.mock(Container.class);
    banqiPresenter = new BanqiPresenter(mockView, mockContainer);
    verify(mockView).setPresenter(banqiPresenter);
  }

  @After
  public void runAfter() {
    // This will ensure I didn't forget to declare any extra interaction the
    // mocks have.
    verifyNoMoreInteractions(mockContainer);
    verifyNoMoreInteractions(mockView);
  }

  @Test
  public void testEmptyStateForR() {
    banqiPresenter.updateUI(createUpdateUI(rId, "0", emptyState));
    verify(mockContainer).sendMakeMove(banqiLogic.getMoveInitial(playerIds));
  }

  @Test
  public void testEmptyStateForB() {
    banqiPresenter.updateUI(createUpdateUI(bId, "0", emptyState));
  }

  @Test
  public void testEmptyStateForViewer() {
    banqiPresenter.updateUI(createUpdateUI(viewerId, "0", emptyState));
  }

  @Test
  public void testGameOverStateForR() {
    UpdateUI updateUI = createUpdateUI(rId, rId, testGameOverState);
    BanqiState banqiState = banqiLogic.gameApiStateToBanqiState(
        updateUI.getState(), Color.R, playerIds);
    banqiPresenter.updateUI(updateUI);

    verify(mockView).setPlayerState(banqiPresenter.getAllCells(banqiState), false);
    verify(mockContainer).sendMakeMove(
        banqiLogic.getEndGameOperation(banqiState));
  }

  @Test
  public void testGameOverStateForB() {
    UpdateUI updateUI = createUpdateUI(rId, rId, testGameOverState);
    BanqiState banqiState = banqiLogic.gameApiStateToBanqiState(
        updateUI.getState(), Color.B, playerIds);
    banqiPresenter.updateUI(updateUI);

    verify(mockView).setPlayerState(banqiPresenter.getAllCells(banqiState), false);
    verify(mockContainer).sendMakeMove(
        banqiLogic.getEndGameOperation(banqiState));
  }

  @Test
  public void testGameOverStateForViewer() {
    UpdateUI updateUI = createUpdateUI(viewerId, rId, testGameOverState);
    BanqiState banqiState = banqiLogic.gameApiStateToBanqiState(
        updateUI.getState(), Color.R, playerIds);
    banqiPresenter.updateUI(updateUI);

    verify(mockView).setViewerState(banqiPresenter.getAllCells(banqiState));
  }

  /* Tests for turning up a piece by R. */
  @Test
  public void testForRTurnUpAPiece() {
    UpdateUI updateUI = createUpdateUI(rId, rId, allFacingDownState);
    BanqiState banqiState = banqiLogic.gameApiStateToBanqiState(
        updateUI.getState(), Color.R, playerIds);
    banqiPresenter.updateUI(updateUI);
    banqiPresenter.cellSelected(0, false);

    verify(mockView).setPlayerState(banqiPresenter.getAllCells(banqiState), false);
    verify(mockView).chooseNextCell(banqiPresenter.getAllCells(banqiState),
        new ArrayList<Integer>());
    verify(mockContainer).sendMakeMove(
        banqiLogic.getTurnPieceOperation(banqiState, new Set(TURNPIECE, "C0")));
  }

  /* Tests for turning up a piece by B. */
  @Test
  public void testForBTurnUpAPiece() {
    UpdateUI updateUI = createUpdateUI(bId, bId, allFacingDownState);
    BanqiState banqiState = banqiLogic.gameApiStateToBanqiState(
        updateUI.getState(), Color.B, playerIds);
    banqiPresenter.updateUI(updateUI);
    banqiPresenter.cellSelected(0, false);

    verify(mockView).setPlayerState(banqiPresenter.getAllCells(banqiState), false);
    verify(mockView).chooseNextCell(banqiPresenter.getAllCells(banqiState),
        new ArrayList<Integer>());
    verify(mockContainer).sendMakeMove(
        banqiLogic.getTurnPieceOperation(banqiState, new Set(TURNPIECE, "C0")));
  }

  /* Tests for moving a piece by R. Move the piece from S0 to S1. */
  @Test
  public void testForRMoveAPiece() {
    UpdateUI updateUI = createUpdateUI(rId, rId, allFacingUpState);
    BanqiState banqiState = banqiLogic.gameApiStateToBanqiState(
        updateUI.getState(), Color.R, playerIds);
    banqiPresenter.updateUI(updateUI);
    banqiPresenter.cellSelected(0, false);
    banqiPresenter.cellSelected(1, false);

    verify(mockView).setPlayerState(banqiPresenter.getAllCells(banqiState), false);
    verify(mockView).chooseNextCell(banqiPresenter.getAllCells(banqiState),
        new ArrayList<Integer>());
    verify(mockView).chooseNextCell(banqiPresenter.getAllCells(banqiState),
        Lists.newArrayList(0));
    verify(mockContainer).sendMakeMove(
        banqiLogic.getMovePieceOperation(banqiState, new Set(MOVEPIECE,
            ImmutableList.of("C0", "C1"))));
  }

  /* Tests for moving a piece by B. Move the piece from S31 to S30. */
  @Test
  public void testForBMoveAPiece() {
    UpdateUI updateUI = createUpdateUI(bId, bId, allFacingUpState);
    BanqiState banqiState = banqiLogic.gameApiStateToBanqiState(
        updateUI.getState(), Color.B, playerIds);
    banqiPresenter.updateUI(updateUI);
    banqiPresenter.cellSelected(31, false);
    banqiPresenter.cellSelected(30, false);

    verify(mockView).setPlayerState(banqiPresenter.getAllCells(banqiState), false);
    verify(mockView).chooseNextCell(banqiPresenter.getAllCells(banqiState),
        new ArrayList<Integer>());
    verify(mockView).chooseNextCell(banqiPresenter.getAllCells(banqiState),
        Lists.newArrayList(31));
    verify(mockContainer).sendMakeMove(
        banqiLogic.getMovePieceOperation(banqiState, new Set(MOVEPIECE,
            ImmutableList.of("C31", "C30"))));
  }

  /* Tests for moving a piece by B and unselecting a piece once. */
  @Test
  public void testForBMoveAPieceAndUnselectAPiece() {
    UpdateUI updateUI = createUpdateUI(bId, bId, allFacingUpState);
    BanqiState banqiState = banqiLogic.gameApiStateToBanqiState(
        updateUI.getState(), Color.B, playerIds);
    banqiPresenter.updateUI(updateUI);
    banqiPresenter.cellSelected(24, false);
    banqiPresenter.cellSelected(24, false);
    banqiPresenter.cellSelected(31, false);
    banqiPresenter.cellSelected(30, false);

    verify(mockView).setPlayerState(banqiPresenter.getAllCells(banqiState), false);
    verify(mockView, times(2)).chooseNextCell(
        banqiPresenter.getAllCells(banqiState), new ArrayList<Integer>());
    verify(mockView).chooseNextCell(banqiPresenter.getAllCells(banqiState),
        Lists.newArrayList(24));
    verify(mockView).chooseNextCell(banqiPresenter.getAllCells(banqiState),
        Lists.newArrayList(31));
    verify(mockContainer).sendMakeMove(
        banqiLogic.getMovePieceOperation(banqiState, new Set(MOVEPIECE,
            ImmutableList.of("C31", "C30"))));
  }

  /* Tests for capturing a piece by R. */
  @Test
  public void testForRCaptureAPiece() {
    UpdateUI updateUI = createUpdateUI(rId, rId, allFacingUpState);
    BanqiState banqiState = banqiLogic.gameApiStateToBanqiState(
        updateUI.getState(), Color.R, playerIds);
    banqiPresenter.updateUI(updateUI);
    banqiPresenter.cellSelected(0, false);
    banqiPresenter.cellSelected(8, false);

    verify(mockView).setPlayerState(banqiPresenter.getAllCells(banqiState), false);
    verify(mockView).chooseNextCell(banqiPresenter.getAllCells(banqiState),
        new ArrayList<Integer>());
    verify(mockView).chooseNextCell(banqiPresenter.getAllCells(banqiState),
        Lists.newArrayList(0));
    verify(mockContainer).sendMakeMove(
        banqiLogic.getCapturePieceOperation(banqiState, new Set(CAPTUREPIECE,
            ImmutableList.of("C0", "C8"))));
  }

  /* Tests for cannon capturing a piece by R. */
  @Test
  public void testForRCannonCaptureAPiece() {
    UpdateUI updateUI = createUpdateUI(rId, rId, allFacingUpState);
    BanqiState banqiState = banqiLogic.gameApiStateToBanqiState(
        updateUI.getState(), Color.R, playerIds);
    banqiPresenter.updateUI(updateUI);
    banqiPresenter.cellSelected(10, false);
    banqiPresenter.cellSelected(8, false);

    verify(mockView).setPlayerState(banqiPresenter.getAllCells(banqiState), false);
    verify(mockView).chooseNextCell(banqiPresenter.getAllCells(banqiState),
        new ArrayList<Integer>());
    verify(mockView).chooseNextCell(banqiPresenter.getAllCells(banqiState),
        Lists.newArrayList(10));
    verify(mockContainer).sendMakeMove(
        banqiLogic.getCapturePieceOperation(banqiState, new Set(CAPTUREPIECE,
            ImmutableList.of("C10", "C8"))));
  }

  /* Tests for capturing a piece by B. */
  @Test
  public void testForBCaptureAPiece() {
    UpdateUI updateUI = createUpdateUI(bId, bId, allFacingUpState);
    BanqiState banqiState = banqiLogic.gameApiStateToBanqiState(
        updateUI.getState(), Color.B, playerIds);
    banqiPresenter.updateUI(updateUI);
    banqiPresenter.cellSelected(8, false);
    banqiPresenter.cellSelected(9, false);

    verify(mockView).setPlayerState(banqiPresenter.getAllCells(banqiState), false);
    verify(mockView).chooseNextCell(banqiPresenter.getAllCells(banqiState),
        new ArrayList<Integer>());
    verify(mockView).chooseNextCell(banqiPresenter.getAllCells(banqiState),
        Lists.newArrayList(8));
    verify(mockContainer).sendMakeMove(
        banqiLogic.getCapturePieceOperation(banqiState, new Set(CAPTUREPIECE,
            ImmutableList.of("C8", "C9"))));
  }

  private UpdateUI createUpdateUI(String yourPlayerId, String turnOfPlayerId,
      Map<String, Object> state) {
    // Our UI only looks at the current state
    // (we ignore: lastState, lastMovePlayerId, playerIdToNumberOfTokensInPot)
    return new UpdateUI(yourPlayerId, playersInfo, state,
        emptyState, // we ignore lastState
        ImmutableList.<Operation> of(new SetTurn(turnOfPlayerId)), "0",
        ImmutableMap.<String, Integer> of());
  }
}
