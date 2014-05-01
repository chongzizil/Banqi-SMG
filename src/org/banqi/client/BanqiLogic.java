package org.banqi.client;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.banqi.client.Piece.Kind;
import org.banqi.client.Piece.PieceColor;
import org.game_api.GameApi.Operation;
import org.game_api.GameApi.Set;
import org.game_api.GameApi.SetTurn;
import org.game_api.GameApi.SetVisibility;
import org.game_api.GameApi.Shuffle;
import org.game_api.GameApi.VerifyMove;
import org.game_api.GameApi.VerifyMoveDone;
import org.game_api.GameApi.EndGame;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class BanqiLogic {

  private static final String C = "C";
  private static final List<Integer> INVISIBLE = new ArrayList<Integer>();
  private static final String EMPTYCELL = "eemp";
  private static final String TURNPIECE = "turnPiece";
  private static final String MOVEPIECE = "movePiece";
  private static final String CAPTUREPIECE = "capturePiece";
  private static final Piece NULL = null;
  private static final Boolean INSAMEROW = true;
  
  public VerifyMoveDone verify(VerifyMove verifyMove) {
    try {
      checkMoveIsLegal(verifyMove);
      return new VerifyMoveDone();
    } catch (Exception e) {
      return new VerifyMoveDone(verifyMove.getLastMovePlayerId(), e.getMessage());
    }
  }
  
  void checkMoveIsLegal(VerifyMove verifyMove) {
    List<Operation> lastMove = verifyMove.getLastMove();
    Map<String, Object> lastState = verifyMove.getLastState();
    // Checking the operations are as expected.
    List<Operation> expectedOperations = getExpectedOperations(
        lastState, lastMove, verifyMove.getPlayerIds(), verifyMove.getLastMovePlayerId());
    check(expectedOperations.equals(lastMove), expectedOperations, lastMove);
    // We use SetTurn, so we don't need to check that the correct player did the move.
    // However, we do need to check the first move is done by the red player (and then in the
    // first MakeMove we'll send SetTurn which will guarantee the correct player send MakeMove).
    if (lastState.isEmpty()) {
      check(verifyMove.getLastMovePlayerId() == verifyMove.getPlayerIds().get(0));
    }
  }

  /** Check the coordinate is legal. */
  boolean isCoordinateLegal(int coord) {
    if (coord >= 0 && coord < 32) {
      return true;
    }
    return false;
  }
  
  /**
   * Check the two coordinates are near each other so that the move is legal.
   * Notice that the capture move by cannon is an exception, therefore the
   * corresponding coordinates are not checked by this method.
   **/
  boolean isMoveCoordLegal(int from, int to) {
    //Transfer to two digits representation (e.g., 11 indicates row 1 column 1)
    int fromCoord = (from / 8 + 1) * 10 + (from % 8 + 1);
    int toCoord = (to / 8 + 1) * 10 + (to % 8 + 1);
    switch(fromCoord - toCoord) {
    case  1 :
    case -1 :
    case  10:
    case -10: return true; // Legal move
    default: return false; // Illegal move
    }
  }
  
  /**
   * Calculate the number of pieces on the board from the capturer inclusive to
   * the captured exclusive.
   **/
  int computeIntermediatePieceCount(List<Optional<Piece>> cells,
      int fromCoord,
      int toCoord,
      boolean inSameRow) {
    int intermediatePieceCount = 0;
    int incr = inSameRow ? 1 : 8;
    incr = fromCoord < toCoord ? incr : -incr;
    
    for (int i = fromCoord; i != toCoord; i += incr) {
      if (!cells.get(i).isPresent()
          || (cells.get(i).isPresent()
              && cells.get(i).get().getKind() != Piece.Kind.EMPTY)) {
        intermediatePieceCount++;
      }
    }

    return intermediatePieceCount;
  }
  
  /** Check the cannon can do the capture move. */
  boolean canCannonCapture(List<Optional<Piece>> cells,
      int fromCoord,
      int toCoord) {
    
    int intermediatePieceCount = 0;
    
    //Both pieces are in the same row
    if (fromCoord / 8 == toCoord / 8) {
      intermediatePieceCount = 
          computeIntermediatePieceCount(cells, fromCoord, toCoord, INSAMEROW);
    } else { //Both pieces are in the same column
      intermediatePieceCount = 
          computeIntermediatePieceCount(cells, fromCoord, toCoord, !INSAMEROW);
    }
    
    //check(intermediatePieceCount == 2, intermediatePieceCount);
    if (intermediatePieceCount != 2) {
      return false;
    }
    return true;
  }
  
  /** Check the piece can do the capture move. */
  boolean canCapture(List<Optional<Piece>> cells, int fromCoord, int toCoord) {

    Piece fromPiece = cells.get(fromCoord).get();
    Piece toPiece = cells.get(toCoord).get();

    if (fromPiece.getKind() == Piece.Kind.SOLDIER
        && toPiece.getKind() == Piece.Kind.GENERAL) {
      return isMoveCoordLegal(fromCoord, toCoord);
    } else if (fromPiece.getKind() == Piece.Kind.GENERAL
        && toPiece.getKind() == Piece.Kind.SOLDIER) {
      // A general can not capture a soldier
      return false;
    } else if (fromPiece.getKind() == Piece.Kind.CANNON) {
      return canCannonCapture(cells, fromCoord, toCoord);
    } else {
      check(isMoveCoordLegal(fromCoord, toCoord));
      if (fromPiece.getKind() != toPiece.getKind()) {
        return (fromPiece.getKind().ordinal() < toPiece.getKind().ordinal());
      } else {
        return true;
      }
    }
  }

  /** Returns the operations for moving a piece (e.g., I move a piece from S0 to S1). */
  List<Operation> getMovePieceOperation(BanqiState state, Set move) {
    ImmutableList<Optional<Piece>> cells = state.getCells();
    Color turnOfColor = state.getTurn();
    
    @SuppressWarnings("unchecked")
    List<String> coord = (List<String>) move.getValue();
    
    //Check if there are exactly 2 legal coordinates and the move is legal.
    check(coord.size() == 2, "Coord size is illegal: " + coord.size());
    
    checkNotNull(coord.get(0));
    int fromCoord = Integer.parseInt(coord.get(0).substring(1));
    check(isCoordinateLegal(fromCoord), "FromCoord is illegal: " + fromCoord);

    checkNotNull(coord.get(1));
    int toCoord = Integer.parseInt(coord.get(1).substring(1));
    check(isCoordinateLegal(toCoord), "ToCoord is illegal: " + toCoord);

    check(isMoveCoordLegal(fromCoord, toCoord), "Move Coord is illegal: "
        + fromCoord + " " + toCoord);

    // Check there's a piece on the from square and no piece on the to square
    check(cells.get(fromCoord).isPresent()
        && cells.get(fromCoord).get().getKind() != Piece.Kind.EMPTY,
        "No piece in from cell: " + cells.get(fromCoord).get());
    check(cells.get(toCoord).isPresent()
        && cells.get(toCoord).get().getKind() == Piece.Kind.EMPTY,
        "Move destination cell contains other piece: " + cells.get(toCoord).get());
    
    
    // Check the color is as same as the last move's player color
    Piece fromPiece = cells.get(fromCoord).get();
    check(fromPiece.getPieceColor().name().substring(0, 1).equals(turnOfColor.toString()),
        "Wrong piece color: " + fromPiece.getPieceColor());
    
    List<Operation> expectedOperations;
    expectedOperations = Lists.newArrayList();
    expectedOperations.add(new SetTurn(state.getPlayerId(turnOfColor.getOppositeColor())));
    expectedOperations.add(new Set(MOVEPIECE, ImmutableList.of(C + fromCoord, C + toCoord)));
    expectedOperations.add(new Set("C" + fromCoord, EMPTYCELL));
    expectedOperations.add(new Set("C" + toCoord, fromPiece.getPieceFourLetterString())); 
    return expectedOperations; 
  }
  
  /** Returns the operations for turning a piece (e.g., I turn a piece up at S0). */
  List<Operation> getTurnPieceOperation(BanqiState state, Set move) {
    ImmutableList<Optional<Piece>> cells = state.getCells();
    Color turnOfColor = state.getTurn();
    
    String coord = (String) move.getValue();

    int fromCoord = Integer.parseInt(coord.substring(1));
    check(isCoordinateLegal(fromCoord), "FromCoord is illegal: " + fromCoord);
    
    // Check there's a piece on the turn square
    check(!cells.get(fromCoord).isPresent(),
        "No face down piece in the cell: " + cells.get(fromCoord));
    
    List<Operation> expectedOperations;
    expectedOperations = Lists.newArrayList();
    expectedOperations.add(new SetTurn(state.getPlayerId(turnOfColor.getOppositeColor())));
    expectedOperations.add(new Set(TURNPIECE, C + fromCoord));
    expectedOperations.add(new SetVisibility(C + fromCoord));    
    return expectedOperations;
  }
  
  /** Returns the operations for capturing a piece (e.g., I use P0 at S0 to capture P1 at S1). */
  List<Operation> getCapturePieceOperation(BanqiState state, Set move) {
    ImmutableList<Optional<Piece>> cells = state.getCells();
    Color turnOfColor = state.getTurn();
    
    @SuppressWarnings("unchecked")
    List<String> coord = (List<String>) move.getValue();
    
    //Check if there are exactly 2 legal coordinates.
    check(coord.size() == 2, coord.size());
    
    checkNotNull(coord.get(0));
    int fromCoord = Integer.parseInt(coord.get(0).substring(1));
    check(isCoordinateLegal(fromCoord), "FromCoord is illegal: " + fromCoord);
    
    checkNotNull(coord.get(1));
    int toCoord = Integer.parseInt(coord.get(1).substring(1));
    check(isCoordinateLegal(toCoord), "ToCoord is illegal: " + toCoord);
    
    // Check there's a piece in the from cell and a piece in the to cell and both
    // of them are facing up
    check(cells.get(fromCoord).isPresent()
        && cells.get(fromCoord).get().getKind() != Piece.Kind.EMPTY,
        "No piece in from cell: " + cells.get(fromCoord).get());
    check(cells.get(toCoord).isPresent()
        && cells.get(toCoord).get().getKind() != Piece.Kind.EMPTY,
        "No piece in from cell: " + cells.get(toCoord).get());
    
    // Check the color of the from piece is as same as the last move's player color
    Piece fromPiece = cells.get(fromCoord).get();
    check(fromPiece.getPieceColor().name().substring(0, 1).equals(turnOfColor.toString()),
        "Wrong from piece color: " + fromPiece.getPieceColor());
    
    // Check the color of the to piece is opposite to the last move's player color
    Piece toPiece = cells.get(toCoord).get();
    check(!toPiece.getPieceColor().name().substring(0, 1).equals(turnOfColor.toString()),
        "Wrong to piece color: " + fromPiece.getPieceColor());
    
    // Check if the capture move is valid
    check(canCapture(cells, fromCoord, toCoord));
    
    List<Operation> expectedOperations = Lists.newArrayList();
    expectedOperations.add(new SetTurn(state.getPlayerId(turnOfColor.getOppositeColor())));
    expectedOperations.add(new Set(CAPTUREPIECE, ImmutableList.of(C + fromCoord, C + toCoord)));
    expectedOperations.add(new Set(C + fromCoord, EMPTYCELL));
    expectedOperations.add(new Set(C + toCoord, fromPiece.getPieceFourLetterString())); 
    return expectedOperations;
  }
  
  /** Return the winner color if the game ends properly. */
  Color whoWinTheGame(BanqiState state) {
    boolean hasBlack = state.hasRedOrBlackPieces(Color.B);
    boolean hasRed = state.hasRedOrBlackPieces(Color.R);
    //Returning the winner's color
    if (state.hasFacingDownPiece()) {
      return null;
    } else if (hasBlack && !hasRed) {
      return Color.B;
    } else if (!hasBlack && hasRed) {
      return Color.R;
    } else {
      return null;
    }
  }
  
  /** Returns the operations for ending the game. */
  List<Operation> getEndGameOperation(BanqiState state) {
    List<Operation> expectedOperations = Lists.newArrayList();
    Color turnOfColor = state.getTurn();
    
    Color winnerColor = whoWinTheGame(state);
    check(winnerColor != null);
    if (winnerColor == turnOfColor) {
      expectedOperations.add(new SetTurn(state.getPlayerId(winnerColor.getOppositeColor())));
      expectedOperations.add(new EndGame(state.getPlayerId(winnerColor)));
    } else if (winnerColor == turnOfColor.getOppositeColor()) {
      expectedOperations.add(new SetTurn(state.getPlayerId(winnerColor.getOppositeColor())));
      expectedOperations.add(new EndGame(state.getPlayerId(winnerColor)));
    }
    return expectedOperations;
  }
  
  /**
   * Returns the expected move, which is one of:
   * getInitialMove, getMovePieceOperation, getTurnPieceOperation,
   * getCapturePieceOperation, getEndGameOperation.
   */
  List<Operation> getExpectedOperations(Map<String, Object> lastApiState,
      List<Operation> lastMove,
      List<String> playerIds,
      String lastMovePlayerId) {
    
    if (lastApiState.isEmpty()) {
      return getMoveInitial(playerIds);
    }
    
    BanqiState lastState = gameApiStateToBanqiState(lastApiState,
        Color.values()[playerIds.indexOf(lastMovePlayerId)],
        playerIds);
    
    List<Operation> expectedOperations = Lists.newArrayList();
    /*
     * 
     * There are 3 types of moves:
     * 1) Moving a face-up piece of his/her own color.
     * 2) Turning up a face-down piece.
     * 3) Capturing a face-down piece of his/her own color.
     * 
     * If the last move turned up the last facing down piece, or captured
     * a piece on the board and hence all the pieces on the board have
     * the same color, then the game will be ended and the winner is decided.
     * 
     */
    if (lastMove.get(1) instanceof Set) {
      Set move = (Set) lastMove.get(1);
      if (move.getKey() == MOVEPIECE) {
        //Moving a piece
        expectedOperations = getMovePieceOperation(lastState, move);
      } else if (move.getKey() == TURNPIECE) {
        //Turning a piece
        expectedOperations = getTurnPieceOperation(lastState, move);
      } else if (move.getKey() == CAPTUREPIECE) {
        //Moving a piece
        expectedOperations = getCapturePieceOperation(lastState, move);
      }
    } else {
      //Ending the game
      expectedOperations = getEndGameOperation(lastState);
    }

    return expectedOperations;
  }
  
  public List<String> getCellsKey() {
    List<String> keys = Lists.newArrayList();
    for (int i = 0; i < 32; i++) {
      keys.add(C + i);
    }
    return keys;
  }

  /** Transform the pieceId to String (e.g., 0 to "rgen"). */
  String pieceIdToString(int pieceId) {
    checkArgument(pieceId >= 0 && pieceId < 32);
    //colorId = 0 : Red, colorId = 1 : Black
    int colorId = pieceId / 16;
    String colorString = PieceColor.values()[colorId].getFirstLetterLowerCase();
    int kindId = pieceId % 16 == 0 ? 0 //GENERAL
      : pieceId % 16 < 3 ? 1 //ADVISOR
      : pieceId % 16 < 5 ? 2 //ELEPHANT
      : pieceId % 16 < 7 ? 3 //CHARIOT
      : pieceId % 16 < 9 ? 4 //HORSE
      : pieceId % 16 < 11 ? 5 //CANNON
      : 6; //SOLDIER
    String kindString = Kind.values()[kindId].getFirstThreeLetterLowerCase();
    return colorString + kindString;
  }

  /**
   * Set the initial move operation.
   */
  List<Operation> getMoveInitial(List<String> playerIds) {
    String redPlayerId = playerIds.get(0);
    List<Operation> operations = Lists.newArrayList();
    
    //The order of operations: turn, movePiece, turnPiece, capturePiece, C0...C31
    operations.add(new SetTurn(redPlayerId));
    
    //Set the pieces
    for (int i = 0; i < 32; i++) {
      operations.add(new Set(C + i, pieceIdToString(i)));
    }
    
    //Shuffle the pieces
    operations.add(new Shuffle(getCellsKey()));
    
    //Set the visibility of all pieces
    for (int i = 0; i < 32; i++) {
      operations.add(new SetVisibility(C + i, INVISIBLE));
    }
    
    return operations;
  }
  
  /**
   * Convert the gameAPI state to Banqi State.
   */
  BanqiState gameApiStateToBanqiState(Map<String, Object> gameApiState,
      Color turnOfColor, List<String> playerIds) {
    List<Optional<Piece>> cells = Lists.newArrayList();
   
    for (int i = 0; i < 32; i++) {
      String pieceString = (String) gameApiState.get(C + i);
      if (pieceString != null) {
        PieceColor color = PieceColor.fromFirstLetterLowerCase(pieceString.substring(0, 1));
        Kind kind = Kind.fromFirstThreeLetterLowerCase(pieceString.substring(1, 4));
        cells.add(Optional.fromNullable(new Piece(kind, color)));
      } else {
        cells.add(Optional.fromNullable(NULL));
      }
    }
    
    return new BanqiState(
        turnOfColor,
        ImmutableList.copyOf(playerIds),
        ImmutableList.copyOf(cells)
    );
  }
  
  private void check(boolean val, Object... debugArguments) {
    if (!val) {
      throw new RuntimeException("We have a hacker! debugArguments="
          + Arrays.toString(debugArguments));
    }
  }

}