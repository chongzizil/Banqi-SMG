package org.banqi.graphics;

import java.util.Arrays;

import org.banqi.client.Equality;

/**
 * A representation of a square image.
 */
public final class SquareImage extends Equality {

  enum SquareImageKind {
    NORMAL,
  }

  public static class Factory {
    public static SquareImage getSquareImage(int squareId) {
      return new SquareImage(SquareImageKind.NORMAL, squareId);
    }
  }

  public final SquareImageKind kind;
  public final int squareId;

  private SquareImage(SquareImageKind kind, int squareId) {
    this.kind = kind;
    this.squareId = squareId;
  }

  @Override
  public Object getId() {
    return Arrays.asList(kind, squareId);
  }

  @Override
  public String toString() {
    return "square/square.gif";
  }
}
