package org.banqi.graphics;

import java.util.List;

import org.banqi.client.Piece;
import org.banqi.client.BanqiPresenter;

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
  
  private boolean enableClicks = false;
  private final PieceImageSupplier pieceImageSupplier;
  private final SquareImageSupplier squareImageSupplier;
  private BanqiPresenter presenter;

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
    
    AbsolutePanel board = new AbsolutePanel();
    board.setSize("800px", "400px");
    board.add(images.get(0), 0, 0);
    
    //Row1
    if (images.get(32) != null) {
      board.add(images.get(32), 0, 0);
    }
    board.add(images.get(1), 100, 0);
    if (images.get(33) != null) {
      board.add(images.get(33), 100, 0);
    }
    board.add(images.get(2), 200, 0);
    if (images.get(34) != null) {
      board.add(images.get(34), 200, 0);
    }
    board.add(images.get(3), 300, 0);
    if (images.get(35) != null) {
      board.add(images.get(35), 300, 0);
    }
    board.add(images.get(4), 400, 0);
    if (images.get(36) != null) {
      board.add(images.get(36), 400, 0);
    }
    board.add(images.get(5), 500, 0);
    if (images.get(37) != null) {
      board.add(images.get(37), 500, 0);
    }
    board.add(images.get(6), 600, 0);
    if (images.get(38) != null) {
      board.add(images.get(38), 600, 0);
    }
    board.add(images.get(7), 700, 0);
    if (images.get(39) != null) {
      board.add(images.get(39), 700, 0);
    }
    //Row2
    board.add(images.get(8), 0, 100);
    if (images.get(40) != null) {
      board.add(images.get(40), 0, 100);
    }
    board.add(images.get(9), 100, 100);
    if (images.get(41) != null) {
      board.add(images.get(41), 100, 100);
    }
    board.add(images.get(10), 200, 100);
    if (images.get(42) != null) {
      board.add(images.get(42), 200, 100);
    }
    board.add(images.get(11), 300, 100);
    if (images.get(43) != null) {
      board.add(images.get(43), 300, 100);
    }
    board.add(images.get(12), 400, 100);
    if (images.get(44) != null) {
      board.add(images.get(44), 400, 100);
    }
    board.add(images.get(13), 500, 100);
    if (images.get(45) != null) {
      board.add(images.get(45), 500, 100);
    }
    board.add(images.get(14), 600, 100);
    if (images.get(46) != null) {
      board.add(images.get(46), 600, 100);
    }
    board.add(images.get(15), 700, 100);
    if (images.get(47) != null) {
      board.add(images.get(47), 700, 100);
    } 
    //Row3
    board.add(images.get(16), 0, 200);
    if (images.get(48) != null) {
      board.add(images.get(48), 0, 200);
    }
    board.add(images.get(17), 100, 200);
    if (images.get(49) != null) {
      board.add(images.get(49), 100, 200);
    }
    board.add(images.get(18), 200, 200);
    if (images.get(50) != null) {
      board.add(images.get(50), 200, 200);
    }
    board.add(images.get(19), 300, 200);
    if (images.get(51) != null) {
      board.add(images.get(51), 300, 200);
    }
    board.add(images.get(20), 400, 200);
    if (images.get(52) != null) {
      board.add(images.get(52), 400, 200);
    }
    board.add(images.get(21), 500, 200);
    if (images.get(53) != null) {
      board.add(images.get(53), 500, 200);
    }
    board.add(images.get(22), 600, 200);
    if (images.get(54) != null) {
      board.add(images.get(54), 600, 200);
    }
    board.add(images.get(23), 700, 200);
    if (images.get(55) != null) {
      board.add(images.get(55), 700, 200);
    }
    //Row4
    board.add(images.get(24), 0, 300);
    if (images.get(56) != null) {
      board.add(images.get(56), 0, 300);
    }
    board.add(images.get(25), 100, 300);
    if (images.get(57) != null) {
      board.add(images.get(57), 100, 300);
    }
    board.add(images.get(26), 200, 300);
    if (images.get(58) != null) {
      board.add(images.get(58), 200, 300);
    }
    board.add(images.get(27), 300, 300);
    if (images.get(59) != null) {
      board.add(images.get(59), 300, 300);
    }
    board.add(images.get(28), 400, 300);
    if (images.get(60) != null) {
      board.add(images.get(60), 400, 300);
    }
    board.add(images.get(29), 500, 300);
    if (images.get(61) != null) {
      board.add(images.get(61), 500, 300);
    }
    board.add(images.get(30), 600, 300);
    if (images.get(62) != null) {
      board.add(images.get(62), 600, 300);
    }
    board.add(images.get(31), 700, 300);
    if (images.get(63) != null) {
      board.add(images.get(63), 700, 300);
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
  public void chooseNextPieceOrSquare(List<Integer> squares, List<Piece> pieces,
      List<Integer> selectedPieceIds) {
    // High light the selected piece
    placeImages(playerArea, createSquareAndPieceImages(
        squares, pieces, selectedPieceIds));
    enableClicks = true;
  }
}
