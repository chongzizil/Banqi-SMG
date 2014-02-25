package org.banqi.client;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

/**
 * Representation of the banqi game state.
 * The game state uses these keys:
 * P0...P31, S0...S31
 * 
 * which are mapped to these fields:
 * pieces, squares
 */
public class State {
  private final Color turn;
  private final ImmutableList<Integer> playerIds;
  
  /**
   * Note that some of the entries will have null,
   * meaning the piece is not visible to us.
   */
  private final ImmutableList<Optional<Piece>> pieces;
  
  /**
   * Note that some of the entries will have null,
   * meaning there is no piece on that square.
   */
  private final ImmutableList<Optional<Integer>> squares;
  
  public State(Color turn, ImmutableList<Integer> playerIds,
      ImmutableList<Optional<Piece>> pieces,
      ImmutableList<Optional<Integer>> squares) {

    super();
    this.turn = checkNotNull(turn);
    this.playerIds = checkNotNull(playerIds);
    this.pieces = checkNotNull(pieces);
    this.squares = checkNotNull(squares);
  }

  public boolean hasFacingDownPiece() {
    //Traverse every square of the board and check if there is at least one facing-down piece
    for (Optional<Integer> pieceIdString : squares) {
      //Check if there is a piece on the square
      if (pieceIdString.isPresent()) {
        //If there's piece on the square, check if the piece is facing-down
        int pieceId = pieceIdString.get();
        if (!pieces.get(pieceId).isPresent()) {
          return true; //At least one facing-down piece on the board
        }
      } 
    }
    return false; //There's no facing-down piece on the board
  }
  
  public boolean hasRedOrBlackPieces(Color color) {
    //Traverse every square of the board and check if all the pieces left have the same color
    for (Optional<Integer> pieceIdString : squares) {
      //Check if there is a piece on the square
      if (pieceIdString.isPresent()) {
        //If there's piece on the square, check if the piece is facing up
        int pieceId = pieceIdString.get();
        if (pieces.get(pieceId).isPresent()) {
          //Check the color of the piece.
          Piece piece = pieces.get(pieceId).get();
          if (piece.getColor().name().substring(0, 1).equals(color.name())) {
            return true; //At least one piece of that color is on the board
          }
        }
      } 
    }
    return false; //There's no piece of that color on the board
  }
  
  public Color getTurn() {
    return turn;
  }

  public ImmutableList<Integer> getPlayerIds() {
    return playerIds;
  }

  public int getPlayerId(Color color) {
    return playerIds.get(color.ordinal());
  }
  
  public ImmutableList<Optional<Piece>> getPieces() {
    return pieces;
  }
  
  public ImmutableList<Optional<Integer>> getSquares() {
    return squares;
  }
}