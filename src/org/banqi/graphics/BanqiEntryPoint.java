package org.banqi.graphics;

import org.banqi.client.BanqiLogic;
import org.banqi.client.BanqiPresenter;
import org.game_api.GameApi.ContainerConnector;
import org.game_api.GameApi.Game;
import org.game_api.GameApi.UpdateUI;
import org.game_api.GameApi.VerifyMove;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class BanqiEntryPoint implements EntryPoint {
  
  ContainerConnector container;
  //  IteratingPlayerContainer container;
  BanqiPresenter banqiPresenter;
  
  @Override
  public void onModuleLoad() {
    Window.enableScrolling(false);
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
    
    ///////////////////For test withou emulator//////////////////////
    RootPanel.get("mainDiv").add(banqiGraphics);
    container.sendGameReady();
    /////////////////////////////////////////////////////////////////
    
    ///////////////////For test withou emulator//////////////////////
    // Most of button codes copied from http://bit.ly/1i5W9M4
   
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
//    
//    RootPanel.get("mainDiv").add(flowPanel);
//    container.sendGameReady();
//    container.updateUi(container.getPlayerIds().get(0));
    
    /////////////////////////////////////////////////////////////////
  }
}