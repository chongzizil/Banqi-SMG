package org.banqi.graphics;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.banqi.client.BanqiState;
import org.banqi.client.Color;
import org.banqi.client.Move;
import org.banqi.client.Piece;
import org.banqi.client.BanqiPresenter;
import org.banqi.client.Position;
import org.banqi.client.StateExplorerImpl;
import org.banqi.sounds.GameSounds;

import com.allen_sauer.gwt.dnd.client.DragContext;
//import com.allen_sauer.gwt.dnd.client.DragEndEvent;
import com.allen_sauer.gwt.dnd.client.DragHandlerAdapter;
import com.allen_sauer.gwt.dnd.client.DragStartEvent;
import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.allen_sauer.gwt.dnd.client.drop.SimpleDropController;
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
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
//import com.google.gwt.user.client.ui.LayoutPanel;
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
  private static final int ANIMATION_NORMAL_DURATION = 500;
  private static final int ANIMATION_ZERO_DURATION = 0;
  // private static final int ANIMATION_DURATION_OFFSET = 100;

  private Audio pieceDown;
  private Audio pieceCaptured;
  private Audio cannonCaptured;
  private Audio normalCaptured;
  private int startCoordOfAnimation;
  private int endCoordOfAnimation;
  private boolean isCaptureOfAnimation;
  private boolean isDndOfAnimation;
  private boolean hasAnimation = false;
  private List<Optional<Piece>> cellsOfAnimation;
  private final StateExplorerImpl stateExplorer = new StateExplorerImpl();
  List<Image> allImages;

  public BanqiGraphics() {
    GameSounds gameSounds = GWT.create(GameSounds.class);
    BanqiImages banqiImages = GWT.create(BanqiImages.class);
    this.banqiImageSupplier = new BanqiImageSupplier(banqiImages);
    BanqiGraphicsUiBinder uiBinder = GWT.create(BanqiGraphicsUiBinder.class);
    initWidget(uiBinder.createAndBindUi(this));

    if (Audio.isSupported()) {
      cannonCaptured = Audio.createIfSupported();
      cannonCaptured.setControls(false);
      if (cannonCaptured.canPlayType(AudioElement.TYPE_WAV).equals(
          AudioElement.CAN_PLAY_PROBABLY)
          || cannonCaptured.canPlayType(AudioElement.TYPE_WAV).equals(
              AudioElement.CAN_PLAY_MAYBE)) {
        cannonCaptured.addSource(gameSounds.cannonCapturedWav().getSafeUri()
            .asString(), AudioElement.TYPE_WAV);
      }
      if (cannonCaptured.canPlayType(AudioElement.TYPE_MP3).equals(
          AudioElement.CAN_PLAY_PROBABLY)
          || cannonCaptured.canPlayType(AudioElement.TYPE_MP3).equals(
              AudioElement.CAN_PLAY_MAYBE)) {
        cannonCaptured.addSource(gameSounds.cannonCapturedMp3().getSafeUri()
            .asString(), AudioElement.TYPE_WAV);
      }

      normalCaptured = Audio.createIfSupported();
      normalCaptured.setControls(false);
      if (normalCaptured.canPlayType(AudioElement.TYPE_WAV).equals(
          AudioElement.CAN_PLAY_PROBABLY)
          || normalCaptured.canPlayType(AudioElement.TYPE_WAV).equals(
              AudioElement.CAN_PLAY_MAYBE)) {
        normalCaptured.addSource(gameSounds.normalCapturedWav().getSafeUri()
            .asString(), AudioElement.TYPE_WAV);
      }
      if (normalCaptured.canPlayType(AudioElement.TYPE_MP3).equals(
          AudioElement.CAN_PLAY_PROBABLY)
          || normalCaptured.canPlayType(AudioElement.TYPE_MP3).equals(
              AudioElement.CAN_PLAY_MAYBE)) {
        normalCaptured.addSource(gameSounds.normalCapturedMp3().getSafeUri()
            .asString(), AudioElement.TYPE_WAV);
      }

      pieceDown = Audio.createIfSupported();
      pieceDown.setControls(false);
      if (pieceDown.canPlayType(AudioElement.TYPE_WAV).equals(
          AudioElement.CAN_PLAY_PROBABLY)
          || pieceDown.canPlayType(AudioElement.TYPE_WAV).equals(
              AudioElement.CAN_PLAY_MAYBE)) {
        pieceDown.addSource(gameSounds.pieceDownWav().getSafeUri().asString(),
            AudioElement.TYPE_WAV);
      }
      if (pieceDown.canPlayType(AudioElement.TYPE_MP3).equals(
          AudioElement.CAN_PLAY_PROBABLY)
          || pieceDown.canPlayType(AudioElement.TYPE_MP3).equals(
              AudioElement.CAN_PLAY_MAYBE)) {
        pieceDown.addSource(gameSounds.pieceDownMp3().getSafeUri().asString(),
            AudioElement.TYPE_WAV);
      }

      pieceCaptured = Audio.createIfSupported();
      pieceCaptured.setControls(false);
      if (pieceCaptured.canPlayType(AudioElement.TYPE_WAV).equals(
          AudioElement.CAN_PLAY_PROBABLY)
          || pieceCaptured.canPlayType(AudioElement.TYPE_WAV).equals(
              AudioElement.CAN_PLAY_MAYBE)) {
        pieceCaptured.addSource(gameSounds.pieceDownWav().getSafeUri()
            .asString(), AudioElement.TYPE_WAV);
      }
      if (pieceCaptured.canPlayType(AudioElement.TYPE_MP3).equals(
          AudioElement.CAN_PLAY_PROBABLY)
          || pieceCaptured.canPlayType(AudioElement.TYPE_MP3).equals(
              AudioElement.CAN_PLAY_MAYBE)) {
        pieceCaptured.addSource(gameSounds.pieceDownMp3().getSafeUri()
            .asString(), AudioElement.TYPE_WAV);
      }
    }
  }

  /** Create all images. */
  private List<Image> createImages(List<BanqiImage> banqiImages,
      final List<Piece> cells) {

    List<Image> res = Lists.newArrayList();

    // Add click handler to each cell image
    for (int i = 0; i < banqiImages.size(); i++) {
      BanqiImage img = banqiImages.get(i);
      final BanqiImage imgFinal = img;
      Image image = new Image(banqiImageSupplier.getResource(img));
      int row = i / 8 + 1;
      int col = i % 8 + 1;
      // Add the cell image
      image.setAltText(row + "," + col);
      // Change the image size for mobile device...
      // image.setPixelSize(70, 70);
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

  /** Creat all banqiImages. */
  private List<Image> createBanqiImages(List<Piece> cells,
      List<Integer> selectedCells) {

    // Initial the possible target indexes
    List<Integer> possibleMovesTargetIndex = new ArrayList<Integer>();

    // A piece has been selected, need to find all possible targets
    if (selectedCells.size() == 1) {
      // Get the selected piece's index
      int cellIndex = selectedCells.get(0);
      // Get the selected piece's position
      Position startPos = stateExplorer.convertToCoord(cellIndex);
      // Get all possible target positions
      Set<Move> possibleMovesFromPosition = stateExplorer
          .getPossibleMovesFromPosition(presenter.getState(), startPos);
      // Get all possible target indexs
      possibleMovesTargetIndex = convertTargetMoveToIndex(possibleMovesFromPosition);
    }

    // Create all banqiImages
    List<BanqiImage> banqiImages = Lists.newArrayList();

    // Create all banqi banqiImages
    for (int i = 0; i < 32; i++) {
      if (cells.get(i) != null) {
        // The cell does not contain a face down piece
        Piece piece = cells.get(i);
        if (piece.getKind() == Piece.Kind.EMPTY) {
          // The cell is empty
          if (possibleMovesTargetIndex.contains((Integer) i)) {
            // The empty cell is highlighted as a target
            banqiImages.add(BanqiImage.Factory.getTargetHLEmptyCellImage(
                cells.get(i), i));
          } else {
            // The empty cell is not highlighted as a target
            banqiImages.add(BanqiImage.Factory.getEmptyCellImage(cells.get(i),
                i));
          }
        } else {
          // The cell is not empty and contains a face up piece
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
        // The cell contains a face down piece
        banqiImages.add(BanqiImage.Factory.getBackOfPieceImage(i));
      }
    }
    return createImages(banqiImages, cells);
  }

  /** Place the images on the board and set the drag and drop controller. */
  private void placeImages(HorizontalPanel playerArea, final List<Image> images) {
    AbsolutePanel board = new AbsolutePanel();
    // Clear the playerArea
    playerArea.clear();
    // Clear the board
    board.clear();

    // Place the boardIamge first
    BanqiImage boardBanqiImage = BanqiImage.Factory.getBoardImage();
    Image boardImage = new Image(
        banqiImageSupplier.getResource(boardBanqiImage));
    // Set the board
    board.setSize(boardImage.getWidth() + "px", boardImage.getHeight() + "px");
    board.add(boardImage, 0, 0);

    // Initialize the drag controller
    /*PickupDragController dragCtrl = new PickupDragController(board, false);
    dragCtrl.setBehaviorConstrainedToBoundaryPanel(true);
    dragCtrl.setBehaviorMultipleSelection(false);
    dragCtrl.setBehaviorDragStartSensitivity(3);
    dragCtrl.unregisterDropControllers();
    dragCtrl.resetCache();
    dragCtrl.addDragHandler(new DragHandlerAdapter() {
      @Override
      public void onDragStart(DragStartEvent event) {
        // Get the dragger's position
        Position startPos = getPosition((Image) event.getContext().draggable);
        // Convert the coordinate from row/col to index (0-31)
        int indexOfDropper = stateExplorer.convertToIndex(startPos.getRow(),
            startPos.getCol());

        presenter.setFromCellIndex(indexOfDropper);
      }
    });*/

    // Get all possible start positions
    Set<Position> possibleStartPositions = stateExplorer
        .getPossibleStartPositions(presenter.getState());
    // Get all possible start cell indexes
    List<Integer> possibleStartIndexOfSquare = convertFromPosToIndex(possibleStartPositions);

    // Place all 32 cell images
    for (int i = 0; i < 32; i++) {
      int xCoord = (i % 8) * images.get(i).getWidth();
      int yCoord = (i / 8) * images.get(i).getHeight();

      board.add(images.get(i), xCoord, yCoord);
      // Get the state
      BanqiState state = presenter.getState();
      // Get the current turn color
      Color turnColor = state.getTurn();
      // Get the color of the player
      Color myColor = presenter.getMyColor();
      // If it's the player's turn, add the possible start position's
      // piece to drag controller so the player can perform drag on the valid
      // pieces only.
      /*if (myColor != null && myColor.name().equals(turnColor.name())) {
        if (possibleStartIndexOfSquare.contains(i)) {
          dragCtrl.makeDraggable(images.get(i));
        }
      }*/
      final Image image = images.get(i);
      /*SimpleDropController dropController = new SimpleDropController(image) {

        @Override
        public void onDrop(DragContext context) {
          super.onDrop(context);
          // Get the target's position
          Position target = getPosition((Image) image);
          // Get the target's square ID (position in the board).
          int cellIndex = stateExplorer.convertToIndex(target.getRow(),
              target.getCol());
          int fromCellIndex = presenter.getFromCellIndex();
          presenter.cellSelected(fromCellIndex, true);
          presenter.cellSelected(cellIndex, true);
        }
      };
      dragCtrl.registerDropController(dropController);*/
    }

    // Place the board onto the playerArea
    playerArea.add(board);
  }

  /** Convert all possible start cell from row/col base to index base (0-31). */
  public List<Integer> convertFromPosToIndex(
      Set<Position> possibleStartPositions) {

    List<Integer> possibleStartIndexOfCells = new ArrayList<Integer>();
    // Get all possible start positions and convert them into indexes
    for (Position pos : possibleStartPositions) {
      int row = pos.getRow();
      int col = pos.getCol();
      int index = stateExplorer.convertToIndex(row, col);
      possibleStartIndexOfCells.add(index);
    }
    return possibleStartIndexOfCells;
  }

  /**
   * Convert all possible moves of a start position to all possible target
   * indexes.
   */
  public List<Integer> convertTargetMoveToIndex(
      Set<Move> possibleMovesFromPosition) {

    List<Integer> possibleTargetIndexOfCells = new ArrayList<Integer>();
    // Get all possible moves from the start position and retrieve all target
    // indexes
    for (Move move : possibleMovesFromPosition) {
      Position toPos = move.getTo();
      int row = toPos.getRow();
      int col = toPos.getCol();
      int index = stateExplorer.convertToIndex(row, col);
      possibleTargetIndexOfCells.add(index);
    }
    return possibleTargetIndexOfCells;
  }

  /** Return the image position. */
  public Position getPosition(Image image) {
    String[] coords = image.getAltText().split(",");
    int row = Integer.parseInt(coords[0]);
    int col = Integer.parseInt(coords[1]);
    return new Position(row, col);
  }

  // /** Print debug info in the console. */
  // public static native void console(String text)
  // /*-{
  // console.log(text);
  // }-*/;

  @Override
  public void setPresenter(BanqiPresenter banqiPresenter) {
    this.presenter = banqiPresenter;
  }

  @Override
  public void setViewerState(final List<Piece> cells) {
    // Set up a timer, the board will only get updated after the animation is
    // performed
    Timer animationTimer = new Timer() {
      public void run() {
        placeImages(playerArea,
            createBanqiImages(cells, Lists.<Integer> newArrayList()));
      }
    };
    if (hasAnimation) {
      animateMove(cellsOfAnimation, startCoordOfAnimation, endCoordOfAnimation,
          isCaptureOfAnimation, isDndOfAnimation);
      if (isDndOfAnimation) {
        // The player makes the move by drag and drop, hence no animation will
        // be performed
        animationTimer.schedule(ANIMATION_ZERO_DURATION);
      } else {
        // The board will get updated after the animation completed
        animationTimer.schedule(ANIMATION_NORMAL_DURATION);
      }
      hasAnimation = false;
    } else {
      // Since the initial move does not require any animation, the board will
      // get updated directly
      animationTimer.schedule(ANIMATION_ZERO_DURATION);
    }
  }

  @Override
  public void setPlayerState(final List<Piece> cells) {
    // Set up a timer, the board will only get updated after the animation is
    // performed
    Timer animationTimer = new Timer() {
      public void run() {
        placeImages(playerArea,
            createBanqiImages(cells, Lists.<Integer> newArrayList()));
      }
    };
    if (hasAnimation) {
      animateMove(cellsOfAnimation, startCoordOfAnimation, endCoordOfAnimation,
          isCaptureOfAnimation, isDndOfAnimation);
      if (isDndOfAnimation) {
        // The player makes the move by drag and drop, hence no animation will
        // be performed
        animationTimer.schedule(ANIMATION_ZERO_DURATION);
      } else {
        // The board will get updated after the animation completed
        animationTimer
            .schedule(ANIMATION_NORMAL_DURATION /*- ANIMATION_DURATION_OFFSET*/);
      }
      hasAnimation = false;
    } else {
      // Since the initial move does not require any animation, the board will
      // get updated directly
      animationTimer
          .schedule(ANIMATION_ZERO_DURATION /*- ANIMATION_DURATION_OFFSET*/);
    }
  }

  @Override
  public void chooseNextCell(List<Piece> cells, List<Integer> selectedCells) {
    placeImages(playerArea, createBanqiImages(cells, selectedCells));
    enableClicks = true;
  }

  @Override
  public void setAnimateArgs(List<Optional<Piece>> cells, int startCoord,
      int endCoord, boolean isCapture, boolean isDnd) {
    cellsOfAnimation = cells;
    startCoordOfAnimation = startCoord;
    endCoordOfAnimation = endCoord;
    isCaptureOfAnimation = isCapture;
    isDndOfAnimation = isDnd;
    hasAnimation = true;
  }

  @Override
  public void animateMove(List<Optional<Piece>> cells, int startCoord,
      int endCoord, boolean isCapture, boolean isDnd) {

    Image startImage = allImages.get(startCoord);
    Image endImage = allImages.get(endCoord);
    BanqiImage startImg;
    BanqiImage endImg;
    ImageResource startRes;
    ImageResource endRes;
    ImageResource blankRes;
    boolean isCannon = false;
    // The first selected cell
    if (cells.get(startCoord).isPresent()) {
      // The cell does not contain a face down piece
      startImg = BanqiImage.Factory.getNormalPieceImage(cells.get(startCoord)
          .get(), startCoord);
      startRes = banqiImageSupplier.getResource(startImg);
      if (cells.get(startCoord).get().getKind() == Piece.Kind.CANNON) {
        isCannon = true;
      }
    } else {
      // The cell containes a face down piece
      startImg = BanqiImage.Factory.getBackOfPieceImage(startCoord);
      startRes = banqiImageSupplier.getResource(startImg);
    }

    // The second selected cell (Same as the first one if it's turnPiece move)
    if (cells.get(endCoord).isPresent()) {
      // The cell does not contain a face down piece
      if (cells.get(endCoord).get().getKind() == Piece.Kind.EMPTY) {
        // The target cell is empty
        endImg = BanqiImage.Factory.getEmptyCellImage(
            cells.get(endCoord).get(), endCoord);
        endRes = banqiImageSupplier.getResource(endImg);
      } else {
        // The target cell has piece within
        endImg = BanqiImage.Factory.getNormalPieceImage(cells.get(endCoord)
            .get(), endCoord);
        endRes = banqiImageSupplier.getResource(endImg);
      }
    } else {
      // The cell containes a face down piece
      endImg = BanqiImage.Factory.getBackOfPieceImage(endCoord);
      endRes = banqiImageSupplier.getResource(endImg);
    }

    // Set a empty cell
    blankRes = banqiImageSupplier.getResource(BanqiImage.Factory
        .getEmptyCellImage(null, 0));

    Audio audio = isCapture ? (isCannon ? cannonCaptured : normalCaptured) : pieceDown;
    
    PieceMovingAnimation animation = new PieceMovingAnimation(startImage,
        endImage, startRes, endRes, blankRes, audio, isDnd);

    // Only perfomr the animation if the player make the move by click instead
    // of drag and drop
    if (!isDnd) {
      animation.run(ANIMATION_NORMAL_DURATION);
    }
  }
}