package org.banqi.client;

/*
 * Copy from https://code.google.com/p/nyu-gaming-course-2013/-
 * source/browse/trunk/eclipse/src/org/shared/chess/Position.java
 */

import com.google.common.base.Objects;

public class Position {
  private int row;
  private int col;

  public Position(int row, int col) {
    this.row = row;
    this.col = col;
  }

  public int getRow() {
    return row;
  }

  public int getCol() {
    return col;
  }
  
  @Override
  public String toString() {
    return "(" + row + "," + col + ")";
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(col, row);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Position)) {
      return false;
    }
    Position other = (Position) obj;
    return col == other.col 
      && row == other.row;
  }
}