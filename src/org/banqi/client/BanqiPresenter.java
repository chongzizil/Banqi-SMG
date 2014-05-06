package org.banqi.client;

import java.util.List;

import org.banqi.ai.AlphaBetaPruning;
import org.banqi.ai.DateTimer;
import org.banqi.ai.Heuristic;
import org.game_api.GameApi.Container;
import org.game_api.GameApi.Operation;
import org.game_api.GameApi.Set;
import org.game_api.GameApi.SetTurn;
import org.game_api.GameApi.UpdateUI;

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

    /** Set the argument of the animation. */
    void setAnimateArgs(List<Optional<Piece>> cells,
        int startCoord, int endCoord,
        boolean isCapture, boolean isDnd);
    
    /** Set the animation of the move. */
    void animateMove(List<Optional<Piece>> cells,
        int startCoord, int endCoord, boolean isCapture, boolean isDnd);

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
  boolean hasAiMakeMove = false;
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

    // Set the view
    view.setPlayerState(getAllCells(banqiState));
    
    if (updateUI.isViewer()) {
      return;
    }

    if (updateUI.isAiPlayer()) {
      if (!hasAiMakeMove) {
        hasAiMakeMove = true;
        if (banqiState.hasGameEnded()) {
          // The game is over, send the endGame operation
          endGame();
        } else {
          // The game is not over, make the move :)
          Heuristic heuristic = new Heuristic();
          AlphaBetaPruning ai = new AlphaBetaPruning(heuristic, banqiState);
          
          // The move of the AI takes at most 0.8 second
          DateTimer timer = new DateTimer(800);
          
          // The depth is 50 though due to the time limit, it may not reach that deep
          Move move = ai.findBestMove(50, timer);
          
          // AI make the move.
          int selectedFromCoord = ((move.getFrom().getRow() - 1) * 8 + move.getFrom().getCol()) - 1;
          int selectedToCoord = ((move.getTo().getRow() - 1) * 8 + move.getTo().getCol()) - 1;
          if (move.getType() == Move.Type.CAPTURE) {
            
            Set capturePiece = new Set(CAPTUREPIECE, ImmutableList.of("C"
                + selectedFromCoord, "C" + selectedToCoord));
            view.setAnimateArgs(banqiState.getCells(), selectedFromCoord, selectedToCoord,
                true, false);
            capturePiece(capturePiece);
          } else if (move.getType() == Move.Type.MOVE) {
            
            Set movePiece = new Set(MOVEPIECE, ImmutableList.of("C"
                + selectedFromCoord, "C" + selectedToCoord));
            view.setAnimateArgs(banqiState.getCells(), selectedFromCoord, selectedToCoord,
                false, false);
            movePiece(movePiece);
          } else {
            
            Set turnPiece = new Set(TURNPIECE, "C" + selectedFromCoord);
            view.setAnimateArgs(banqiState.getCells(), selectedFromCoord, selectedFromCoord,
                false, false);
            turnPiece(turnPiece);
          }
        }
      } 
      return;
    }

    // Must be a player!
    if (isMyTurn()) {
      hasAiMakeMove = false;
      if (banqiState.hasGameEnded()) {
        // The game is over, send the endGame operation
        endGame();
      } else {
        // Choose the cell only if the game is not over
        chooseNextCell();
      }
    }
  }

  /** Get all cells from the state. */
  List<Piece> getAllCells(BanqiState banqiState) {
    List<Optional<Piece>> cells = banqiState.getCells();
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
   * Adds/remove the cell from the {@link #selectedCells}. The view can only
   * call this method if the presenter called {@link View#chooseNextCell}.
   */
  public void cellSelected(int cellIndex, boolean isDnd) {
    check(isMyTurn());
    // Get all cells
    List<Optional<Piece>> cells = banqiState.getCells();

    if (selectedCells.size() == 0) {
      // No cells are previously selected
      if (!cells.get(cellIndex).isPresent()) {
        // The cell contains a face down piece
        // Get the selected piece coordinate
        int selectedCoord = cellIndex;
        Set turnPiece = new Set(TURNPIECE, "C" + selectedCoord);
        // ************************* make move start *************************
        // Set the arguments for the animation
        view.setAnimateArgs(cells, selectedCoord, selectedCoord, false, isDnd);
        // Make the turnPiece move
        turnPiece(turnPiece);
        // ************************** make move end **************************
      } else if (cells.get(cellIndex).get().getKind() != Piece.Kind.EMPTY) {
        // The cell contains a face up piece
        if (cells.get(cellIndex).get().getPieceColor().name().substring(0, 1)
            .equals(myColor.get().name())) {
          // If the piece's color is as same as the player's, the cell contains
          // it is selected
          selectedCells.add(cellIndex);
          chooseNextCell();
        }
      } else {
        chooseNextCell();
      }
    } else if (selectedCells.size() == 1) {
      // One cell is already selected
      if (cells.get(cellIndex).isPresent()) {
        // The cell does not contain a face down piece
        if (cells.get(cellIndex).get().getKind() == Piece.Kind.EMPTY) {
          // The cell is empty, get the from and to coordinate
          int selectedFromCoord = selectedCells.get(0);
          int selectedToCoord = cellIndex;
          // Only perform movePiece when a legal cell is selected, otherwise
          // need
          // to reselect another cell
          if (banqiLogic.isMoveCoordLegal(selectedFromCoord, selectedToCoord)) {
            Set movePiece = new Set(MOVEPIECE, ImmutableList.of("C"
                + selectedFromCoord, "C" + selectedToCoord));
            // ************************* make move start *************************
            // Set the arguments for the animation
            view.setAnimateArgs(cells, selectedFromCoord, selectedToCoord,
                false, isDnd);
            // Make the movePiece move
            movePiece(movePiece);
            // ************************** make move end **************************
          } else {
            chooseNextCell();
          }
        } else {
          // The cell contains a face up piece
          if (selectedCells.contains(cellIndex)) {
            // The cell is already selected, so it will be unselected
            selectedCells.remove((Integer) cellIndex);
            chooseNextCell();
          } else {
            // The cell is selected, get the from and to coordinate
            int selectedFromCoord = selectedCells.get(0);
            int selectedToCoord = cellIndex;
            // Only perform capturePiece when a cell is legal to be chosen
            if (banqiLogic
                .canCapture(cells, selectedFromCoord, selectedToCoord)) {
              Set capturePiece = new Set(CAPTUREPIECE, ImmutableList.of("C"
                  + selectedFromCoord, "C" + selectedToCoord));
              // ************************* make move start *************************
              // Set the arguments for the animation
              view.setAnimateArgs(cells, selectedFromCoord, selectedToCoord,
                  true, isDnd);
              // Make the capturePiece move
              capturePiece(capturePiece);
              // ************************** make move end **************************
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
