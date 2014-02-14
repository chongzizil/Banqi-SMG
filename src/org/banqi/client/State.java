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
   * meaning the card is not visible to us.
   */
  private final ImmutableList<Optional<Piece>> pieces;
  
  /**
   * Note that some of the entries will have null,
   * meaning there is no piece.
   */
  private final ImmutableList<Optional<String>> squares;
  
  public State(Color turn, ImmutableList<Integer> playerIds,
      ImmutableList<Optional<Piece>> pieces,
      ImmutableList<Optional<String>> squares) {

    super();
    this.turn = checkNotNull(turn);
    this.playerIds = checkNotNull(playerIds);
    this.pieces = checkNotNull(pieces);
    this.squares = checkNotNull(squares);
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
  
  public ImmutableList<Optional<String>> getSquares() {
    return squares;
  }
}