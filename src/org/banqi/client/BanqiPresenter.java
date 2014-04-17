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
     * Sets the presenter.
     * The viewer will call certain methods on the presenter
     * e.g., when a cell or the piece within it is selected ({@link #cellSelected})
     * 
     * * The process of turning up a piece looks as follows to the viewer:
     * 1) The viewer calls {@link #cellSelected} one time to choose a facing-down
     * piece. 
     * * The process of turning up a piece looks as follows to the presenter:
     * 1) The presenter calls {@link #chooseNextCell} and passes the current selection
     * 
     * * The process of moving a piece looks as follows to the viewer:
     * 1) The viewer calls {@link #cellSelected} one time first to choose a piece
     * 2) The viewer calls {@link #cellSelected} one time to choose the empty cell
     * for the piece to move to.
     * * The process of moving a piece looks as follows to the presenter:
     * 1) The presenter calls {@link #chooseNextCell} and passes the current selection
     * 
     * * The process of capturing a piece looks as follows to the viewer:
     * 1) The viewer calls {@link #cellSelected} one time first to choose the capturer
     * 2) The viewer calls {@link #cellSelected} one more time to choose the captured
     * * The process of capturing a piece looks as follows to the presenter:
     * 1) The presenter calls {@link #chooseNextCell} and passes the current selection.
     */
    void setPresenter(BanqiPresenter banqiPresenter);

    /** Sets the state for a viewer, i.e., not one of the players. */
    void setViewerState(List<Piece> cells);

    /** Sets the state for a player (whether the player has the turn or not). */
    void setPlayerState(List<Piece> cells);

    /** Set the sound of the move. */
//    void makeSound(boolean isCapture);

    /** Set the animation of the move. */
    void animateMove(List<Optional<Piece>> cells,
        int startCoord, int endCoord, boolean isCapture, boolean isDnd);

    /** Initialize the behavior of a widget being dragged. */
    // DragHandler initializeDragHandler();

    /** Initialize the behavior of a widget bieng dropped. */
    // Dropper initializeDropHandler();

    /** Find the board position associated with an image. */
    // Position getPosition(Image image);

    /**
     * Asks the player to choose the next piece or empty cell. We pass what
     * are selected (e.g., select a facing up piece one time and a
     * empty cell one time in order to perform a move).
     * 
     * The user can either select a piece or followed with another piece or
     * a empty cell by calling {@link #cellSelected};
     * 
     * If the user selects a cell from selectedCell, then it's removed from selectedCell.
     */
    void chooseNextCell(List<Piece> cells, List<Integer> selectedCells);
  }

  private final BanqiLogic banqiLogic = new BanqiLogic();
  private final View view;
  private final Container container;
  /** A viewer doesn't have a color. */
  private Optional<Color> myColor;
  private BanqiState banqiState;
  private List<Integer> selectedCells;
  // A move has the form: [from, to]
  private static final String MOVEPIECE = "movePiece";
  //A turn has the form: [coordinate]
  private static final String TURNPIECE = "turnPiece"; 
  //A capture has the form: [from, to]
  private static final String CAPTUREPIECE = "capturePiece";
  
  public int fromCellIndex;
  
  public int getFromCellIndex() {
    return fromCellIndex;
  }
  
  public void setFromCellIndex(int fromCellIndex) {
    this.fromCellIndex = fromCellIndex;
  }

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

    selectedCells = Lists.newArrayList();

    if (updateUI.getState().isEmpty()) {
      // The R player sends the initial setup move.
      if (myColor.isPresent() && myColor.get().isRed()) {
        sendInitialMove(playerIds);
      }
      return;
    }

    Color turnOfColor = null;
    // The order of operations: turn, movePiece, turnPiece, capturePiece,
    // C0...C31
    for (Operation operation : updateUI.getLastMove()) {
      if (operation instanceof SetTurn) {
        turnOfColor = Color.values()[playerIds.indexOf(((SetTurn) operation)
            .getPlayerId())];
      }
    }
    banqiState = banqiLogic.gameApiStateToBanqiState(updateUI.getState(),
        turnOfColor, playerIds);

    if (updateUI.isViewer()) {
      view.setViewerState(getAllCells(banqiState));
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
    view.setPlayerState(getAllCells(banqiState));
    if (isMyTurn()) {
      if ((hasBlack ^ hasRed) && !hasFacingDownPieces) { // The game is over
        endGame();
      } else {
        // Choose the piece only if the game is not over
        chooseNextCell();
      }
    }
  }

  /** Get all cells from the state. */
  List<Piece> getAllCells(BanqiState banqiState) {
    ImmutableList<Optional<Piece>> cells = banqiState.getCells();
    List<Piece> newCells = Lists.newArrayList();
    for (int i = 0; i < 32; i++) {
      if (cells.get(i).isPresent()) {
        newCells.add(cells.get(i).get());
      } else {
        newCells.add(null);
      }
    }
    return newCells;
  }

  private boolean isMyTurn() {
    return myColor.isPresent() && myColor.get() == banqiState.getTurn();
  }

  private void chooseNextCell() {
    view.chooseNextCell(getAllCells(banqiState),
        ImmutableList.copyOf(selectedCells));
  }

  /**
   * Adds/remove the piece from the {@link #selectedCells}. The view can only
   * call this method if the presenter called {@link View#chooseNextCell}.
   */
  public void cellSelected(int cellIndex, boolean isDnd) {
    check(isMyTurn());
    List<Optional<Piece>> cells = banqiState.getCells();

    if (selectedCells.size() == 0) {
      // The piece in the cell is facing down
      if (!cells.get(cellIndex).isPresent()) {
        int selectedCoord = cellIndex;
        Set turnPiece = new Set(TURNPIECE, "C" + selectedCoord);
        // ****************** make a move ******************//
        view.animateMove(cells, selectedCoord, selectedCoord, false, isDnd);
        turnPiece(turnPiece);
      } else if (cells.get(cellIndex).get().getKind() != Piece.Kind.EMPTY) {
        // There exists a piece in the cell
        if (cells.get(cellIndex).get().getPieceColor().name().substring(0, 1)
            .equals(myColor.get().name())) {
          selectedCells.add(cellIndex);
          chooseNextCell();
        }
      } else {
        chooseNextCell();
      }
    } else if (selectedCells.size() == 1) {
      if (cells.get(cellIndex).isPresent()) {
        if (cells.get(cellIndex).get().getKind() == Piece.Kind.EMPTY) {
          int selectedFromCoord = selectedCells.get(0);
          int selectedToCoord = cellIndex;
          // Only perform the move when selected a legal square, otherwise need
          // to
          // reselect
          if (banqiLogic.isMoveCoordLegal(selectedFromCoord, selectedToCoord)) {
            Set movePiece = new Set(MOVEPIECE, ImmutableList.of("C"
                + selectedFromCoord, "C" + selectedToCoord));
            // ****************** make a move ******************//
            view.animateMove(cells, selectedFromCoord, selectedToCoord,
                false, isDnd);
            movePiece(movePiece);
          } else {
            chooseNextCell();
          }
        } else {
          if (selectedCells.contains(cellIndex)) {
            selectedCells.remove((Integer) cellIndex);
            chooseNextCell();
          } else {
            // A piece is selected
            int selectedFromCoord = selectedCells.get(0);
            int selectedToCoord = cellIndex;
            // Check the logic to see if the capturee is legal to be chosen
            if (banqiLogic
                .canCapture(cells, selectedFromCoord, selectedToCoord)) {
              Set capturePiece = new Set(CAPTUREPIECE, ImmutableList.of("C"
                  + selectedFromCoord, "C" + selectedToCoord));
              // ****************** make a move ******************//
              view.animateMove(cells, selectedFromCoord, selectedToCoord,
                  true, isDnd);
              capturePiece(capturePiece);
            } else {
              chooseNextCell();
            }
          }
        }
      } else {
        chooseNextCell();
      }
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

  public BanqiState getState() {
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
