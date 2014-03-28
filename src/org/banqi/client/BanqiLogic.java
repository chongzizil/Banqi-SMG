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
  /* The entries used in the banqi game are:
   *   movePiece, turnPiece, capturePiece, P0...P31, S0...S31
   *   When we send operations on these keys, it will always be in the above order.
   */

  private static final String S = "S"; //Square of board key (S0...S31)
  private static final String P = "P"; //Piece key (P0...P31)
  private static final List<Integer> INVISIBLE = new ArrayList<Integer>();
  private static final Integer NULL = null;
  private static final String MOVEPIECE = "movePiece"; //A move has the form: [from, to]
  private static final String TURNPIECE = "turnPiece"; //A turn has the form: [coordinate]
  private static final String CAPTUREPIECE = "capturePiece"; //A capture has the form: [from, to]
  
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
  int computeIntermediatePieceCount(List<Optional<Piece>> pieces,
      List<Optional<Integer>> squares,
      int fromCoord, int toCoord, boolean isSameRow) {
    int intermediatePieceCount = 0;
    int incr = isSameRow ? 1 : 8;
    incr = fromCoord < toCoord ? incr : -incr;
    
    for (int i = fromCoord; i != toCoord; i += incr) {
      if (squares.get(i).isPresent()) {
        intermediatePieceCount++;
      }
    }

    return intermediatePieceCount;
  }
  
  /** Check the cannon can do the capture move. */
  boolean canCannonCapture(List<Optional<Piece>> pieces,
      List<Optional<Integer>> squares, int fromCoord, int toCoord) {
    int intermediatePieceCount = 0;
    //Both pieces are in the same row
    if (fromCoord / 8 == toCoord / 8) {
      intermediatePieceCount = 
          computeIntermediatePieceCount(pieces, squares, fromCoord, toCoord, true);
    } else { //Both pieces are in the same column
      intermediatePieceCount = 
          computeIntermediatePieceCount(pieces, squares, fromCoord, toCoord, false);
    }
    
    check(intermediatePieceCount == 2, intermediatePieceCount);
    return true;
  }
  
  /** Check the cannon can do the capture move. */
  boolean canCapture(List<Optional<Piece>> pieces,
      List<Optional<Integer>> squares, int fromCoord, int toCoord) {
    int pieceFromId = squares.get(fromCoord).get();
    int pieceToId = squares.get(toCoord).get();
    Piece fromPiece = pieces.get(pieceFromId).get();
    Piece toPiece = pieces.get(pieceToId).get();
    
    if (fromPiece.getKind().name() == "SOLDIER" && toPiece.getKind().name() == "GENERAL") {
      return isMoveCoordLegal(fromCoord, toCoord);
    } else if (fromPiece.getKind().name() == "GENERAL" && toPiece.getKind().name() == "SOLDIER") {
      // A general can not capture a soldier
      return false;
    } else if (fromPiece.getKind().name() == "CANNON") {
      return canCannonCapture(pieces, squares, fromCoord, toCoord);
    } else {
      check(isMoveCoordLegal(fromCoord, toCoord));
      if (fromPiece.getKind().name() != toPiece.getKind().name()) {
        return (fromPiece.getKind().ordinal() < toPiece.getKind().ordinal());
      } else {
        return true;
      }
    }
  }

  
  /** Returns the operations for moving a piece (e.g., I move a piece from S0 to S1). */
  List<Operation> getMovePieceOperation(State state, Set move) {
    ImmutableList<Optional<Piece>> pieces = state.getPieces();
    ImmutableList<Optional<Integer>> squares = state.getSquares();
    Color turnOfColor = state.getTurn();
    
    @SuppressWarnings("unchecked")
    List<String> coord = (List<String>) move.getValue();
    
    //Check if there are exactly 2 legal coordinates and the move is legal.
    check(coord.size() == 2, coord.size());
    
    checkNotNull(coord.get(0));
    int fromCoord = Integer.parseInt(coord.get(0).substring(1));
    check(isCoordinateLegal(fromCoord), fromCoord);
    
    checkNotNull(coord.get(1));
    int toCoord = Integer.parseInt(coord.get(1).substring(1));
    check(isCoordinateLegal(toCoord), toCoord);
    
    check(isMoveCoordLegal(fromCoord, toCoord));
    
    // Check there's a piece on the from square and no piece on the to square
    check(squares.get(fromCoord).isPresent());
    check(!squares.get(toCoord).isPresent());
    
    // Check the piece on the from square is facing up
    int pieceId = squares.get(fromCoord).get();
    check(pieces.get(pieceId).isPresent());
    
    // Check the color is as same as the last move's player color
    Piece fromPiece = pieces.get(pieceId).get();
    check(fromPiece.getColor().name().substring(0, 1).equals(turnOfColor.toString()));
    
    List<Operation> expectedOperations;
    expectedOperations = Lists.newArrayList();
    expectedOperations.add(new SetTurn(state.getPlayerId(turnOfColor.getOppositeColor())));
    expectedOperations.add(new Set(MOVEPIECE, ImmutableList.of(S + fromCoord, S + toCoord)));
    expectedOperations.add(new Set("S" + fromCoord, null));
    expectedOperations.add(new Set("S" + toCoord, "P" + pieceId)); 
    return expectedOperations; 
  }
  
  /** Returns the operations for turning a piece (e.g., I turn a piece up at S0). */
  List<Operation> getTurnPieceOperation(State state, Set move) {
    ImmutableList<Optional<Piece>> pieces = state.getPieces();
    ImmutableList<Optional<Integer>> squares = state.getSquares();
    Color turnOfColor = state.getTurn();
    
    String coord = (String) move.getValue();

    int fromCoord = Integer.parseInt(coord.substring(1));
    check(isCoordinateLegal(fromCoord), fromCoord);
    
    // Check there's a piece on the turn square
    check(squares.get(fromCoord).isPresent());
    // Check the piece is facing down
    int pieceId = squares.get(fromCoord).get();
    check(!pieces.get(pieceId).isPresent());
    
    List<Operation> expectedOperations;
    expectedOperations = Lists.newArrayList();
    expectedOperations.add(new SetTurn(state.getPlayerId(turnOfColor.getOppositeColor())));
    expectedOperations.add(new Set(TURNPIECE, S + fromCoord));
    expectedOperations.add(new SetVisibility(P + pieceId));    
    return expectedOperations;
  }
  
  /** Returns the operations for capturing a piece (e.g., I use P0 at S0 to capture P1 at S1). */
  List<Operation> getCapturePieceOperation(State state, Set move) {
    ImmutableList<Optional<Piece>> pieces = state.getPieces();
    ImmutableList<Optional<Integer>> squares = state.getSquares();
    Color turnOfColor = state.getTurn();
    
    @SuppressWarnings("unchecked")
    List<String> coord = (List<String>) move.getValue();
    
    //Check if there are exactly 2 legal coordinates.
    check(coord.size() == 2, coord.size());
    
    checkNotNull(coord.get(0));
    int fromCoord = Integer.parseInt(coord.get(0).substring(1));
    check(isCoordinateLegal(fromCoord), fromCoord);
    
    checkNotNull(coord.get(1));
    int toCoord = Integer.parseInt(coord.get(1).substring(1));
    check(isCoordinateLegal(toCoord), toCoord);
    
    // Check there's a piece on the from square and a piece on the to square
    check(squares.get(fromCoord).isPresent());
    check(squares.get(toCoord).isPresent());
    
    // Check both the pieces is facing up
    int pieceFromId = squares.get(fromCoord).get();
    check(pieces.get(pieceFromId).isPresent());

    int pieceToId = squares.get(toCoord).get();
    check(pieces.get(pieceToId).isPresent());
    
    // Check the color of the from piece is as same as the last move's player color
    Piece fromPiece = pieces.get(pieceFromId).get();
    check(fromPiece.getColor().name().substring(0, 1).equals(turnOfColor.toString()));
    
    // Check the color of the to piece is opposite to the last move's player color
    Piece toPiece = pieces.get(pieceToId).get();
    check(!toPiece.getColor().name().substring(0, 1).equals(turnOfColor.toString()));
    
    // Check if the capture move is valid
    check(canCapture(pieces, squares, fromCoord, toCoord));
    
    List<Operation> expectedOperations = Lists.newArrayList();
    expectedOperations.add(new SetTurn(state.getPlayerId(turnOfColor.getOppositeColor())));
    expectedOperations.add(new Set(CAPTUREPIECE, ImmutableList.of(S + fromCoord, S + toCoord)));
    expectedOperations.add(new Set("S" + fromCoord, null));
    expectedOperations.add(new Set("S" + toCoord, "P" + pieceFromId)); 
    return expectedOperations;
  }
  
  /** Return the winner color if the game ends properly. */
  Color whoWinTheGame(State state) {
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
  List<Operation> getEndGameOperation(State state) {
    List<Operation> expectedOperations = Lists.newArrayList();
    Color turnOfColor = state.getTurn();
    
    Color winnerColor = whoWinTheGame(state);
    check(winnerColor != null);
    if (winnerColor == turnOfColor) {
      expectedOperations.add(new SetTurn(state.getPlayerId(winnerColor)));
      expectedOperations.add(new EndGame(state.getPlayerId(winnerColor)));
    } else if (winnerColor == turnOfColor.getOppositeColor()) {
      expectedOperations.add(new SetTurn(state.getPlayerId(winnerColor.getOppositeColor())));
      expectedOperations.add(new EndGame(state.getPlayerId(winnerColor.getOppositeColor())));
    }
    return expectedOperations;
  }
  
  /**
   * Returns the expected move, which is one of:
   * getInitialMove, getMovePieceOperation, getTurnPieceOperation,
   * getCapturePieceOperation, getEndGameOperation.
   */
  List<Operation> getExpectedOperations(
      Map<String, Object> lastApiState, List<Operation> lastMove, List<String> playerIds,
      String lastMovePlayerId) {
    
    if (lastApiState.isEmpty()) {
      return getMoveInitial(playerIds);
    }
    
    State lastState = gameApiStateToBanqiState(lastApiState,
        Color.values()[playerIds.indexOf(lastMovePlayerId)], playerIds);
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
  
  public List<String> getPiecesKeys() {
    List<String> keys = Lists.newArrayList();
    for (int i = 0; i < 32; i++) {
      keys.add(P + i);
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

  List<Operation> getMoveInitial(List<String> playerIds) {
    String redPlayerId = playerIds.get(0);
    List<Operation> operations = Lists.newArrayList();
    //The order of operations: turn, movePiece, turnPiece, capturePiece, P0...P31, S0...P31 
    operations.add(new SetTurn(redPlayerId));
    //Set the pieces
    for (int i = 0; i < 32; i++) {
      operations.add(new Set(P + i, pieceIdToString(i)));
    }
    //Set the board
    for (int i = 0; i < 32; i++) {
      operations.add(new Set(S + i, P + i));
    }
    //Shuffle the pieces
    operations.add(new Shuffle(getPiecesKeys()));
    //Set the visibility of all pieces
    for (int i = 0; i < 32; i++) {
      operations.add(new SetVisibility(P + i, INVISIBLE));
    }
    return operations;
  }
  
  State gameApiStateToBanqiState(Map<String, Object> gameApiState,
      Color turnOfColor, List<String> playerIds) {
    List<Optional<Piece>> pieces = Lists.newArrayList();
    List<Optional<Integer>> squares = Lists.newArrayList();
   
    for (int i = 0; i < 32; i++) {
      String pieceString = (String) gameApiState.get(P + i);
      Piece piece = null;
      if (pieceString != null) {
        PieceColor color = PieceColor.fromFirstLetterLowerCase(pieceString.substring(0, 1));
        Kind kind = Kind.fromFirstThreeLetterLowerCase(pieceString.substring(1, 4));
        piece = new Piece(color, kind);
      }
      pieces.add(Optional.fromNullable(piece));
    }
    
    for (int i = 0; i < 32; i++) {
      String pieceId = (String) gameApiState.get(S + i);
      if (pieceId == null) {
        squares.add(Optional.fromNullable(NULL));
      } else {
        squares.add(Optional.fromNullable(Integer.parseInt(pieceId.substring(1))));
      }
    }
    
    return new State(
        turnOfColor,
        ImmutableList.copyOf(playerIds),
        ImmutableList.copyOf(pieces), 
        ImmutableList.copyOf(squares)
    );
  }
  
  private void check(boolean val, Object... debugArguments) {
    if (!val) {
      throw new RuntimeException("We have a hacker! debugArguments="
          + Arrays.toString(debugArguments));
    }
  }

}