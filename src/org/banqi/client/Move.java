package org.banqi.client;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;

public class Move {
  public enum Type {
    CAPTURE,
    TURN,
    MOVE
  }
  
  private Position from;
  private Position to;
  private Type typeValue;

  // MovePiece and CapturePiece
  public Move(Position from, Position to, Type typeValue) {
    this.from = checkNotNull(from);
    this.to = checkNotNull(to);
    this.typeValue = typeValue;
  }

  // TurnPiece
  public Move(Position from) {
    this(from, from, Type.TURN);
  }

  public Position getFrom() {
    return from;
  }

  public Position getTo() {
    return to;
  }
  
  public Type getType() {
    return typeValue;
  }

  @Override
  public String toString() {
    return from + "->" + to;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(from, to);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Move)) {
      return false;
    }
    Move other = (Move) obj;
    return Objects.equal(from, other.from) && Objects.equal(to, other.to);
  }
}