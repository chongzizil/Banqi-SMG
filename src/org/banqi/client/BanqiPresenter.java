package org.banqi.client;

import java.util.List;

import org.game_api.GameApi.Container;
import org.game_api.GameApi.Operation;
import org.game_api.GameApi.Set;
import org.game_api.GameApi.SetTurn;
import org.game_api.GameApi.UpdateUI;




//import com.allen_sauer.gwt.dnd.client.DragHandler;
//import com.allen_sauer.gwt.dnd.client.DragHandlerAdapter;
//import com.allen_sauer.gwt.dnd.client.DragStartEvent;
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

  public interface Dropper {
    void onDrop(Position pos);
  }
  
  public interface View {
    /**
     * Sets the presenter. The viewer will call certain methods on the
     * presenter, e.g., when a piece is selected ({@link #pieceSelected}),
     * when a square is selected ({@link #squareSelected}).
     * 
     * * The process of turning up a piece looks as follows to the viewer:
     * 1) The viewer calls {@link #pieceSelected} one time to choose a facing-down piece
     * The process of turning up a piece looks as follows to the presenter:
     * 1) The presenter calls {@link #chooseNextPieceOrSquare} and passes the current selection.
     * 
     * * The process of moving a piece looks as follows to the viewer:
     * 1) The viewer calls {@link #pieceSelected} one time first to choose a piece
     * 2) The viewer calls {@link #squareSelected} one time to choose the square
     *  for the piece to move to.
     * The process of moving a piece looks as follows to the presenter:
     * 1) The presenter calls {@link #chooseNextPieceOrSquare} and passes the current selection.
     * 
     * * The process of capturing a piece looks as follows to the viewer:
     * 1) The viewer calls {@link #pieceSelected} one time first to choose the capturer piece
     * 2) The viewer calls {@link #pieceSelected} one more time to choose the captured piece
     * The process of capturing a piece looks as follows to the presenter:
     * 1) The presenter calls {@link #chooseNextPieceOrSquare} and passes the current selection.
     */
    void setPresenter(BanqiPresenter banqiPresenter);

    /** Sets the state for a viewer, i.e., not one of the players. */
    void setViewerState(List<Integer> squares, List<Piece> pieces);

    /** Sets the state for a player (whether the player has the turn or not). */
    void setPlayerState(List<Integer> squares, List<Piece> pieces);
    
    void animateMove(List<Optional<Integer>> squares, List<Optional<Piece>> pieces,
        int startCoord, int endCoord, boolean isMove, boolean isCapture);
    
    /** Initialize the behavior of a widget being dragged. */
    //DragHandler initializeDragHandler();

    /** Initialize the behavior of a widget bieng dropped. */
    //Dropper initializeDropHandler();
    
    /** Find the board position associated with an image. */
    //Position getPosition(Image image);
    
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
    void chooseNextPieceOrSquare(List<Integer> squares, List<Piece> pieces,
        List<Integer> selectedPieceIds);
  }

  private final BanqiLogic banqiLogic = new BanqiLogic();
  private final View view;
  private final Container container;
  /** A viewer doesn't have a color. */
  private Optional<Color> myColor;
  private State banqiState;
  private boolean isDnd;
  private List<Integer> selectedPieces;
  private static final String MOVEPIECE = "movePiece"; // A move has the form: [from, to]
  private static final String TURNPIECE = "turnPiece"; // A turn has the form: [coordinate]
  private static final String CAPTUREPIECE = "capturePiece"; // A capture has the form: [from, to]
  
  //private final StateExplorerImpl stateExplorer = new StateExplorerImpl();
  //private Position moveFrom = null;
  //private Position moveTo = null;
  //private java.util.Set<MovePiece> possibleMoves;

  public BanqiPresenter(View view, Container container) {
    this.view = view;
    this.container = container;
    view.setPresenter(this);
  }

  /** Updates the presenter and the view with the state in updateUI. */
  public void updateUI(UpdateUI updateUI) {
    List<String> playerIds = updateUI.getPlayerIds();
    String yourPlayerId = updateUI.getYourPlayerId();
    int yourPlayerIndex = updateUI.getPlayerIndex(yourPlayerId);
    myColor = yourPlayerIndex == 0 ? Optional.of(Color.R)
        : yourPlayerIndex == 1 ? Optional.of(Color.B) : Optional
            .<Color> absent();

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
      view.setViewerState(getAllSquares(banqiState), getAllPieces(banqiState));
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
    view.setPlayerState(getAllSquares(banqiState), getAllPieces(banqiState));
    if (isMyTurn()) {
      if ((hasBlack ^ hasRed) && !hasFacingDownPieces) { // The game is over
        endGame();
      } else {
        // Choose the piece only if the game is not over
        chooseNextPieceOrSquare();
      }
    }
  }
  
  /** Get all sqaures from the state. */
  List<Integer> getAllSquares(State banqiState) {
    ImmutableList<Optional<Integer>> squares = banqiState.getSquares();
    List<Integer> square = Lists.newArrayList();
    for (int i = 0; i < 32; i++) {
      if (squares.get(i).isPresent()) {
        square.add(squares.get(i).get());
      } else {
        square.add(null);
      }
    }
    return square;
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
    view.chooseNextPieceOrSquare(getAllSquares(banqiState), getAllPieces(banqiState),
        ImmutableList.copyOf(selectedPieces));
  }

  /**
   * Adds/remove the piece from the {@link #selectedPieces}. The view can only
   * call this method if the presenter called {@link View#chooseNextPiece}.
   */
  public void pieceSelected(int pieceId, boolean isDnd) {
    this.isDnd = isDnd;
    check(isMyTurn());
    List<Optional<Piece>> pieces = banqiState.getPieces();
    List<Optional<Integer>> squares = banqiState.getSquares();
    // Turn up a piece
    if (!pieces.get(pieceId).isPresent()) {
      if (selectedPieces.size() == 0) {
        int selectedCoord = squares.indexOf(Optional.fromNullable(pieceId));
      Set turnPiece = new Set(TURNPIECE, "S" + selectedCoord);
      //****************** make a move ******************//
      if (!isDnd) {
        view.animateMove(squares, pieces, selectedCoord,
            selectedCoord, false, false);
      }
      turnPiece(turnPiece);
      }   
    }
     
    if (pieces.get(pieceId).isPresent()) {
      if (selectedPieces.contains(pieceId)) {
        selectedPieces.remove((Integer) pieceId);
        chooseNextPieceOrSquare();
      } else if (selectedPieces.size() == 0) {
        if (pieces.get(pieceId).get().getColor().name().substring(0, 1).equals(
            myColor.get().name())) {
          selectedPieces.add(pieceId);
        }
        chooseNextPieceOrSquare();
      } else if (selectedPieces.size() == 1) {
        int selectedFromCoord = squares.indexOf(Optional.fromNullable(selectedPieces.get(0)));
        int selectedToCoord = squares.indexOf(Optional.fromNullable(pieceId));
        // Check the logic to see if the capturee is legal to be chosen
        if (banqiLogic.canCapture(pieces, squares, selectedFromCoord, selectedToCoord)) {
          Set capturePiece = new Set(CAPTUREPIECE, ImmutableList.of("S"
              + selectedFromCoord, "S" + selectedToCoord));
          //****************** make a move ******************//
          if (!isDnd) {
            view.animateMove(squares, pieces, selectedFromCoord,
                selectedToCoord, false, true);
          }
          capturePiece(capturePiece);
        }
      }
    }
  }

  /**
   * Adds/remove the square from the {@link #selectedSquares}. The view can only call
   * this method if the presenter called {@link View#chooseSquare}.
   */
  public void squareSelected(int square, boolean isDnd) {
    this.isDnd = isDnd;
    check(isMyTurn());
    // Need to select a piece first to perform a move, otherwise need to reselect
    if (selectedPieces.size() == 1) {
      List<Optional<Integer>> squares = banqiState.getSquares();
      List<Optional<Piece>> pieces = banqiState.getPieces();
      int selectedFromCoord = squares.indexOf(Optional.fromNullable(selectedPieces.get(0)));
      int selectedToCoord = square;
      // Only perform the move when selected a legal square, otherwise need to reselect
      if (banqiLogic.isMoveCoordLegal(selectedFromCoord, selectedToCoord)) {
        Set movePiece = new Set(MOVEPIECE, ImmutableList.of("S"
            + selectedFromCoord, "S" + selectedToCoord));
        //****************** make a move ******************//
        if (!isDnd) {
          view.animateMove(squares, pieces, selectedFromCoord,
            selectedToCoord, true, false);
        }
        movePiece(movePiece);
      } else { // Reselect
        chooseNextPieceOrSquare();
      }
    } else { // Reselect
      chooseNextPieceOrSquare();
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

  public State getState() {
    return this.banqiState;
  }
  
  public Color getMyColor() {
    // Is a player
    if (this.myColor.isPresent()) {
      return this.myColor.get();
    } else {
      // Is a viewer
      return null;
    }
  }
  
  private void sendInitialMove(List<String> playerIds) {
    container.sendMakeMove(banqiLogic.getMoveInitial(playerIds));
  }
  
  private void check(boolean val) {
    if (!val) {
      throw new IllegalArgumentException();
    }
  }
}
