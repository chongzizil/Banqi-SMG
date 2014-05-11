package org.banqi.client;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

/**
 * Representation of the banqi game state. The game state uses these keys:
 * C0...C31
 * 
 * which are mapped to these fields: Pieces
 */
public class BanqiState {
  /**
   * A list of pieces, each entry of the list corresponding to one piece, if
   * there's no piece in the cell, then the kind of the piece will be EMPTY.
   */
  private ImmutableList<Optional<Piece>> cells;
  // A list of all captured pieces
  private ImmutableList<Piece> capturedPieces;
  private ImmutableList<String> playerIds;
  private Color turn;

  public BanqiState(Color turn,
      ImmutableList<String> playerIds,
      ImmutableList<Optional<Piece>> cells,
      ImmutableList<Piece> capturedPieces) {
    super();
    this.turn = checkNotNull(turn);
    this.playerIds = checkNotNull(playerIds);
    this.cells = checkNotNull(cells);
    this.capturedPieces = capturedPieces;
  }

  /**
   * Get the winner, if the end is not ended, return null.
   * 
   * @return Color Return the winner's color or null if the game is not over yet.
   */
  public Color getWinner() {
    boolean hasRed = false;
    boolean hasBlack = false;
    
    // Traverse every cell of the board
    for (Optional<Piece> piece : cells) {
      if (!piece.isPresent()) {
        // At least one facing-down piece on the board, so no winner
        return Color.N;
      } else if (piece.get().getKind() != Piece.Kind.EMPTY) {
        // If there's at least one piece for each color, no winner
        if (hasRed && hasBlack) {
          return Color.N;
        }

        switch(piece.get().getPieceColor().name().substring(0, 1)) {
          case "R": hasRed = true; break;
          case "B": hasBlack = true; break;
          default: hasRed = true; break;
        }
      }
    }
    
    if (hasRed && !hasBlack) {
      return Color.R;
    } else if (!hasRed && hasBlack) {
      return Color.B;
    }
    
    return Color.N;
  }
  
//  /**
//   * Check if there's at least one face down piece.
//   * 
//   * @return boolean Wether the at least one face down piece exists in the board.
//   */
//  public boolean hasFacingDownPiece() {
//    // Traverse every square of the board and check if there is at least one
//    // facing-down piece
//    for (Optional<Piece> piece : cells) {
//      // Check if there is a face down piece
//      if (!piece.isPresent()) {
//        return true; // At least one facing-down piece on the board
//      }
//    }
//    return false; // There's no facing-down piece on the board
//  }

//  /**
//   * Check if there's any pieces of a specific color.
//   * 
//   * @param color The color of pieces need to be check
//   * @return boolean True if at least one piece of that color exists
//   */
//  public boolean hasRedOrBlackPieces(Color color) {
//    // Traverse every square of the board and check if all the pieces left have
//    // the same color
//    for (Optional<Piece> piece : cells) {
//      // Check if the piece is facing up and not empty 
//      if (piece.isPresent() && piece.get().getKind() != Piece.Kind.EMPTY) {
//        // Check the color of the piece.
//        if (piece.get().getPieceColor().name().substring(0, 1)
//            .equals(color.name())) {
//          return true; // At least one piece of that color is on the board
//        }
//      }
//    }
//    return false; // There's no piece of that color on the board
//  }
  
//  public boolean hasGameEnded() {
//    boolean hasBlack = hasRedOrBlackPieces(Color.B);
//    boolean hasRed = hasRedOrBlackPieces(Color.R);
//
//    return (hasBlack ^ hasRed) && !hasFacingDownPiece();
//  }

  public Color getTurn() {
    return turn;
  }
  
  public void setNextTurn() {
    this.turn = this.turn.getOppositeColor();
  }

  public ImmutableList<String> getPlayerIds() {
    return playerIds;
  }

  public String getPlayerId(Color color) {
    return playerIds.get(color.ordinal());
  }

  public List<Optional<Piece>> getCells() {
    return new ArrayList<Optional<Piece>>(this.cells);
  }

  public void setCells(List<Optional<Piece>> cells) {
    this.cells = ImmutableList.copyOf(cells);
  }

  public List<Piece> getCapturedPieces() {
    return new ArrayList<Piece>(this.capturedPieces);
  }

  public void setCapturedPieces(List<Piece> capturedPieces) {
    this.capturedPieces = ImmutableList.copyOf(capturedPieces);
  }
  
  public BanqiState copy() {
    return new BanqiState(this.turn, this.playerIds, this.cells, this.capturedPieces);
  }
}