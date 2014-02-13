package org.banqi.client;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

/**
 * Representation of the banqi game state.
 * The game state uses these keys:
 * turn, BOARD, MOVEPIECE, TURNPIECE, CAPTUREPIECE, P0...P31
 * 
 * which are mapped to these fields:
 * turn, board, movePiece, turnPiece, capturePiece, pieces
 */
public class State {
  private final Color turn;
  
  /**
   * Note that some of the entries will have null, meaning the card is not visible to us.
   */
  private final ImmutableList<Optional<Piece>> pieces;
  
  /**
   * Note that some of the entries will have null,
   * meaning there is no piece.
   */
  private final ImmutableList<Optional<String>> squares;
  
  private final Optional<MovePiece> movePiece;
  private final Optional<TurnPiece> turnPiece;
  private final Optional<CapturePiece> capturePiece;
  
  public State(Color turn,
      ImmutableList<Optional<Piece>> pieces,
      ImmutableList<Optional<String>> squares,
      Optional<MovePiece> movePiece,
      Optional<TurnPiece> turnPiece,
      Optional<CapturePiece> capturePiece) {

    super();
    this.turn = checkNotNull(turn);
    this.pieces = checkNotNull(pieces);
    this.squares = checkNotNull(squares);
    this.movePiece = movePiece;
    this.turnPiece = turnPiece;
    this.capturePiece = capturePiece;
  }

  public Color getTurn() {
    return turn;
  }

  public ImmutableList<Optional<Piece>> getPieces() {
    return pieces;
  }
  
  public ImmutableList<Optional<String>> getSquares() {
    return squares;
  }
  
  public Optional<MovePiece> getMovePiece() {
    return movePiece;
  }
  
  public Optional<TurnPiece> getTurnPiece() {
    return turnPiece;
  }
  
  public Optional<CapturePiece> getCapturePiece() {
    return capturePiece;
  }
}