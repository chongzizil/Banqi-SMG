package org.banqi.client;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Arrays;

import javax.annotation.Nullable;

public class TurnPiece extends Equality {
  @Nullable public static TurnPiece fromTurnPieceEntryInGameState(
      @Nullable String movePieceEntry) {
    if (movePieceEntry == null) {
      return null;
    }
    //"S01"
    int coord = Integer.parseInt(movePieceEntry.substring(1, 3));

    return new TurnPiece(coord);
  }

  @Nullable public static String toTurnPieceEntryInGameState(
      @Nullable TurnPiece turnPiece) {
    return turnPiece == null ? null : String.valueOf(turnPiece.getFromCoord());
  }
  
  private final int coord;
  
  public TurnPiece(int coord) {
    checkArgument(coord >= 0 && coord < 32);
    this.coord = coord;
  }
  
  /**
   * @return the Coord
   */
  public int getFromCoord() {
    return coord;
  }
  
  @Override
  public Object getId() {
    return Arrays.asList(coord);
  }
}
