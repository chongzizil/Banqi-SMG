package org.banqi.graphics;

import org.banqi.client.BanqiPresenter;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Image;
import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.VetoDragException;
import com.allen_sauer.gwt.dnd.client.drop.SimpleDropController;

public class BanqiDropController extends SimpleDropController {
  private final Image image;
  private final BanqiPresenter banqiPresenter;
  private final int id;
  
  public BanqiDropController(Image image, BanqiPresenter banqiPresenter, int id) {
    super(image);
    this.image = image;
    this.banqiPresenter = banqiPresenter;
    this.id = id;
  }
  
  @Override
  public void onDrop(DragContext context) {
    
  }

  @Override
  public void onPreviewDrop(DragContext context) throws VetoDragException {
    if (image != null) {
      throw new VetoDragException();
    }
    super.onPreviewDrop(context);
  }
}
