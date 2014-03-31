package org.banqi.graphics;


import java.util.List;

import org.banqi.client.BanqiPresenter;
import org.banqi.client.Position;
import org.banqi.client.State;
import org.banqi.client.StateExplorerImpl;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.common.base.Optional;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Image;

public class BanqiDragController extends PickupDragController {
  
  private final StateExplorerImpl stateExplorer = new StateExplorerImpl();
  private final BanqiPresenter presenter;
  
  public BanqiDragController(AbsolutePanel boundaryPanel, boolean allowDroppingOnBoundaryPanel,
      BanqiPresenter presenter) {
    super(boundaryPanel, allowDroppingOnBoundaryPanel);
    this.presenter = presenter;
  }
  
  @Override
  public void dragStart() {
    super.dragStart();
    saveSelectedWidgetsLocationAndStyle();
    // Get the dragger's position
    Position startPos = getPosition((Image) context.draggable);

    // Convert the coordinate from row/col to index (0-31)
    int indexOfdropper = stateExplorer.convertCoord(startPos.getRow(), startPos.getCol());

    State state = presenter.getState();
    List<Optional<Integer>> squares = state.getSquares();
    // Get the dragger's piece ID and perform presenter.pieceSelected(pieceId)
    int pieceId = squares.get(indexOfdropper).get();
    presenter.pieceSelected(pieceId, true);

  }
  
  public Position getPosition(Image image) {
    int row = (image.getAbsoluteTop() / 100) + 1;
    int col = (image.getAbsoluteLeft() / 100) + 1;
    return new Position(row, col);
  }
}
