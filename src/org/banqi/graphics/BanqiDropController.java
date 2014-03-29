package org.banqi.graphics;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.Image;
import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.VetoDragException;
import com.allen_sauer.gwt.dnd.client.drop.SimpleDropController;

public class BanqiDropController extends SimpleDropController {
  private final Image image;
  
  public BanqiDropController(Image image) {
    super(image);
    this.image = image;
  }
  
  @Override
  public void onDrop(DragContext context) {
    image.fireEvent(new ClickEvent() {
      
    });
  }

  @Override
  public void onPreviewDrop(DragContext context) throws VetoDragException {
    if (image != null) {
      throw new VetoDragException();
    }
    super.onPreviewDrop(context);
  }
}
