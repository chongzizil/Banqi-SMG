package org.banqi.graphics;

import org.banqi.client.Piece;

import com.google.gwt.resources.client.ImageResource;

/**
 * A mapping from Piece to its ImageResource.
 * The images are all of size 100x100 (width x height).
 */
public class BanqiImageSupplier {
  private final BanqiImages banqiImages;

  public BanqiImageSupplier(BanqiImages banqiImages) {
    this.banqiImages = banqiImages;
  }

  public ImageResource getResource(BanqiImage banqiImage) {
    switch (banqiImage.kind) {
      case BACK:
        return getBackOfPieceImage();
      case HIGHLIGHT:
        return getHLPieceImage(banqiImage.piece);
      case TARGETHL:
        return getTargetHLPieceImage(banqiImage.piece);
      case NORMAL:
        return getPieceImage(banqiImage.piece);
      case EMPTY:
        return getEmptyCellImage();
      case EMPTYTARGETHL:
        return getTargetHLEmptyImage();
      case BOARD:
        return getBoardImage();
      default:
        throw new RuntimeException("Forgot kind=" + banqiImage.kind);
    }
  }

  public ImageResource getBackOfPieceImage() {
    return banqiImages.back();
  }

  public ImageResource getHLPieceImage(Piece piece) {
    switch (piece.getPieceColor()) {
      case RED:
        switch (piece.getKind()) {
          case GENERAL: return banqiImages.rgenHL();
          case ADVISOR: return banqiImages.radvHL();
          case ELEPHANT: return banqiImages.releHL();
          case CHARIOT: return banqiImages.rchaHL();
          case HORSE: return banqiImages.rhorHL();
          case CANNON: return banqiImages.rcanHL();
          case SOLDIER: return banqiImages.rsolHL();
          default:
            throw new RuntimeException("Forgot kind=" + piece.getKind());
        }
      case BLACK:
        switch (piece.getKind()) {
          case GENERAL: return banqiImages.bgenHL();
          case ADVISOR: return banqiImages.badvHL();
          case ELEPHANT: return banqiImages.beleHL();
          case CHARIOT: return banqiImages.bchaHL();
          case HORSE: return banqiImages.bhorHL();
          case CANNON: return banqiImages.bcanHL();
          case SOLDIER: return banqiImages.bsolHL();
          default:
            throw new RuntimeException("Forgot kind=" + piece.getKind());
        }
      default:
        throw new RuntimeException("Forgot color=" + piece.getPieceColor());
    }
  }
  
  public ImageResource getTargetHLPieceImage(Piece piece) {
    switch (piece.getPieceColor()) {
      case RED:
        switch (piece.getKind()) {
          case GENERAL: return banqiImages.rgenTargetHL();
          case ADVISOR: return banqiImages.radvTargetHL();
          case ELEPHANT: return banqiImages.releTargetHL();
          case CHARIOT: return banqiImages.rchaTargetHL();
          case HORSE: return banqiImages.rhorTargetHL();
          case CANNON: return banqiImages.rcanTargetHL();
          case SOLDIER: return banqiImages.rsolTargetHL();
          default:
            throw new RuntimeException("Forgot kind=" + piece.getKind());
        }
      case BLACK:
        switch (piece.getKind()) {
          case GENERAL: return banqiImages.bgenTargetHL();
          case ADVISOR: return banqiImages.badvTargetHL();
          case ELEPHANT: return banqiImages.beleTargetHL();
          case CHARIOT: return banqiImages.bchaTargetHL();
          case HORSE: return banqiImages.bhorTargetHL();
          case CANNON: return banqiImages.bcanTargetHL();
          case SOLDIER: return banqiImages.bsolTargetHL();
          default:
            throw new RuntimeException("Forgot kind=" + piece.getKind());
        }
      default:
        throw new RuntimeException("Forgot color=" + piece.getPieceColor());
    }
  }
  
  public ImageResource getPieceImage(Piece piece) {
    switch (piece.getPieceColor()) {
      case RED:
        switch (piece.getKind()) {
          case GENERAL: return banqiImages.rgen();
          case ADVISOR: return banqiImages.radv();
          case ELEPHANT: return banqiImages.rele();
          case CHARIOT: return banqiImages.rcha();
          case HORSE: return banqiImages.rhor();
          case CANNON: return banqiImages.rcan();
          case SOLDIER: return banqiImages.rsol();
          default:
            throw new RuntimeException("Forgot kind=" + piece.getKind());
        }
      case BLACK:
        switch (piece.getKind()) {
          case GENERAL: return banqiImages.bgen();
          case ADVISOR: return banqiImages.badv();
          case ELEPHANT: return banqiImages.bele();
          case CHARIOT: return banqiImages.bcha();
          case HORSE: return banqiImages.bhor();
          case CANNON: return banqiImages.bcan();
          case SOLDIER: return banqiImages.bsol();
          default:
            throw new RuntimeException("Forgot kind=" + piece.getKind());
        }
      default:
        throw new RuntimeException("Forgot color=" + piece.getPieceColor());
    }
  }
  
  public ImageResource getEmptyCellImage() {
    return banqiImages.eemp();
  }
  
  public ImageResource getTargetHLEmptyImage() {
    return banqiImages.eempTargetHL();
  }
  
  public ImageResource getBoardImage() {
    return banqiImages.board();
  }
}
