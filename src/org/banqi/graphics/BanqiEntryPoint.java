package org.banqi.graphics;

import org.banqi.client.BanqiLogic;
import org.banqi.client.BanqiPresenter;
import org.game_api.GameApi;
import org.game_api.GameApi.Game;
import org.game_api.GameApi.ContainerConnector;
import org.game_api.GameApi.IteratingPlayerContainer;
import org.game_api.GameApi.UpdateUI;
import org.game_api.GameApi.VerifyMove;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class BanqiEntryPoint implements EntryPoint {
  //ContainerConnector container;
  IteratingPlayerContainer container;
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
    
    //container = new ContainerConnector(game);
    container = new IteratingPlayerContainer(game, 2);
    BanqiGraphics banqiGraphics = new BanqiGraphics();
    banqiPresenter = new BanqiPresenter(banqiGraphics, container);
    
    /////////////////////////////////////////
    final ListBox playerSelect = new ListBox();
    playerSelect.addItem("RedPlayer");
    playerSelect.addItem("BlackPlayer");
    playerSelect.addItem("Viewer");
    playerSelect.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        int selectedIndex = playerSelect.getSelectedIndex();
        String playerId = selectedIndex == 2 ? GameApi.VIEWER_ID
            : container.getPlayerIds().get(selectedIndex);
        container.updateUi(playerId);
      }
    });
    DockPanel dockPanel = new DockPanel();
    dockPanel.add(banqiGraphics, DockPanel.CENTER);
    dockPanel.add(playerSelect, DockPanel.SOUTH);
    /////////////////////////////////////////
    
    /*
    DockPanel dockPanel = new DockPanel();
    dockPanel.get("mainDiv").add(banqiGraphics, DockPanel.CENTER);
    
    OtherImages otherImages = GWT.create(OtherImages.class);
    final OtherImageSupplier otherImageSupplier = new OtherImageSupplier(otherImages);
    
    // Title
    Image titleImage = new Image(otherImageSupplier.getResource(
        OtherImage.Factory.getTitleImage()));
    dockPanel.add(titleImage, DockPanel.NORTH);
    // Translation board
    Image translationImage = new Image(otherImageSupplier.getResource(
        OtherImage.Factory.getTranslationImage()));
    dockPanel.add(translationImage, DockPanel.EAST);
    */
    
    RootPanel.get("mainDiv").add(dockPanel);
    container.sendGameReady();
    container.updateUi(container.getPlayerIds().get(0));
    
    // New
    //RootPanel.get("mainDiv").add(banqiGraphics);
    //container.sendGameReady();
  }
}