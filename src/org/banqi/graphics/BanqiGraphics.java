package org.banqi.graphics;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.banqi.client.BanqiState;
import org.banqi.client.Color;
import org.banqi.client.Move;
//import org.banqi.client.BanqiState;
//import org.banqi.client.Color;
import org.banqi.client.Piece;
import org.banqi.client.BanqiPresenter;
import org.banqi.client.Position;
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
 * Graphics for the game of banqi.
 */
public class BanqiGraphics extends Composite implements BanqiPresenter.View {
  public interface BanqiGraphicsUiBinder extends
      UiBinder<Widget, BanqiGraphics> {
  }

  @UiField
  HorizontalPanel playerArea;

  private boolean enableClicks = false;
  private final BanqiImageSupplier banqiImageSupplier;
  private BanqiPresenter presenter;
  private AbsolutePanel board = new AbsolutePanel();
  
  private PieceMovingAnimation animation;
  private Audio pieceDown;
  private Audio pieceCaptured;
//  private boolean isDnd = true;
//  private boolean isTurnPiece = true;
  
  private final StateExplorerImpl stateExplorer = new StateExplorerImpl();
  List<Image> allImages;

  public BanqiGraphics() {
     GameSounds gameSounds = GWT.create(GameSounds.class);
    BanqiImages banqiImages = GWT.create(BanqiImages.class);
    this.banqiImageSupplier = new BanqiImageSupplier(banqiImages);
    BanqiGraphicsUiBinder uiBinder = GWT.create(BanqiGraphicsUiBinder.class);
    initWidget(uiBinder.createAndBindUi(this));

     if (Audio.isSupported()) {
     pieceDown = Audio.createIfSupported();
     pieceDown.addSource(gameSounds.pieceDownMp3().getSafeUri().asString(),
     AudioElement.TYPE_MP3);
     pieceDown.addSource(gameSounds.pieceDownWav().getSafeUri().asString(),
     AudioElement.TYPE_WAV);
     pieceCaptured = Audio.createIfSupported();
     pieceCaptured.addSource(gameSounds.pieceCapturedMp3().getSafeUri()
     .asString(), AudioElement.TYPE_MP3);
     pieceCaptured.addSource(gameSounds.pieceCapturedWav().getSafeUri()
     .asString(), AudioElement.TYPE_WAV);
     }
  }

  private List<Image> createImages(List<BanqiImage> banqiImages,
      final List<Piece> cells) {

    List<Image> res = Lists.newArrayList();

    // Add click handler to each cell image
    for (BanqiImage img : banqiImages) {
      final BanqiImage imgFinal = img;
      Image image = new Image(banqiImageSupplier.getResource(img));
      image.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          if (enableClicks) {
            presenter.cellSelected(imgFinal.cellId, false);
          }
        }
      });
      res.add(image);
    }

    allImages = res;
    return res;
  }

  private List<Image> createBanqiImages(List<Piece> cells,
      List<Integer> selectedCells) {

    List<Integer> possibleMovesTargetIndex = new ArrayList<Integer>();
    if (selectedCells.size() == 1) {
      int cellIndex = selectedCells.get(0);
      Position startPos = stateExplorer.convertToCoord(cellIndex);
      // Get all possible target positions
      Set<Move> possibleMovesFromPosition = stateExplorer
          .getPossibleMovesFromPosition(presenter.getState(), startPos);
      possibleMovesTargetIndex =
          convertTargetMoveToIndex(possibleMovesFromPosition);
    }

    // Create all square images
    List<BanqiImage> banqiImages = Lists.newArrayList();

    // Create all banqi piece images
    for (int i = 0; i < 32; i++) {
      if (cells.get(i) != null) {
        Piece piece = cells.get(i);
        if (piece.getKind() == Piece.Kind.EMPTY) {
          if (possibleMovesTargetIndex.contains((Integer) i)) {
            // The empty cell is highlighted as a target
            banqiImages
            .add(BanqiImage.Factory.getTargetHLEmptyCellImage(cells.get(i), i));
          } else {
            // The empty cell is not highlighted
            banqiImages
              .add(BanqiImage.Factory.getEmptyCellImage(cells.get(i), i));
          }
        } else {
          if (selectedCells.contains((Integer) i)) {
            // The piece is highlighted as selected
            banqiImages.add(BanqiImage.Factory.getHighLightPieceImage(
                cells.get(i), i));
          } else if (possibleMovesTargetIndex.contains((Integer) i)) {
            // The piece is highlighted as a target
            banqiImages.add(BanqiImage.Factory.getTargetHLPieceImage(
                cells.get(i), i));
          } else {
            // The piece is not high lighted
            banqiImages.add(BanqiImage.Factory.getNormalPieceImage(
                cells.get(i), i));
          }
        }
      } else {
        banqiImages.add(BanqiImage.Factory.getBackOfPieceImage(i));
      }
    }
    return createImages(banqiImages, cells);
  }

  private void placeImages(HorizontalPanel playerArea, List<Image> images) {
    playerArea.clear();
    
    // Initialize the drag controller
    BanqiDragController dragCtrl = new BanqiDragController(board, false,
        presenter);
    dragCtrl.setBehaviorConstrainedToBoundaryPanel(true);
    dragCtrl.setBehaviorMultipleSelection(false);
    dragCtrl.setBehaviorDragStartSensitivity(3);
    dragCtrl.unregisterDropControllers();
    dragCtrl.resetCache();

//    if (isDnd || isTurnPiece) {
      board.clear();
//    }

    board.setSize("800px", "400px");

    // Get all possible start positions
    Set<Position> possibleStartPositions = stateExplorer
        .getPossibleStartPositions(presenter.getState());
    List<Integer> possibleStartIndexOfSquare = convertFromPosToIndex(possibleStartPositions);

    BanqiImage boardBanqiImage = BanqiImage.Factory.getBoardImage();
    Image boardImage = new Image(
        banqiImageSupplier.getResource(boardBanqiImage));

//    if (isDnd || isTurnPiece) {
      board.add(boardImage, 0, 0);

      for (int i = 0; i < 32; i++) {
        int xCoord = (i % 8) * 100;
        int yCoord = (i / 8) * 100;
        // Add the square image
        board.add(images.get(i), xCoord, yCoord);

        // Get the state
        BanqiState state = presenter.getState();
        Color turnColor = state.getTurn();
        Color myColor = presenter.getMyColor();
        // If the it's the player's turn, add the possible start position's
        // piece
        // to drag controller, thus the player can perform drag on the valid
        // pieces only.
        if (myColor != null && myColor.name().equals(turnColor.name())) {
          if (possibleStartIndexOfSquare.contains(i)) {
            dragCtrl.makeDraggable(images.get(i));
          }
        }

        dragCtrl.registerDropController(new BanqiDropController(images.get(i),
            presenter));
      }
//    }
    
    playerArea.add(board);
  }

  @Override
  public void setPresenter(BanqiPresenter banqiPresenter) {
    this.presenter = banqiPresenter;
  }

  @Override
  public void setViewerState(List<Piece> cells) {
    placeImages(playerArea,
        createBanqiImages(cells, Lists.<Integer> newArrayList()));
  }

  @Override
  public void setPlayerState(List<Piece> cells) {
    placeImages(playerArea,
        createBanqiImages(cells, Lists.<Integer> newArrayList()));
  }

  @Override
  public void chooseNextCell(List<Piece> cells, List<Integer> selectedCells) {
    // High light the selected piece
    placeImages(playerArea, createBanqiImages(cells, selectedCells));
    enableClicks = true;
  }

  public Position getPosition(Image image) {
    int row = (image.getAbsoluteTop() / 100) + 1;
    int col = (image.getAbsoluteLeft() / 100) + 1;
    return new Position(row, col);
  }

  /** Convert from Position (row/col base) to index base (0-31). */
  public List<Integer> convertFromPosToIndex(
      Set<Position> possibleStartPositions) {
    List<Integer> possibleStartIndexOfCells = new ArrayList<Integer>();
    for (Position pos : possibleStartPositions) {
      int row = pos.getRow();
      int col = pos.getCol();
      int index = stateExplorer.convertToIndex(row, col);
      possibleStartIndexOfCells.add(index);
    }
    return possibleStartIndexOfCells;
  }
  
  public List<Integer> convertTargetMoveToIndex(
      Set<Move> possibleMovesFromPosition) {
    List<Integer> possibleTargetIndexOfCells = new ArrayList<Integer>();
    for (Move move : possibleMovesFromPosition) {
      Position toPos = move.getTo();
      int row = toPos.getRow();
      int col = toPos.getCol();
      int index = stateExplorer.convertToIndex(row, col);
      possibleTargetIndexOfCells.add(index);
    }
    return possibleTargetIndexOfCells;
  }

  @Override
  public void animateMove(List<Optional<Piece>> cells,
      int startCoord, int endCoord, boolean isCapture, boolean isDnd) {
//    this.isDnd = isDnd;
//    if (startCoord == endCoord) {
//      this.isTurnPiece = true;
//    } else {
//      this.isTurnPiece = false;
//    }
    Image startImage = allImages.get(startCoord);
    Image endImage = allImages.get(endCoord);
    BanqiImage startImg;
    BanqiImage endImg;
    ImageResource startRes;
    ImageResource endRes;
    ImageResource blankRes;

    // If first piece is facing up
    if (cells.get(startCoord).isPresent()) {
      startImg = BanqiImage.Factory.getNormalPieceImage(cells.get(startCoord).get(),
          startCoord);
      startRes = banqiImageSupplier.getResource(startImg);
    } else {
      startImg = BanqiImage.Factory.getBackOfPieceImage(startCoord);
      startRes = banqiImageSupplier.getResource(startImg);
    }

    // If the second selected is a square
    if (cells.get(endCoord).isPresent()) {
      if (cells.get(endCoord).get().getKind() == Piece.Kind.EMPTY) {
        // The target cell is empty
        endImg = BanqiImage.Factory.getEmptyCellImage(cells.get(endCoord).get(),
            endCoord);
        endRes = banqiImageSupplier.getResource(endImg);
      } else {
        // The target cell has piece within
        endImg = BanqiImage.Factory.getNormalPieceImage(cells.get(endCoord).get(),
            endCoord);
        endRes = banqiImageSupplier.getResource(endImg);
      }
    } else {
      endImg = BanqiImage.Factory.getBackOfPieceImage(endCoord);
      endRes = banqiImageSupplier.getResource(endImg);
    }
    
    blankRes = banqiImageSupplier.getResource(BanqiImage.Factory.getEmptyCellImage(null, 0));

    animation = new PieceMovingAnimation(startImage, endImage, startRes,
        endRes, blankRes, isCapture ? pieceCaptured : pieceDown, isDnd);
    if (!isDnd) {
      animation.run(600);
    }
  }
}