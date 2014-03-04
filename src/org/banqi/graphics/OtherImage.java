package org.banqi.graphics;

import java.util.Arrays;

import org.banqi.client.Equality;

/**
 * A representation of a square image.
 */
public final class OtherImage extends Equality {

  enum OtherImageKind {
    TRANSLATION,
    TITLE,
  }
  
  public static class Factory {
    public static OtherImage getTranslationImage() {
      return new OtherImage(OtherImageKind.TRANSLATION);
    }
    
    public static OtherImage getTitleImage() {
      return new OtherImage(OtherImageKind.TITLE);
    }
  }

  public final OtherImageKind kind;
  
  private OtherImage(OtherImageKind kind) {
    this.kind = kind;
  }

  @Override
  public Object getId() {
    return Arrays.asList(kind);
  }

  @Override
  public String toString() {
    switch (kind) {
      case TRANSLATION:
        return "other/translation.gif";
      case TITLE:
        return "other/title.png";
      default:
        return "Forgot kind=" + kind;
    }
    
  }
}
