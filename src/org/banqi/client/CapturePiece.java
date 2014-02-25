package org.banqi.client;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

public class CapturePiece extends Equality {
  @Nullable public static CapturePiece fromCapturePieceEntryInGameState(
      @Nullable List<String> capturePieceEntry) {
    if (capturePieceEntry == null) {
      return null;
    }
    //"S0", "S1"
    int fromCoord = Integer.parseInt(capturePieceEntry.get(0).substring(1, 3));
    int toCoord = Integer.parseInt(capturePieceEntry.get(1).substring(1, 3));

    return new CapturePiece(fromCoord, toCoord);
  }

  @Nullable public static List<String> toCapturePieceEntryInGameState(
      @Nullable CapturePiece capturePiece) {
    return capturePiece == null ? null
        : ImmutableList.of(String.valueOf(capturePiece.getFromCoord()),
            String.valueOf(capturePiece.getToCoord()));
  }
  
  private final int fromCoord;
  private final int toCoord;
  
  public CapturePiece(int fromCoord, int toCoord) {
    checkArgument(fromCoord >= 0 && fromCoord < 32
        && toCoord >= 0 && toCoord < 32);
    
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
