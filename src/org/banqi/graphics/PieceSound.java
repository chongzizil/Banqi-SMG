package org.banqi.graphics;

/*
 * Copy from https://code.google.com/p/nyu-gaming-course-2013/source/-
 * browse/trunk/eclipse/src/org/simongellis/hw5/PieceMovingAnimation.java
 */

import com.google.gwt.animation.client.Animation;
import com.google.gwt.media.client.Audio;

public class PieceSound extends Animation {
  Audio soundAtEnd;
  boolean cancelled;

  public PieceSound(Audio sfx) {
    soundAtEnd = sfx;
    cancelled = false;
  }

  @Override
  protected void onUpdate(double progress) {
  }

  @Override
  protected void onCancel() {
    cancelled = true;
  }

  @Override
  protected void onComplete() {
    if (!cancelled) {
      if (soundAtEnd != null) {
        soundAtEnd.play();
      }
    }
  }
}