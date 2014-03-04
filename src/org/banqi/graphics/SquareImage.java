package org.banqi.graphics;

import java.util.Arrays;

import org.banqi.client.Equality;

/**
 * A representation of a square image.
 */
public final class SquareImage extends Equality {

  public static class Factory {
    public static SquareImage getSquareImage(int squareId) {
      return new SquareImage(squareId);
    }
  }

  public final int squareId;

  private SquareImage(int squareId) {
    this.squareId = squareId;
  }

  @Override
  public Object getId() {
    return Arrays.asList(squareId);
  }

  @Override
  public String toString() {
    return "square/square.gif";
  }
}
