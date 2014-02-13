package org.banqi.client;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

public class TurnPiece extends Equality {
  @Nullable public static TurnPiece
    fromTurnPieceEntryInGameState(@Nullable String movePieceEntry) {
      if (movePieceEntry == null) {
        return null;
      }
      //"S11", "S12"
      
      int coord = Integer.parseInt(movePieceEntry.substring(1, 3));

      return new TurnPiece(coord);
  }

  @Nullable public static List<String>
    toTurnPieceEntryInGameState(@Nullable MovePiece movePiece) {
      return movePiece == null ? null : ImmutableList.of("coordinate:" + movePiece.getFromCoord());
  }
  
  private final int coord;
  
  public TurnPiece(int coord) {
    checkArgument(coord % 10 > 0 && coord % 10 <= 8
      && coord / 10 > 0 && coord / 10 <= 4);
    
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
