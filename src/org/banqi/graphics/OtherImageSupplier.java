package org.banqi.graphics;

import com.google.gwt.resources.client.ImageResource;

public class OtherImageSupplier {
  private final OtherImages otherImages;

  public OtherImageSupplier(OtherImages otherImages) {
    this.otherImages = otherImages;
  }

  public ImageResource getResource(OtherImage otherImage) {
    switch (otherImage.kind) {
    case TRANSLATION:
      return getTranslationImage();
    case TITLE:
      return getTitleImage();
    default:
      throw new RuntimeException("Forgot kind=" + otherImage.kind);
    }
  }

  public ImageResource getTranslationImage() {
    return otherImages.translation();
  }
  
  public ImageResource getTitleImage() {
    return otherImages.title(); //TODO:
  }
}
