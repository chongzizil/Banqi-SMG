package org.banqi.graphics;

import org.banqi.client.BanqiPresenter;
import org.banqi.client.Position;
import org.banqi.client.StateExplorerImpl;

import com.google.gwt.user.client.ui.Image;
import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.VetoDragException;
import com.allen_sauer.gwt.dnd.client.drop.SimpleDropController;

public class BanqiDropController extends SimpleDropController {
  private final Image image;
  private final BanqiPresenter presenter;
  private final StateExplorerImpl stateExplorer = new StateExplorerImpl();
  
  public BanqiDropController(Image image, BanqiPresenter presenter) {
    super(image);
    this.image = image;
    this.presenter = presenter;
  }
  
  @Override
  public void onDrop(DragContext context) {
    // Get the target's position
    Position target = getPosition((Image) image);
    // Get the target's square ID (position in the board).
    int cellIndex = stateExplorer.convertToIndex(target.getRow(), target.getCol());
    
    int fromCellIndex = presenter.getFromCellIndex();
    presenter.cellSelected(fromCellIndex, true);
    presenter.cellSelected(cellIndex, true);
  }

  @Override
  public void onPreviewDrop(DragContext context) throws VetoDragException {
    if (image == null) {
      throw new VetoDragException();
    }
    super.onPreviewDrop(context);
  }
  
  public Position getPosition(Image image) {
    int row = (image.getAbsoluteTop() / 100) + 1;
    int col = (image.getAbsoluteLeft() / 100) + 1;
    return new Position(row, col);
  }
}