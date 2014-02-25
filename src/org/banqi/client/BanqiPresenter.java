package org.banqi.client;

import java.util.List;
import java.util.Map;

import org.banqi.client.GameApi.Container;
import org.banqi.client.GameApi.Operation;
import org.banqi.client.GameApi.Set;
import org.banqi.client.GameApi.SetTurn;
import org.banqi.client.GameApi.UpdateUI;

import com.google.appengine.repackaged.com.google.api.client.util.Maps;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * The presenter that controls the banqi graphics. We use the MVP pattern: the
 * model is {@link BanqiState}, the view will have the banqi graphics and it
 * will implement {@link BanqiPresenter.View}, and the presenter is
 * {@link BanqiPresenter}.
 */
public class BanqiPresenter {

  public interface View {
    /**
     * Sets the presenter. The viewer will call certain methods on the
     * presenter, e.g., when a piece is selected ({@link #pieceSelected}),
     * when a square is selected ({@link #squareSelected}),
     * when selection is done ({@link #finishedSelection}) etc.
     * 
     * * The process of turning up a piece looks as follows to the viewer:
     * 1) The viewer calls {@link #pieceSelected} one time to choose the piece
     * 2) The viewer calls {@link #finishedSelection} to finalize his move
     * The process of turning up a piece looks as follows to the presenter:
     * 1) The presenter calls {@link #chooseNextPieceOrSquare} and passes the current selection.
     * 
     * * The process of moving a piece looks as follows to the viewer:
     * 1) The viewer calls {@link #pieceSelected} one time first to choose a piece
     * and then calls {@link #squareSelected} one time to choose the square for the piece
     * to move to.
     * 2) The viewer calls {@link #finishedSelection} to finalize his move
     * The process of moving a piece looks as follows to the presenter:
     * 1) The presenter calls {@link #chooseNextPieceOrSquare} and passes the current selection.
     * 
     * The process of capturing a piece looks as follows to the viewer:
     * 1) The viewer calls {@link #pieceSelected} one time first to choose the capturer piece
     * and then calls {@link #pieceSelected} one more time to choose the captured piece
     * 2) The viewer calls {@link #finishedSelection} to finalize his move
     * The process of capturing a piece looks as follows to the presenter:
     * 1) The presenter calls {@link #chooseNextPieceOrSquare} and passes the current selection.
     * 
     */
    void setPresenter(BanqiPresenter banqiPresenter);

    /** Sets the state for a viewer, i.e., not one of the players. */
    void setViewerState(Map<Integer, Integer> squares, List<Piece> pieces);

    /** Sets the state for a player (whether the player has the turn or not). */
    void setPlayerState(Map<Integer, Integer> squares, List<Piece> pieces);
    
    /**
     * Asks the player to choose the next piece or square. We pass what pieces or
     * squares are selected (e.g., select a facing up piece one time and a unoccupied
     * square one time in order to perform a move).
     * The user can either select a piece (by calling {@link #cardSelected}) or select
     * a square (by calling {@link #squareSelected}) or finish selecting (by calling
     * {@link #finishedSelection}); only allowed if selectedPieces = 2 or selectedPieces = 1
     * and selectedSquares = 0 or = 1; 
     * If the user selects a card from selectedCards, then it's removed from selectedCards.
     */
    void chooseNextPieceOrSquare(List<Integer> selectedPieceIds,
        List<Integer> selectedSquares);
  }

  private final BanqiLogic banqiLogic = new BanqiLogic();
  private final View view;
  private final Container container;
  /** A viewer doesn't have a color. */
  private Optional<Color> myColor;
  private State banqiState;
  private List<Integer> selectedSquares;
  private List<Integer> selectedPieces;
  private static final String MOVEPIECE = "movePiece"; // A move has the form: [from, to]
  private static final String TURNPIECE = "turnPiece"; // A turn has the form: [coordinate]
  private static final String CAPTUREPIECE = "capturePiece"; // A capture has the form: [from, to]

  public BanqiPresenter(View view, Container container) {
    this.view = view;
    this.container = container;
    view.setPresenter(this);
  }

  /** Updates the presenter and the view with the state in updateUI. */
  public void updateUI(UpdateUI updateUI) {
    List<Integer> playerIds = updateUI.getPlayerIds();
    int yourPlayerId = updateUI.getYourPlayerId();
    int yourPlayerIndex = updateUI.getPlayerIndex(yourPlayerId);
    myColor = yourPlayerIndex == 0 ? Optional.of(Color.R)
        : yourPlayerIndex == 1 ? Optional.of(Color.B) : Optional
            .<Color> absent();

    selectedSquares = Lists.newArrayList();
    selectedPieces = Lists.newArrayList();

    if (updateUI.getState().isEmpty()) {
      // The R player sends the initial setup move.
      if (myColor.isPresent() && myColor.get().isRed()) {
        sendInitialMove(playerIds);
      }
      return;
    }
    
    Color turnOfColor = null;
    // The order of operations: turn, movePiece, turnPiece, capturePiece,
    //   P0...P31, S0...P31
    for (Operation operation : updateUI.getLastMove()) {
      if (operation instanceof SetTurn) {
        turnOfColor = Color.values()[playerIds.indexOf(((SetTurn) operation)
            .getPlayerId())];
      }
    }
    banqiState = banqiLogic.gameApiStateToBanqiState(updateUI.getState(),
        turnOfColor, playerIds);

    if (updateUI.isViewer()) {
      view.setViewerState(getBanqiBoardSetting(banqiState), getAllPieces(banqiState));
      return;
    }
    
    if (updateUI.isAiPlayer()) {
      // TODO: implement AI in a later HW!
      // container.sendMakeMove(..);
      return;
    }
    
    // Must be a player!
    boolean hasBlack = banqiState.hasRedOrBlackPieces(Color.B);
    boolean hasRed = banqiState.hasRedOrBlackPieces(Color.R);
    boolean hasFacingDownPieces = banqiState.hasFacingDownPiece();
    view.setPlayerState(getBanqiBoardSetting(banqiState), getAllPieces(banqiState));
    if (isMyTurn()) {
      if ((hasBlack ^ hasRed) && !hasFacingDownPieces) { // The game is over
        endGame();
      } else {
        // Choose the piece only if the game is not over
        chooseNextPieceOrSquare();
      }
    }
  }
  
  /** Get the board setting. */
  Map<Integer, Integer> getBanqiBoardSetting(State banqiState) {
    ImmutableList<Optional<Integer>> squares = banqiState.getSquares();
    Map<Integer, Integer> squareMapPiece = Maps.newHashMap();
    for (int i = 0; i < 32; i++) {
      if (squares.get(i).isPresent()) {
        squareMapPiece.put(i, squares.get(i).get());
      } else {
        squareMapPiece.put(i, null);
      }
    }
    return squareMapPiece;
  }
  
  /** Get all pieces from the state. */
  List<Piece> getAllPieces(State banqiState) {
    ImmutableList<Optional<Piece>> pieces = banqiState.getPieces();
    List<Piece> piece = Lists.newArrayList();
    for (int i = 0; i < 32; i++) {
      if (pieces.get(i).isPresent()) {
        piece.add(pieces.get(i).get());
      } else {
        piece.add(null);
      }
    }
    return piece;
  }
  
  private boolean isMyTurn() {
    return myColor.isPresent() && myColor.get() == banqiState.getTurn();
  }

  private void chooseNextPieceOrSquare() {
    view.chooseNextPieceOrSquare(ImmutableList.copyOf(selectedPieces),
        ImmutableList.copyOf(selectedSquares));
  }

  /**
   * Adds/remove the piece from the {@link #selectedPieces}. The view can only
   * call this method if the presenter called {@link View#chooseNextPiece}.
   */
  public void pieceSelected(int pieceId) {
    check(isMyTurn());
    if (selectedPieces.contains(pieceId)) {
      selectedPieces.remove((Integer) pieceId);
    } else if (!selectedPieces.contains(pieceId) && selectedPieces.size() < 2
        && selectedSquares.size() == 0) {
      selectedPieces.add(pieceId);
    }
    chooseNextPieceOrSquare();
  }

  /**
   * Adds/remove the square from the {@link #selectedSquares}. The view can only call
   * this method if the presenter called {@link View#chooseSquare}.
   */
  public void squareSelected(int square) {
    check(isMyTurn());
    if (selectedSquares.contains(square)) {
      selectedSquares.remove((Integer) square);
    } else if (!selectedSquares.contains(square) && selectedSquares.size() < 1) {
      selectedSquares.add(square);
    }
    chooseNextPieceOrSquare();
  }

  /**
   * Finishes the piece and/or square selection process. The view can only call
   * this method if the presenter called {@link View#chooseNextPiece} and/or
   * called {@link View#chooseSquare} and two pieces was selected by
   * calling {@link #cardSelected} or one piece was selected by
   * calling {@link #cardSelected} and one or zero square was selected by
   * calling {@link #squareSelected}.
   */

  void finishedSelection() {
    check(isMyTurn() && !selectedPieces.isEmpty());
    // TODO: more check
    ImmutableList<Optional<Integer>> squares = banqiState.getSquares();
    if (selectedPieces.size() == 1 && selectedSquares.size() == 0) {
      int selectedCoord = squares.indexOf(Optional.fromNullable(selectedPieces.get(0)));
      Set turnPiece = new Set(TURNPIECE, "S" + selectedCoord);
      turnPiece(turnPiece);
    } else if (selectedPieces.size() == 1 && selectedSquares.size() == 1) {
      int selectedFromCoord = squares.indexOf(Optional.fromNullable(selectedPieces.get(0)));
      int selectedToCoord = selectedSquares.get(0);
      Set movePiece = new Set(MOVEPIECE, ImmutableList.of("S"
          + selectedFromCoord, "S" + selectedToCoord));
      movePiece(movePiece);
    } else if (selectedPieces.size() == 2 && selectedSquares.size() == 0) {
      int selectedFromCoord = squares.indexOf(Optional.fromNullable(selectedPieces.get(0)));
      int selectedToCoord = squares.indexOf(Optional.fromNullable(selectedPieces.get(1)));
      Set capturePiece = new Set(CAPTUREPIECE, ImmutableList.of("S"
          + selectedFromCoord, "S" + selectedToCoord));
      capturePiece(capturePiece);
    }
  }

  /**
   * Sends a move that the opponent is a cheater. The view can only call this
   * method if the presenter passed CheaterMessage.IS_OPPONENT_CHEATING in
   * {@link View#setPlayerState}.
   */
  void movePiece(Set move) {
    container.sendMakeMove(banqiLogic.getMovePieceOperation(banqiState, move));
  }

  void turnPiece(Set move) {
    container.sendMakeMove(banqiLogic.getTurnPieceOperation(banqiState, move));
  }

  void capturePiece(Set move) {
    container.sendMakeMove(banqiLogic
        .getCapturePieceOperation(banqiState, move));
  }
  
  void endGame() {
    container.sendMakeMove(banqiLogic.getEndGameOperation(banqiState));
  }

  private void sendInitialMove(List<Integer> playerIds) {
    container.sendMakeMove(banqiLogic.getMoveInitial(playerIds));
  }

  private void check(boolean val) {
    if (!val) {
      throw new IllegalArgumentException();
    }
  }
}
