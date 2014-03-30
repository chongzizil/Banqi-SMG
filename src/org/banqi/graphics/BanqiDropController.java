package org.banqi.graphics;

import org.banqi.client.BanqiPresenter;
import org.banqi.client.Position;
import org.banqi.client.StateExplorerImpl;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Image;
import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.VetoDragException;
import com.allen_sauer.gwt.dnd.client.drop.SimpleDropController;

public class BanqiDropController extends SimpleDropController {
  private final Image image;
  private final BanqiPresenter presenter;
  private final AbsolutePanel board;
  private final StateExplorerImpl stateExplorer = new StateExplorerImpl();
  private final int index;
  private final boolean isPiece;
  
  public BanqiDropController(Image image, BanqiPresenter presenter,
      AbsolutePanel board, int index, boolean isPiece) {
    super(image);
    this.image = image;
    this.presenter = presenter;
    this.board = board;
    this.index = index;
    this.isPiece = isPiece;
  }
  
  @Override
  public void onDrop(DragContext context) {
    Position dropper = getPosition((Image) context.draggable);
    Position target = getPosition((Image) image);
    int indexOfTarget = stateExplorer.convertCoord(target.getRow(), target.getCol());
    int indexOfdropper = stateExplorer.convertCoord(dropper.getRow(), dropper.getCol());
    //presenter.pieceSelected(indexOfdropper);
    if (isPiece) {
      presenter.pieceSelected(indexOfTarget);
    } else {
      presenter.squareSelected(indexOfTarget);
    }
    ///Image dropper1 = (Image) context.draggable;
    ///image.setSize("70px", "70px");
    ///dropper1.setSize("50px", "50px");
    
    ////Position target = getPosition((Image) image);
    ////Position dropper = getPosition((Image) context.draggable);
    //Position startPos = getPosition((Image) context.draggable);
    //if (startPos == null) {
    //    return;
    //}
    //int indexOfdropper = stateExplorer.convertCoord(startPos.getRow(), startPos.getCol());
    //super.onDrop(context);
    
    //dropper.onDrop(new Position(row, col));
    //int startX = (startPos.getCol() - 1) * 100;
    //int startY = (startPos.getRow() - 1) * 100;
    ////int indexOfTarget = stateExplorer.convertCoord(target.getRow(), target.getCol());
    ////int indexOfdropper = stateExplorer.convertCoord(dropper.getRow(), dropper.getCol());
    //int indexOfTarget = index;
    ////System.out.println(indexOfdropper);
    ////System.out.println(indexOfTarget);
    ////presenter.pieceSelected(indexOfdropper);
    ////presenter.pieceSelected(indexOfTarget);
  }
  
  /*
  @Override
  public void onEnter(DragContext context) {
    Position dropper = getPosition((Image) context.draggable);
    Position target = getPosition((Image) image);
    int indexOfTarget = stateExplorer.convertCoord(target.getRow(), target.getCol());
    int indexOfdropper = stateExplorer.convertCoord(dropper.getRow(), dropper.getCol());
    //presenter.pieceSelected(indexOfdropper);
    if (isPiece) {
      presenter.pieceSelected(indexOfTarget);
    } else {
      presenter.squareSelected(indexOfTarget);
    }
  }
  
  @Override
  public void onLeave(DragContext context) {
    Image dropper = (Image) context.draggable;
    image.setSize("100px", "100px");
    dropper.setSize("100px", "100px");
  }*/

  @Override
  public void onPreviewDrop(DragContext context) throws VetoDragException {
    if (image == null) {
      throw new VetoDragException();
    }
    super.onPreviewDrop(context);
  }
  
  public Position getPosition(Image image) {
    int top = image.getAbsoluteTop();
    int left = image.getAbsoluteLeft();
    int row = (image.getAbsoluteTop() / 100) + 1;
    int col = (image.getAbsoluteLeft() / 100) + 1;
    /*
     * 
    if (image.getAbsoluteTop() % 100 != 0 || image.getAbsoluteLeft() % 100 != 0) {
      for (int i = 0; i < 64; i++) {
        Image img = (Image) board.getWidget(i);
        String imgUrl = img.getUrl();
        String imageUrl = image.getUrl();
        if (img.getUrl().equals(image.getUrl())) {
          row = (img.getAbsoluteTop() / 100) + 1;
          col = (img.getAbsoluteLeft() / 100) + 1;
          return new Position(((i % 32) / 8) + 1, ((i % 32) % 8) + 1);
        }
      }
    }
    
    for (int i = 0; i < 64; i++) {
      Image img = (Image) board.getWidget(i);
      if (img.getHeight() == image.getHeight() && img.getWidth() == image.getWidth()) {
        return new Position(((i % 32) / 8) + 1, ((i % 32) % 8) + 1);
      }
    }*/
    return new Position(row, col);
  }
}
