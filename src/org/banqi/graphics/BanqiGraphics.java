package org.banqi.graphics;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.banqi.client.Color;
import org.banqi.client.Piece;
import org.banqi.client.BanqiPresenter;
import org.banqi.client.Position;
import org.banqi.client.State;
import org.banqi.client.StateExplorerImpl;
import org.banqi.sounds.GameSounds;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.AudioElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.media.client.Audio;
import com.google.gwt.resources.client.ImageResource;
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
  private PieceMovingAnimation animation;
  private PieceSound sound;
  private Audio pieceDown;
  private Audio pieceCaptured;
  List<Image> allImages;

  private final StateExplorerImpl stateExplorer = new StateExplorerImpl();
  //private Position moveFrom = null;
  //private Position moveTo = null;
  //private java.util.Set<MovePiece> possibleMoves;

  public BanqiGraphics() {
    GameSounds gameSounds = GWT.create(GameSounds.class);
    PieceImages pieceImages = GWT.create(PieceImages.class);
    SquareImages squareImages = GWT.create(SquareImages.class);
    this.pieceImageSupplier = new PieceImageSupplier(pieceImages);
    this.squareImageSupplier = new SquareImageSupplier(squareImages);
    BanqiGraphicsUiBinder uiBinder = GWT.create(BanqiGraphicsUiBinder.class);
    initWidget(uiBinder.createAndBindUi(this));
    
    if (Audio.isSupported()) {
      pieceDown = Audio.createIfSupported();
      pieceDown.addSource(gameSounds.pieceDownMp3().getSafeUri()
                      .asString(), AudioElement.TYPE_MP3);
      pieceDown.addSource(gameSounds.pieceDownWav().getSafeUri()
                      .asString(), AudioElement.TYPE_WAV);
      pieceCaptured = Audio.createIfSupported();
      pieceCaptured.addSource(gameSounds.pieceCapturedMp3().getSafeUri()
                      .asString(), AudioElement.TYPE_MP3);
      pieceCaptured.addSource(gameSounds.pieceCapturedWav().getSafeUri()
                      .asString(), AudioElement.TYPE_WAV);
}
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
              presenter.squareSelected(imgFinal.squareId, false); //squareId
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
              presenter.pieceSelected(imgFinal.pieceId, false); //piceeId
            }
          }
        });
        res.add(image);
      } else {
        res.add(null);
      }
    }
    allImages = res;
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
    board.setSize("800px", "400px");
    
    // Initialize the drag controller
    BanqiDragController dragCtrl = new BanqiDragController(board, false, presenter);
    dragCtrl.setBehaviorConstrainedToBoundaryPanel(true);
    dragCtrl.setBehaviorMultipleSelection(false);
    dragCtrl.setBehaviorDragStartSensitivity(1);
    // Get all possible start positions
    Set<Position> possibleStartPositions = stateExplorer.getPossibleStartPositions(
        presenter.getState());
    List<Integer> possibleStartIndexOfSquare = convertFromPosToIndex(possibleStartPositions);
    
    for (int i = 0; i < 32; i++) {
      final int squareIndex = i;
      final int pieceIndex = i + 32;
      int xCoord = (i % 8) * 100;
      int yCoord = (i / 8) * 100;
      // Add the square image
      board.add(images.get(squareIndex), xCoord, yCoord);
      BanqiDropController target;
      
      if (images.get(pieceIndex) != null) {
        // If the piece exist in square i, add its image
        board.add(images.get(pieceIndex), xCoord, yCoord);
        // Get the state
        State state = presenter.getState();
        // Get the turn color and player color (or null if it's viewer)
        Color turnColor = state.getTurn();
        Color myColor = presenter.getMyColor();
        // If the it's the player's turn, add the possible start position's piece
        // to drag controller, thus the player can perform drag on the valid pieces only.
        if (myColor != null && myColor.name().equals(turnColor.name())) {
          if (possibleStartIndexOfSquare.contains(pieceIndex - 32)) {
            dragCtrl.makeDraggable(images.get(pieceIndex));
          }
        }
        // Register all pieces to the drag controller
        target = new BanqiDropController(images.get(pieceIndex), presenter, true);
        dragCtrl.registerDropController(target);
      } else {
        // Register squares with no pieces inside to the drag controller
        target = new BanqiDropController(images.get(squareIndex), presenter, false);
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
  
  public Position getPosition(Image image) {
    int row = (image.getAbsoluteTop() / 100) + 1;
    int col = (image.getAbsoluteLeft() / 100) + 1;
    return new Position(row, col);
  }

  /** Convert from Position (row/col base) to index base (0-31). */
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
  
  @Override
  public void makeSound(boolean isCapture) {
    sound = new PieceSound(isCapture ? pieceCaptured : pieceDown);
    sound.run(50);
  }
  
  @Override
  public void animateMove(List<Optional<Integer>> squares, List <Optional<Piece>> pieces,
      int startCoord, int endCoord, boolean isMove) {
    
    int startId = squares.get(startCoord).get();
    int endId = isMove ? endCoord : squares.get(endCoord).get();
    
    Image startImage = allImages.get(startCoord + 32);
    Image endImage = isMove ? allImages.get(endId) : allImages.get(endCoord + 32);
    PieceImage startImg;
    ImageResource startRes;
    ImageResource endRes;
    
    // If first piece is facing up
    if (pieces.get(startId).isPresent()) {
      startImg = PieceImage.Factory.getPieceImage(
          pieces.get(startId).get(), startId);
      startRes = pieceImageSupplier.getResource(startImg);
    } else {
      startImg = PieceImage.Factory.getBackOfPieceImage(
          squares.get(startCoord).get());
      startRes = pieceImageSupplier.getResource(startImg);
    }
    
    
    // If the second selected is a square
    if (isMove) {
      SquareImage endImg;
      Piece endPiece = pieces.get(endId).get();
      int endSquareId = endId;
      endImg = SquareImage.Factory.getSquareImage(endId);
      endRes = squareImageSupplier.getResource(endImg);
    } else {
      PieceImage endImg;
      if (pieces.get(endId).isPresent()) {
        endImg = PieceImage.Factory.getPieceImage(
            pieces.get(endId).get(), endId);
        endRes = pieceImageSupplier.getResource(endImg);
      } else {
        endImg = PieceImage.Factory.getBackOfPieceImage(
            squares.get(endCoord).get());
        endRes = pieceImageSupplier.getResource(endImg);
      }
    }
 
    ImageResource transformImage = null;

    animation = new PieceMovingAnimation(startImage, endImage, startRes,
        endRes, endRes);
    animation.run(1000);
  }
  
  
}