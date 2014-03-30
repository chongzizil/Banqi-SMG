package org.banqi.graphics;


import org.banqi.client.BanqiPresenter;
import org.banqi.client.Position;
import org.banqi.client.StateExplorerImpl;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Image;

public class BanqiDragController extends PickupDragController {
  
  private final StateExplorerImpl stateExplorer = new StateExplorerImpl();
  private final BanqiPresenter presenter;
  private Image image;
  
  public BanqiDragController(AbsolutePanel boundaryPanel, boolean allowDroppingOnBoundaryPanel,
      BanqiPresenter presenter) {
    super(boundaryPanel, allowDroppingOnBoundaryPanel);
    this.presenter = presenter;
  }
  
  /*
  @Override
  public void dragEnd() {
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
    super.dragEnd();
  }*/
  
  @Override
  public void dragStart() {
    super.dragStart();
    saveSelectedWidgetsLocationAndStyle();
    image = (Image) context.draggable;
    Position startPos = getPosition((Image) context.draggable);
    context.desiredDraggableX = (startPos.getCol() - 1) * 100;
    context.desiredDraggableY = (startPos.getRow() - 1) * 100;
    // Convert the coordinate from row/col to index (0-31)
    int indexOfdropper = stateExplorer.convertCoord(startPos.getRow(), startPos.getCol());
    presenter.pieceSelected(indexOfdropper);
  }
  
  public Position getPosition(Image image) {
    int top = image.getAbsoluteTop();
    int left = image.getAbsoluteLeft();
    int row = (image.getAbsoluteTop() / 100) + 1;
    int col = (image.getAbsoluteLeft() / 100) + 1;
    return new Position(row, col);
  }
}
