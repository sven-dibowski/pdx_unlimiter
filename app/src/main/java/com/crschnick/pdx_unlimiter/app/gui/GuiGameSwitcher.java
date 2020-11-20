package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.game.GameIntegration;
import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.installation.LogManager;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;

import java.io.IOException;
import java.nio.file.Files;

public class GuiGameSwitcher {

    public static void showGameSwitchDialog() {
        Alert alert = DialogHelper.createEmptyAlert();
        alert.setTitle("Select game");

        HBox games = new HBox();
        for (var integ : GameIntegration.getAvailable()) {
            var icon = integ.getGuiFactory().createIcon();
            ColorAdjust desaturate = new ColorAdjust();
            desaturate.setSaturation(-1);
            icon.getStyleClass().add(GuiStyle.CLASS_GAME_ICON);

            Tooltip.install(icon, new Tooltip(integ.getName()));
            icon.setOnMouseClicked(e -> {
                GameIntegration.selectIntegration(integ);
                alert.setResult(ButtonType.CLOSE);
            });

            icon.setOnMouseEntered(e -> {
                icon.setEffect(null);
            });
            icon.setOnMouseExited(e -> {
                icon.setEffect(desaturate);
            });
            games.getChildren().add(icon);
            icon.setEffect(desaturate);
        }
        games.setFillHeight(true);
        games.getStyleClass().add(GuiStyle.CLASS_GAME_ICON_BAR);

        alert.getDialogPane().getScene().getWindow().setOnCloseRequest(e -> alert.setResult(ButtonType.CLOSE));
        alert.getDialogPane().setContent(games);
        alert.showAndWait();
    }
}
