package org.banqi.client;

public enum Color {
  R, B;

  public boolean isRed() {
    return this == R;
  }

  public boolean isBlack() {
    return this == B;
  }

  public Color getOppositeColor() {
    return this == R ? B : R;
  }
}