package org.banqi.graphics;

import java.util.Arrays;

import org.banqi.client.Piece;
import org.banqi.client.Equality;
import org.banqi.client.Piece.Kind;
import org.banqi.client.Piece.PieceColor;


/**
 * A representation of a card image.
 */
public final class PieceImage extends Equality {

  enum PieceImageKind {
    BACK,
    NORMAL,
  }

  public static class Factory {
    public static PieceImage getBackOfPieceImage(int pieceId) {
      return new PieceImage(PieceImageKind.BACK, null, pieceId);
    }

    public static PieceImage getPieceImage(Piece piece, int pieceId) {
      return new PieceImage(PieceImageKind.NORMAL, piece, pieceId);
    }
  }

  public final PieceImageKind kind;
  public final Piece piece;
  public final int pieceId;

  private PieceImage(PieceImageKind kind, Piece piece, int pieceId) {
    this.kind = kind;
    this.piece = piece;
    this.pieceId = pieceId;
  }

  @Override
  public Object getId() {
    return Arrays.asList(kind, piece, pieceId);
  }


  private String piece2str(PieceColor color, Kind kind) {
    return (color.ordinal() == 0 ? "r" : "b")
        + (kind == Kind.GENERAL ? "gen"
        : kind == Kind.ADVISOR ? "adv"
        : kind == Kind.ELEPHANT ? "ele"
        : kind == Kind.CHARIOT ? "cha"
        : kind == Kind.HORSE ? "hor"
        : kind == Kind.CANNON ? "can" : "sol"
            );
  }

  @Override
  public String toString() {
    switch (kind) {
      case BACK:
        return "pieces/back.gif";
      case NORMAL:
        return "cards/" + piece2str(piece.getColor(), piece.getKind()) + ".gif";
      default:
        return "Forgot kind=" + kind;
    }
  }
}
