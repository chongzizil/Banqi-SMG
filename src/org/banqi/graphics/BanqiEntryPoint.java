package org.banqi.graphics;

import org.banqi.client.BanqiLogic;
import org.banqi.client.BanqiPresenter;
import org.game_api.GameApi.Game;
import org.game_api.GameApi.ContainerConnector;
import org.game_api.GameApi.UpdateUI;
import org.game_api.GameApi.VerifyMove;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class BanqiEntryPoint implements EntryPoint {
  ContainerConnector container;
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
    
    OtherImages otherImages = GWT.create(OtherImages.class);
    final OtherImageSupplier otherImageSupplier = new OtherImageSupplier(otherImages);
    
    container = new ContainerConnector(game);
    BanqiGraphics banqiGraphics = new BanqiGraphics();
    banqiPresenter = new BanqiPresenter(banqiGraphics, container);
    
    DockPanel dockPanel = new DockPanel();
    dockPanel.add(banqiGraphics, DockPanel.CENTER);
    
    // Title
    Image titleImage = new Image(otherImageSupplier.getResource(
        OtherImage.Factory.getTitleImage()));
    dockPanel.add(titleImage, DockPanel.NORTH);
    // Translation board
    Image translationImage = new Image(otherImageSupplier.getResource(
        OtherImage.Factory.getTranslationImage()));
    dockPanel.add(translationImage, DockPanel.EAST);
    
    
    RootPanel.get("mainDiv").add(dockPanel);
    container.sendGameReady();
  }
}