package org.banqi.client;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

/**
 * Representation of the banqi game state. The game state uses these keys:
 * C0...C31
 * 
 * which are mapped to these fields: Pieces
 */
public class BanqiState {
  private final Color turn;
  private final ImmutableList<String> playerIds;

  /**
   * A list of pieces, each entry of the list corresponding to one piece, if
   * there's no piece in the cell, then the kind of the piece will be EMPTY.
   */
  private final ImmutableList<Optional<Piece>> cells;

  public BanqiState(Color turn, ImmutableList<String> playerIds,
      ImmutableList<Optional<Piece>> cells) {
    super();
    this.turn = checkNotNull(turn);
    this.playerIds = checkNotNull(playerIds);
    this.cells = checkNotNull(cells);
  }

   public boolean hasFacingDownPiece() {
     //Traverse every square of the board and check if there is at least one facing-down piece
     for (Optional<Piece> piece : cells) {
       //Check if there is a face down piece
       if (!piece.isPresent()) {
         return true; //At least one facing-down piece on the board
       }
     }
     return false; //There's no facing-down piece on the board
   }
  
   public boolean hasRedOrBlackPieces(Color color) {
     // Traverse every square of the board and check if all the pieces left have the same color
     for (Optional<Piece> piece : cells) {
       // Check if there the piece is facing up
       if (piece.isPresent()) {
         // Check the piece is not empty
         if (piece.get().getKind() != Piece.Kind.EMPTY) {
           //Check the color of the piece.
           if (piece.get().getPieceColor().name().substring(0, 1).equals(color.name())) {
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

  public ImmutableList<String> getPlayerIds() {
    return playerIds;
  }

  public String getPlayerId(Color color) {
    return playerIds.get(color.ordinal());
  }

  public ImmutableList<Optional<Piece>> getCells() {
    return cells;
  }
}