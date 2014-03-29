package org.banqi.graphics;


import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.user.client.ui.AbsolutePanel;

public class BanqiDragController extends PickupDragController {
  
  public BanqiDragController(AbsolutePanel boundaryPanel, boolean allowDroppingOnBoundaryPanel) {
    super(boundaryPanel, allowDroppingOnBoundaryPanel);
  }
  
  @Override
  public void dragEnd() {
    assert context.finalDropController == null == (context.vetoException != null);
    if (context.vetoException != null) {
      context.dropController.onLeave(context);
      context.dropController = null;
      if (!getBehaviorDragProxy()) {
        restoreSelectedWidgetsLocation();
      }
    } else {
      context.dropController.onDrop(context);
      context.dropController.onLeave(context);
      context.dropController = null;
    }

    if (!getBehaviorDragProxy()) {
      restoreSelectedWidgetsStyle();
    }

    super.dragEnd();
  }
}
