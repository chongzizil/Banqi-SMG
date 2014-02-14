package org.banqi.client;

import static com.google.common.base.Preconditions.checkArgument;
//import static com.google.common.base.Preconditions.checkNotNull;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.banqi.client.Piece.Kind;
import org.banqi.client.Piece.PieceColor;
import org.banqi.client.GameApi.Operation;
import org.banqi.client.GameApi.Set;
import org.banqi.client.GameApi.SetTurn;
import org.banqi.client.GameApi.SetVisibility;
import org.banqi.client.GameApi.Shuffle;
import org.banqi.client.GameApi.VerifyMove;
import org.banqi.client.GameApi.VerifyMoveDone;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class BanqiLogic {
  /* The entries used in the cheat game are:
   *   movePiece, turnPiece, capturePiece, P0...P31, S0...S31
   *   When we send operations on these keys, it will always be in the above order.
   */

  private static final String S = "S"; //Square of board key (S0...S31)
  private static final String P = "P"; //Piece key (P0...P31)
  private static final List<Integer> INVISIBLE = new ArrayList<Integer>();
  private static final String NULL = null;
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
    // However, we do need to check the first move is done by the white player (and then in the
    // first MakeMove we'll send SetTurn which will guarantee the correct player send MakeMove).
    if (lastState.isEmpty()) {
      check(verifyMove.getLastMovePlayerId() == verifyMove.getPlayerIds().get(0));
    }
  }

  /** Check the coordinate. */
  boolean checkCoord(int coord) {
    if (coord >= 0 && coord < 32) {
      return true;
    }
    return false;
  }
  
  /** Check the two coordinates are near each other. */
  int checkFromToCoord(int from, int to) {
    //Transfer to two digits representation where 11 indicates row 1 column 1
    int fromCoord = (from / 8 + 1) * 10 + (from % 8 + 1);
    int toCoord = (to / 8 + 1) * 10 + (to % 8 + 1);
    switch(fromCoord - toCoord) {
    case  1 :
    case -1 :
    case  10:
    case -10: return 1;
    default: return 0; // Illegal move
    }
  }
  
  boolean cannonCaptureCheck(ImmutableList<Optional<Piece>> pieces,
      ImmutableList<Optional<String>> squares, int fromCoord, int toCoord) {
    int intermediatePieceCount = 0;
    //In the same row
    if (fromCoord / 8 == toCoord / 8) {
      if (fromCoord > toCoord) {
        //Jump left
        for (int i = fromCoord; i > toCoord; i--) {
          if (squares.get(fromCoord).isPresent()) {
            intermediatePieceCount++;
          }
        }
      } else {
        //Jump Right
        for (int i = fromCoord; i < toCoord; i++) {
          if (squares.get(fromCoord).isPresent()) {
            intermediatePieceCount++;
          }
        }
      }
    } else if (fromCoord % 8 == toCoord % 8) { //In the same column
      if (fromCoord < toCoord) {
        //Jump Down
        while (fromCoord < toCoord) {
          if (squares.get(fromCoord).isPresent()) {
            intermediatePieceCount++;
          }
          fromCoord += 8;
        }
      } else {
        //Jump UP
        while (fromCoord > toCoord) {
          if (squares.get(fromCoord).isPresent()) {
            intermediatePieceCount++;
          }
          fromCoord -= 8;
        }
      }
    }
    check(intermediatePieceCount == 2, intermediatePieceCount);
    return true;
  }
  
  List<Operation> movePieceOperation(State state, Set move) {
    ImmutableList<Optional<Piece>> pieces = state.getPieces();
    ImmutableList<Optional<String>> squares = state.getSquares();
    Color turnOfColor = state.getTurn();
    
    @SuppressWarnings("unchecked")
    List<String> coord = (List<String>) move.getValue();
    
    check(coord.size() == 2);

    int fromCoord = Integer.parseInt(coord.get(0).substring(1));
    check(checkCoord(fromCoord), fromCoord);
    int toCoord = Integer.parseInt(coord.get(1).substring(1));
    check(checkCoord(toCoord), toCoord);
    
    check(checkFromToCoord(fromCoord, toCoord) != 0);
    // Check there's a piece on the from square
    check(squares.get(fromCoord).isPresent());
    // Check there no piece on the to square
    check(!squares.get(toCoord).isPresent());
    // Check the piece on the from square is facing up
    String pieceIdString = squares.get(fromCoord).get();
    int pieceId = Integer.parseInt(pieceIdString);
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
  
  List<Operation> turnPieceOperation(State state, Set move) {
    ImmutableList<Optional<Piece>> pieces = state.getPieces();
    ImmutableList<Optional<String>> squares = state.getSquares();
    Color turnOfColor = state.getTurn();
    
    String coord = (String) move.getValue();

    int fromCoord = Integer.parseInt(coord.substring(1));
    check(checkCoord(fromCoord), fromCoord);
    
    // Check there's a piece on the turn square
    check(squares.get(fromCoord).isPresent());
    // Check the piece is facing down
    String pieceIdString = squares.get(fromCoord).get();
    int pieceId = Integer.parseInt(pieceIdString);
    check(!pieces.get(pieceId).isPresent());
    
    //TODO: ENDGAME CHECK
    
    List<Operation> expectedOperations;
    expectedOperations = Lists.newArrayList();
    expectedOperations.add(new SetTurn(state.getPlayerId(turnOfColor.getOppositeColor())));
    expectedOperations.add(new Set(TURNPIECE, S + fromCoord));
    expectedOperations.add(new SetVisibility(P + pieceId));
    return expectedOperations;
  }
  
  List<Operation> capturePieceOperation(State state, Set move) {
    ImmutableList<Optional<Piece>> pieces = state.getPieces();
    ImmutableList<Optional<String>> squares = state.getSquares();
    Color turnOfColor = state.getTurn();
    
    @SuppressWarnings("unchecked")
    List<String> coord = (List<String>) move.getValue();
    
    check(coord.size() == 2);

    int fromCoord = Integer.parseInt(coord.get(0).substring(1));
    check(checkCoord(fromCoord), fromCoord);
    int toCoord = Integer.parseInt(coord.get(1).substring(1));
    check(checkCoord(toCoord), toCoord);
    
    // Check there's a piece on the from square
    check(squares.get(fromCoord).isPresent());
    
    // Check there's a piece on the to square
    check(squares.get(toCoord).isPresent());
    
    // Check both the pieces is facing up
    String pieceFromIdString = squares.get(fromCoord).get();
    int pieceFromId = Integer.parseInt(pieceFromIdString);
    check(pieces.get(pieceFromId).isPresent());
    
    String pieceToIdString = squares.get(toCoord).get();
    int pieceToId = Integer.parseInt(pieceToIdString);
    check(pieces.get(pieceToId).isPresent());
    
    // Check the color of the from piece is as same as the last move's player color
    Piece fromPiece = pieces.get(pieceFromId).get();
    check(fromPiece.getColor().name().substring(0, 1).equals(turnOfColor.toString()));
    
    // Check the color of the to piece is opposite to the last move's player color
    Piece toPiece = pieces.get(pieceToId).get();
    check(!toPiece.getColor().name().substring(0, 1).equals(turnOfColor.toString()));
    
    if (fromPiece.getKind().name() == "SOLDIER" && toPiece.getKind().name() == "GENERAL") {
      check(checkFromToCoord(fromCoord, toCoord) != 0);
    } else if (fromPiece.getKind().name() == "GENERAL" && toPiece.getKind().name() == "SOLDIER") {
      check(checkFromToCoord(fromCoord, toCoord) != 0);
      check(false, "General can't capture soldier");
    } else if (fromPiece.getKind().name() == "CANNON") {
      check(cannonCaptureCheck(pieces, squares, fromCoord, toCoord));
    } else {
      check(checkFromToCoord(fromCoord, toCoord) != 0);
      if (fromPiece.getKind().name() != toPiece.getKind().name()) {
        check(fromPiece.getKind().ordinal() < toPiece.getKind().ordinal());
      }
    }
    
    List<Operation> expectedOperations;
    expectedOperations = Lists.newArrayList();
    expectedOperations.add(new SetTurn(state.getPlayerId(turnOfColor.getOppositeColor())));
    expectedOperations.add(new Set(CAPTUREPIECE, ImmutableList.of(S + fromCoord, S + toCoord)));
    expectedOperations.add(new Set("S" + fromCoord, null));
    expectedOperations.add(new Set("S" + toCoord, "P" + pieceFromId));
    //TODO: I'll implement the end game later...
    return expectedOperations;
  }
  
  List<Operation> getExpectedOperations(
      Map<String, Object> lastApiState, List<Operation> lastMove, List<Integer> playerIds,
      int lastMovePlayerId) {
    
    if (lastApiState.isEmpty()) {
      return getInitialMove(playerIds.get(0), playerIds.get(1));
    }
    State lastState = gameApiStateToState(lastApiState,
        Color.values()[playerIds.indexOf(lastMovePlayerId)], playerIds);
    
    List<Operation> expectedOperations;
    //Get the operation set of one of three move.
    Set move = (Set) lastMove.get(1);
    
    /*
     * 
     * There are 3 types of moves:
     * 1) Moving a face-up piece of his/her own color.
     * 2) Turning up a face-down piece.
     * 3) Capturing a face-down piece of his/her own color.
     * 
     */  
    
    if (move.getKey() == MOVEPIECE) {
      // Moving a piece.
      expectedOperations = movePieceOperation(lastState, move);
    } else if (move.getKey() == TURNPIECE) {
      // Turning a piece.
      expectedOperations = turnPieceOperation(lastState, move);
    } else {
      // Moving a piece.
      expectedOperations = capturePieceOperation(lastState, move);
    }
    return expectedOperations;
  }
  
  List<String> getPieces() {
    List<String> keys = Lists.newArrayList();
    for (int i = 0; i < 32; i++) {
      keys.add(P + i);
    }
    return keys;
  }

  String pieceIdToString(int pieceId) {
    checkArgument(pieceId >= 0 && pieceId < 32);
    int color = pieceId / 16;
    String colorString = PieceColor.values()[color].getFirstLetterLowerCase();
    int kind = pieceId % 16 == 0 ? 0
      : pieceId % 16 < 3 ? 1
      : pieceId % 16 < 5 ? 2
      : pieceId % 16 < 7 ? 3
      : pieceId % 16 < 9 ? 4
      : pieceId % 16 < 11 ? 5
      : 6;
    String kindString = Kind.values()[kind].getFirstThreeLetterLowerCase();
    return colorString + kindString;
  }

  List<Operation> getInitialMove(int redPlayerId, int blackPlayerId) {
    List<Operation> operations = Lists.newArrayList();
  //The order of operations: turn, movePiece, turnPiece, capturePiece, P0...P31, S0...P31 
    operations.add(new SetTurn(redPlayerId));
    // Set the pieces
    for (int i = 0; i < 32; i++) {
      operations.add(new Set(P + i, pieceIdToString(i)));
    }
    // Set the board
    for (int i = 0; i < 32; i++) {
      operations.add(new Set(S + i, P + i));
    }
    operations.add(new Shuffle(getPieces()));
    
    // Sets visibility
    for (int i = 0; i < 32; i++) {
      operations.add(new SetVisibility(P + i, INVISIBLE));
    }
    return operations;
  }
  
  @SuppressWarnings("unchecked")
  private State gameApiStateToState(Map<String, Object> gameApiState,
      Color turnOfColor, List<Integer> playerIds) {   
    List<Optional<Piece>> pieces = Lists.newArrayList();
    List<Optional<String>> squares = Lists.newArrayList();
   
    for (int i = 0; i < 32; i++) {
      String pieceString = (String) gameApiState.get(P + i);
      Piece piece;
      if (pieceString == null) {
        piece = null;
      } else {
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
        squares.add(Optional.fromNullable(pieceId.substring(1)));
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