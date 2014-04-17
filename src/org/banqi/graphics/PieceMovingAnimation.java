package org.banqi.graphics;

/*
 * Copied from https://code.google.com/p/nyu-gaming-course-2013/source/-
 * browse/trunk/eclipse/src/org/simongellis/hw5/PieceMovingAnimation.java
 */

import com.google.gwt.animation.client.Animation;
import com.google.gwt.media.client.Audio;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Image;

public class PieceMovingAnimation extends Animation {
  AbsolutePanel panel;
  Image start, end, moving;
  ImageResource piece, transform;
  int startX, startY, startWidth, startHeight;
  int endX, endY;
  Audio audio;
  boolean cancelled;
  
  public PieceMovingAnimation(Image startImage, Image endImage,
      ImageResource startRes, ImageResource endRes,
      ImageResource blankRes, Audio sfx, boolean isDnd) {
    start = startImage;
    end = endImage;
    piece = startRes;
    transform = endRes == null ? startRes : endRes;
    panel = (AbsolutePanel) start.getParent();
    startX = panel.getWidgetLeft(start);
    startY = panel.getWidgetTop(start);
    startWidth = startImage.getWidth();
    startHeight = startImage.getHeight();
    endX = panel.getWidgetLeft(end);
    endY = panel.getWidgetTop(end);
    audio = sfx;
    cancelled = false;
    

    start.setResource(blankRes);
    moving = new Image(startRes);
    moving.setPixelSize(startWidth, startHeight);
    panel.add(moving, startX, startY);


    playAudio(audio);
  }

  @Override
  protected void onUpdate(double progress) {
    int x = (int) (startX + (endX - startX) * progress);
    int y = (int) (startY + (endY - startY) * progress);
    double scale = 1 + 0.5 * Math.sin(progress * Math.PI);
    int width = (int) (startWidth * scale);
    int height = (int) (startHeight * scale);
    moving.setPixelSize(width, height);
    x -= (width - startWidth) / 2;
    y -= (height - startHeight) / 2;

    panel.remove(moving);
    moving = new Image(piece.getSafeUri());
    moving.setPixelSize(width, height);
    panel.add(moving, x, y);
  }

  @Override
  protected void onCancel() {
    cancelled = true;
    panel.remove(moving);
  }

  @Override
  protected void onComplete() {
    if (!cancelled) {
      end.setResource(transform);
      panel.remove(moving);
    }
  }
  
  private void playAudio(Audio audio) {
    if (audio != null) {
      audio.play();
    }
  }
}