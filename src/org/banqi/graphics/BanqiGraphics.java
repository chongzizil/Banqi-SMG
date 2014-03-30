package org.banqi.graphics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.banqi.client.BanqiPresenter.Dropper;
import org.banqi.client.MovePiece;
import org.banqi.client.Piece;
import org.banqi.client.BanqiPresenter;
import org.banqi.client.Position;
import org.banqi.client.State;
import org.banqi.client.StateExplorerImpl;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.DragEndEvent;
import com.allen_sauer.gwt.dnd.client.DragHandler;
import com.allen_sauer.gwt.dnd.client.DragHandlerAdapter;
import com.allen_sauer.gwt.dnd.client.DragStartEvent;
import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.allen_sauer.gwt.dnd.client.drop.AbsolutePositionDropController;
import com.allen_sauer.gwt.dnd.client.drop.DropController;
import com.allen_sauer.gwt.dnd.client.drop.SimpleDropController;
import com.google.common.collect.Lists;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * Graphics for the game of cheat.
 */
public class BanqiGraphics extends Composite implements BanqiPresenter.View {
  public interface BanqiGraphicsUiBinder extends UiBinder<Widget, BanqiGraphics> {
  }

  @UiField
  HorizontalPanel playerArea;
  @UiField
  GameCss css;
  
  private boolean enableClicks = false;
  private final PieceImageSupplier pieceImageSupplier;
  private final SquareImageSupplier squareImageSupplier;
  private BanqiPresenter presenter;
  AbsolutePanel board = new AbsolutePanel();
  Position dragger;

  private final StateExplorerImpl stateExplorer = new StateExplorerImpl();
  //private Position moveFrom = null;
  //private Position moveTo = null;
  //private java.util.Set<MovePiece> possibleMoves;

  public BanqiGraphics() {
    PieceImages pieceImages = GWT.create(PieceImages.class);
    SquareImages squareImages = GWT.create(SquareImages.class);
    this.pieceImageSupplier = new PieceImageSupplier(pieceImages);
    this.squareImageSupplier = new SquareImageSupplier(squareImages);
    BanqiGraphicsUiBinder uiBinder = GWT.create(BanqiGraphicsUiBinder.class);
    initWidget(uiBinder.createAndBindUi(this));
  }

  private List<Image> createImages(List<SquareImage> squareImages,
      List<PieceImage> pieceImages,
      List<Integer> squares,
      final List<Piece> pieces) {
    
    List<Image> res = Lists.newArrayList();
    // Add click handler to each square image
    for (SquareImage img : squareImages) {
      final SquareImage imgFinal = img;
      Image image = new Image(squareImageSupplier.getResource(img));
        image.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            if (enableClicks) {
              presenter.squareSelected(imgFinal.squareId); //squareId
            }
          }
        });
      res.add(image);
    }
    
    // Add click handler if there has a piece image
    for (PieceImage img : pieceImages) {
      final PieceImage imgFinal = img;
      if (imgFinal != null) {
        Image image = new Image(pieceImageSupplier.getResource(img));
        image.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            if (enableClicks) {
              presenter.pieceSelected(imgFinal.pieceId); //piceeId
            }
          }
        });
        res.add(image);
      } else {
        res.add(null);
      }
    }
    return res;
  }

  private List<Image> createSquareAndPieceImages(List<Integer> squares,
      List<Piece> pieces, List<Integer> selectedPieceIds) {
    
    // Create all square images
    List<SquareImage> squareImages = Lists.newArrayList();
    for (int i = 0; i < 32; i++) {
      squareImages.add(SquareImage.Factory.getSquareImage(i));
    }
    
    // Create all piece images
    List<PieceImage> pieceImages = Lists.newArrayList();
    for (int i = 0; i < 32; i++) {
      // If there's a piece on square i, create a corresponding image
      if (squares.get(i) != null) {
        // If the piece is facing-up, create it's image
        if (pieces.get(squares.get(i)) != null) {
          // If the piece is selected, create the high light image
          if (selectedPieceIds.contains((Integer) squares.get(i))) {
            pieceImages.add(PieceImage.Factory.getHighLightPieceImage(
                pieces.get(squares.get(i)), squares.get(i)));
          } else { // The piece is not high lighted
            pieceImages.add(PieceImage.Factory.getPieceImage(
                pieces.get(squares.get(i)), squares.get(i)));
          }
        } else { // The piece is facing-down, create a back image
          pieceImages.add(PieceImage.Factory.getBackOfPieceImage(squares.get(i)));
        }
      } else { // There's no piece on square i
        pieceImages.add(null);
      }
    }
    
    return createImages(squareImages, pieceImages, squares, pieces);
  }
  
  private void placeImages(HorizontalPanel playerArea, List<Image> images) {
    playerArea.clear();
    ////PickupDragController dragCtrl = new PickupDragController(board, false);
    board.setSize("800px", "400px");
    
    BanqiDragController dragCtrl = new BanqiDragController(board, false, presenter);
    ////dragCtrl.addDragHandler(initializeDragHandler());
    dragCtrl.setBehaviorConstrainedToBoundaryPanel(true);
    dragCtrl.setBehaviorMultipleSelection(false);
    dragCtrl.setBehaviorDragStartSensitivity(1);
    Set<Position> possibleStartPositions = stateExplorer.getPossibleStartPositions(
        presenter.getState());
    List<Integer> possibleStartIndexOfSquare = convertFromPosToIndex(possibleStartPositions);
    
    for (int i = 0; i < 32; i++) {
      final int squareIndex = i;
      final int pieceIndex = i + 32;
      int xCoord = (i % 8) * 100;
      int yCoord = (i / 8) * 100;
      board.add(images.get(squareIndex), xCoord, yCoord);
      BanqiDropController target;
      
      if (images.get(pieceIndex) != null) {
        board.add(images.get(pieceIndex), xCoord, yCoord);
        if (possibleStartIndexOfSquare.contains(pieceIndex - 32)) {
          dragCtrl.makeDraggable(images.get(pieceIndex));
        }
        target = new BanqiDropController(images.get(pieceIndex),
            presenter, board, squareIndex, true);
        dragCtrl.registerDropController(target);
      } else {
        target = new BanqiDropController(images.get(squareIndex),
            presenter, board, squareIndex, false);
        dragCtrl.registerDropController(target);
      }
    }
    playerArea.add(board);
  }

  @Override
  public void setPresenter(BanqiPresenter banqiPresenter) {
    this.presenter = banqiPresenter;
  }

  @Override
  public void setViewerState(List<Integer> squares, List<Piece> pieces) {
    placeImages(playerArea, createSquareAndPieceImages(
        squares, pieces, Lists.<Integer>newArrayList()));
  }
  
  @Override
  public void setPlayerState(List<Integer> squares, List<Piece> pieces) {
    placeImages(playerArea, createSquareAndPieceImages(
        squares, pieces, Lists.<Integer>newArrayList()));
  }

  @Override
  public void chooseNextPieceOrSquare(List<Integer> squares,
      List<Piece> pieces, List<Integer> selectedPieceIds) {
    // High light the selected piece
    placeImages(playerArea,
        createSquareAndPieceImages(squares, pieces, selectedPieceIds));
    enableClicks = true;
  }
  
  @Override
  public Position getPosition(Image image) {
    int top = image.getAbsoluteTop();
    int left = image.getAbsoluteLeft();
    int row = (image.getAbsoluteTop() / 100) + 1;
    int col = (image.getAbsoluteLeft() / 100) + 1;
    return new Position(row, col);
  }

  public List<Integer> convertFromPosToIndex(Set<Position> possibleStartPositions) {
    List<Integer> possibleStartIndexOfSquares = new ArrayList<Integer>();
    for (Position pos: possibleStartPositions) {
      int row = pos.getRow();
      int col = pos.getCol();
      int index = stateExplorer.convertCoord(row, col);
      possibleStartIndexOfSquares.add(index);
    }
    return possibleStartIndexOfSquares;
  }
  
  public void setDragger(Position pos) {
    dragger = pos;
  }
  
  public Position getDragger() {
    return dragger;
  }
}
