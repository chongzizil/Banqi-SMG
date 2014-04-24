package org.banqi.graphics;

import java.util.Arrays;

import org.banqi.client.Piece;
import org.banqi.client.Equality;
import org.banqi.client.Piece.Kind;
import org.banqi.client.Piece.PieceColor;

/**
 * A representation of a card image.
 */
public final class BanqiImage extends Equality {

  enum BanqiImageKind {
    BACK, HIGHLIGHT, TARGETHL, NORMAL, EMPTY, EMPTYTARGETHL, BOARD
  }

  public static class Factory {
    public static BanqiImage getBackOfPieceImage(int cellIndex) {
      return new BanqiImage(BanqiImageKind.BACK, null, cellIndex);
    }

    public static BanqiImage getHighLightPieceImage(Piece piece, int cellIndex) {
      return new BanqiImage(BanqiImageKind.HIGHLIGHT, piece, cellIndex);
    }
    
    public static BanqiImage getTargetHLPieceImage(Piece piece, int cellIndex) {
      return new BanqiImage(BanqiImageKind.TARGETHL, piece, cellIndex);
    }

    public static BanqiImage getNormalPieceImage(Piece piece, int cellIndex) {
      return new BanqiImage(BanqiImageKind.NORMAL, piece, cellIndex);
    }
    
    public static BanqiImage getEmptyCellImage(Piece piece, int cellIndex) {
      return new BanqiImage(BanqiImageKind.EMPTY, null, cellIndex);
    }
    
    public static BanqiImage getTargetHLEmptyCellImage(Piece piece, int cellIndex) {
      return new BanqiImage(BanqiImageKind.EMPTYTARGETHL, null, cellIndex);
    }
    
    public static BanqiImage getBoardImage() {
      return new BanqiImage(BanqiImageKind.BOARD);
    }
  }

  public final BanqiImageKind kind;
  public final Piece piece;
  public final int cellId;

  private BanqiImage(BanqiImageKind kind, Piece piece, int cellId) {
    this.kind = kind;
    this.piece = piece;
    this.cellId = cellId;
  }
  
  private BanqiImage(BanqiImageKind kind) {
    this.kind = kind;
    this.piece = null;
    this.cellId = -1;
  }

  @Override
  public Object getId() {
    return Arrays.asList(kind, piece, cellId);
  }

  private String piece2str(PieceColor color, Kind kind) {
    return (color.ordinal() == 0 ? "r" : "b")
        + "_"
        + (kind == Kind.GENERAL ? "gen" : kind == Kind.ADVISOR ? "adv"
            : kind == Kind.ELEPHANT ? "ele" : kind == Kind.CHARIOT ? "cha"
                : kind == Kind.HORSE ? "hor" : kind == Kind.CANNON ? "can"
                    : "sol");
  }

  @Override
  public String toString() {
    switch (kind) {
    case BACK:
      return "pieces/back.gif";
    case HIGHLIGHT:
      return "pieces/" + piece2str(piece.getPieceColor(), piece.getKind()) + "_hl.gif";
    case TARGETHL:
      return "pieces/" + piece2str(piece.getPieceColor(), piece.getKind()) + "_target_hl.gif";
    case NORMAL:
      return "pieces/" + piece2str(piece.getPieceColor(), piece.getKind()) + ".gif";
    case EMPTY:
      return "other/" + "empty_cell.gif";
    case EMPTYTARGETHL:
      return "other/" + "empty_cell_target.hl.gif";
    case BOARD:
//      return "other/" + "board_mobile.gif";
      return "other/" + "board.gif";
    default:
      return "Forgot kind=" + kind;
    }
  }
}
