package org.banqi.graphics;

import org.banqi.client.BanqiLogic;
import org.banqi.client.BanqiPresenter;
import org.banqi.client.Piece;
import org.banqi.client.Position;
import org.game_api.GameApi;
import org.game_api.GameApi.Game;
import org.game_api.GameApi.ContainerConnector;
import org.game_api.GameApi.IteratingPlayerContainer;
import org.game_api.GameApi.UpdateUI;
import org.game_api.GameApi.VerifyMove;

import com.allen_sauer.gwt.dnd.client.DragHandlerAdapter;
import com.allen_sauer.gwt.dnd.client.DragStartEvent;
import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.allen_sauer.gwt.dnd.client.drop.SimpleDropController;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.googlecode.mgwt.dom.client.event.tap.TapEvent;
import com.googlecode.mgwt.dom.client.event.tap.TapHandler;
import com.googlecode.mgwt.ui.client.MGWTStyle;
import com.googlecode.mgwt.ui.client.theme.base.ButtonCss;
import com.googlecode.mgwt.ui.client.widget.Button;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class BanqiEntryPoint implements EntryPoint {
  
    ContainerConnector container;
//  IteratingPlayerContainer container;
  BanqiPresenter banqiPresenter;
  
  @Override
  public void onModuleLoad() {
   Game game = new Game() {
      @Override
      public void sendVerifyMove(VerifyMove verifyMove) {
        container.sendVerifyMoveDone(new BanqiLogic().verify(verifyMove));
      }

      @Override
      public void sendUpdateUI(UpdateUI updateUI) {
        banqiPresenter.updateUI(updateUI);
      }
    };
    
    container = new ContainerConnector(game);
//    container = new IteratingPlayerContainer(game, 2);
    
    BanqiGraphics banqiGraphics = new BanqiGraphics();
    banqiPresenter = new BanqiPresenter(banqiGraphics, container);
    
//    ///////////////////For test withou emulator//////////////////////
//    // Most of button codes copied from http://bit.ly/1i5W9M4
//    
//    final HorizontalPanel buttonGroup = new HorizontalPanel();
//    
//    final ButtonCss buttonCss = MGWTStyle.getTheme().getMGWTClientBundle().getButtonCss();
//    final Button redPlayer = new Button("Red player");
//    final Button blackPlayer = new Button("Black player");
//    final Button viewer = new Button("Viewer"); 
//    redPlayer.setSmall(true);
//    blackPlayer.setSmall(true);
//    viewer.setSmall(true);
//    
//    redPlayer.addTapHandler(new TapHandler() {
//      @Override
//      public void onTap(TapEvent event) {
//        container.updateUi(container.getPlayerIds().get(0));
//        redPlayer.addStyleName(buttonCss.active());
//        blackPlayer.removeStyleName(buttonCss.active());
//        viewer.removeStyleName(buttonCss.active());
//      }                    
//    });
//    
//    blackPlayer.addTapHandler(new TapHandler() {
//      @Override
//      public void onTap(TapEvent event) {
//        container.updateUi(container.getPlayerIds().get(1));
//        blackPlayer.addStyleName(buttonCss.active());
//        redPlayer.removeStyleName(buttonCss.active());
//        viewer.removeStyleName(buttonCss.active());
//      }                    
//    });
//    
//    viewer.addTapHandler(new TapHandler() {
//      @Override
//      public void onTap(TapEvent event) {
//        container.updateUi(GameApi.VIEWER_ID);
//        viewer.addStyleName(buttonCss.active());
//        redPlayer.removeStyleName(buttonCss.active());
//        blackPlayer.removeStyleName(buttonCss.active());
//      }                    
//    }); 
//    
//    buttonGroup.add(redPlayer);
//    buttonGroup.add(blackPlayer);
//    buttonGroup.add(viewer);
//   
//    FlowPanel flowPanel = new FlowPanel();
//    flowPanel.add(banqiGraphics);
//    flowPanel.add(buttonGroup);

//  final ListBox playerSelect = new ListBox();
//  playerSelect.addItem("RedPlayer");
//  playerSelect.addItem("BlackPlayer");
//  playerSelect.addItem("Viewer");
//  playerSelect.addChangeHandler(new ChangeHandler() {
//    @Override
//    public void onChange(ChangeEvent event) {
//      int selectedIndex = playerSelect.getSelectedIndex();
//      String playerId = selectedIndex == 2 ? GameApi.VIEWER_ID
//          : container.getPlayerIds().get(selectedIndex);
//      container.updateUi(playerId);
//    }
//  }); 
//  LayoutPanel layoutPanel = new LayoutPanel();
//  layoutPanel.add(banqiGraphics);
//  layoutPanel.add(playerSelect);
    
//    RootPanel.get("mainDiv").add(flowPanel);
//    container.sendGameReady();
//    container.updateUi(container.getPlayerIds().get(0));
    ////////////////////////////////////////////////////////////////////////
    
//
    RootPanel.get("mainDiv").add(banqiGraphics);
    container.sendGameReady();
    
    
   
    
    
    
    
//    //TEST
//    // Initialize the drag controller
//    AbsolutePanel board = new AbsolutePanel();
//    PickupDragController dragCtrl = new PickupDragController(board, false);
//    dragCtrl.setBehaviorConstrainedToBoundaryPanel(true);
//    dragCtrl.setBehaviorMultipleSelection(false);
//    dragCtrl.setBehaviorDragStartSensitivity(3);
//    dragCtrl.unregisterDropControllers();
//    dragCtrl.resetCache();
//    
//    FlowPanel test = new FlowPanel();
//    
//    BanqiImages banqiImages = GWT.create(BanqiImages.class);
//    BanqiImageSupplier banqiImageSupplier = new BanqiImageSupplier(banqiImages);
//    
//    Image image1 = new Image(banqiImageSupplier.getResource(BanqiImage.Factory.getNormalPieceImage(
//        new Piece(Piece.Kind.ADVISOR, Piece.PieceColor.BLACK), 0)));
//    Image image2 = new Image(banqiImageSupplier.getResource(BanqiImage.Factory.getNormalPieceImage(
//        new Piece(Piece.Kind.ADVISOR, Piece.PieceColor.BLACK), 0)));
//    dragCtrl.makeDraggable(image1);
//    dragCtrl.makeDraggable(image2);
////    SimpleDropController dropController = new SimpleDropController(image1);
//    test.add(image1);
//    test.add(image2);
//    test.add(board);
//    RootPanel.get("mainDiv").add(board);
  }
  
  /** Print debug info in the console. */
  public static native void console(String text)
  /*-{
      console.log(text);
  }-*/;
}