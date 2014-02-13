package org.banqi.client;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

public class MovePiece extends Equality {
  @Nullable public static MovePiece
    fromMovePieceEntryInGameState(@Nullable List<String> movePieceEntry) {
      if (movePieceEntry == null) {
        return null;
      }
      //"S11", "S12"
      
      int fromCoord = Integer.parseInt(movePieceEntry.get(0).substring(1, 3));
      int toCoord = Integer.parseInt(movePieceEntry.get(1).substring(1, 3));

      return new MovePiece(fromCoord, toCoord);
  }

  @Nullable public static List<String>
    toMovePieceEntryInGameState(@Nullable MovePiece movePiece) {
      return movePiece == null ? null : ImmutableList.of("from" + movePiece.getFromCoord(),
        "to" + movePiece.getToCoord());
  }
  
  private final int fromCoord;
  private final int toCoord;
  
  public MovePiece(int fromCoord, int toCoord) {
    checkArgument(fromCoord % 10 > 0 && fromCoord % 10 <= 8
      && fromCoord / 10 > 0 && fromCoord / 10 <= 4
      && toCoord % 10 > 0 && toCoord % 10 <= 8
      && toCoord / 10 > 0 && toCoord / 10 <= 4);
    
    this.fromCoord = fromCoord;
    this.toCoord = toCoord;
  }
  
  /**
   * @return the fromCoord
   */
  public int getFromCoord() {
    return fromCoord;
  }

  /**
   * @return the toCoord
   */
  public int getToCoord() {
    return toCoord;
  }
  
  @Override
  public Object getId() {
    return Arrays.asList(fromCoord, toCoord);
  }
}
