package org.banqi.graphics;

import java.util.List;

import org.banqi.client.BanqiPresenter;
import org.banqi.client.Position;
import org.banqi.client.State;
import org.banqi.client.StateExplorerImpl;

import com.google.common.base.Optional;
import com.google.gwt.user.client.ui.Image;
import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.VetoDragException;
import com.allen_sauer.gwt.dnd.client.drop.SimpleDropController;

public class BanqiDropController extends SimpleDropController {
  private final Image image;
  private final BanqiPresenter presenter;
  private final StateExplorerImpl stateExplorer = new StateExplorerImpl();
  private final boolean isPiece;
  
  public BanqiDropController(Image image, BanqiPresenter presenter, boolean isPiece) {
    super(image);
    this.image = image;
    this.presenter = presenter;
    this.isPiece = isPiece;
  }
  
  @Override
  public void onDrop(DragContext context) {
    // Get the target's position
    Position target = getPosition((Image) image);
    // Get the target's square ID (position in the board).
    int squareId = stateExplorer.convertCoord(target.getRow(), target.getCol());
    
    State state = presenter.getState();
    List<Optional<Integer>> squares = state.getSquares();
    // If the target is a piece, get its id and perform presenter.pieceSelected(pieceId).
    // Otherwise perform presenter.squareSelected(squareId).
    if (isPiece) {
      int pieceId = squares.get(squareId).get();
      presenter.pieceSelected(pieceId, true);
    } else {
      presenter.squareSelected(squareId, true);
    }
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
