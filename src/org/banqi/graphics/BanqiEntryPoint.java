package org.banqi.graphics;

import org.banqi.client.BanqiLogic;
import org.banqi.client.BanqiPresenter;
import org.banqi.client.GameApi;
import org.banqi.client.GameApi.Game;
import org.banqi.client.GameApi.IteratingPlayerContainer;
import org.banqi.client.GameApi.UpdateUI;
import org.banqi.client.GameApi.VerifyMove;

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
    container = new IteratingPlayerContainer(game, 2);
    BanqiGraphics banqiGraphics = new BanqiGraphics();
    banqiPresenter =
        new BanqiPresenter(banqiGraphics, container);
    final ListBox playerSelect = new ListBox();
    playerSelect.addItem("RedPlayer");
    playerSelect.addItem("BlackPlayer");
    playerSelect.addItem("Viewer");
    playerSelect.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        int selectedIndex = playerSelect.getSelectedIndex();
        int playerId = selectedIndex == 2 ? GameApi.VIEWER_ID
            : container.getPlayerIds().get(selectedIndex);
        container.updateUi(playerId);
      }
    });
    DockPanel dockPanel = new DockPanel();
    dockPanel.add(banqiGraphics, DockPanel.CENTER);
    dockPanel.add(playerSelect, DockPanel.SOUTH);

    RootPanel.get("mainDiv").add(dockPanel);
    container.sendGameReady();
    container.updateUi(container.getPlayerIds().get(0));
  }
}