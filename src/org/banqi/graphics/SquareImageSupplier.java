package org.banqi.graphics;

import com.google.gwt.resources.client.ImageResource;

/**
 * A mapping from Square to its ImageResource.
 * The images are all of size 100x100 (width x height).
 */
public class SquareImageSupplier {
  private final SquareImages squareImages;

  public SquareImageSupplier(SquareImages squareImages) {
    this.squareImages = squareImages;
  }

  public ImageResource getResource(SquareImage squareImage) {
    switch (squareImage.kind) {
      case NORMAL:
        return getSquareImage();
      default:
        throw new RuntimeException("Forgot kind=" + squareImage.kind);
    }
  }

  public ImageResource getSquareImage() {
    return squareImages.square();
  }
}
