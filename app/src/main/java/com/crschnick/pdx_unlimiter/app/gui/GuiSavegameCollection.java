package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.game.GameCampaign;
import com.crschnick.pdx_unlimiter.app.game.GameCampaignEntry;
import com.crschnick.pdx_unlimiter.app.game.GameIntegration;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameActions;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCollection;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameManagerState;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameInfo;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.SetChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.Glow;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.*;

public class GuiSavegameCollection {

    public static <T, I extends SavegameInfo<T>> Node createCampaignButton(
            SavegameCollection<T, I> c) {
        GameIntegration<T, I> gi = GameIntegration.ALL.stream()
                .filter(i -> i.getSavegameCache().getCollections().contains(c))
                .findFirst()
                .map(v -> (GameIntegration<T, I>) v)
                .get();

        HBox btn = new HBox();
        btn.setOnMouseClicked((m) -> SavegameManagerState.<T, I>get().selectCollection(c));
        btn.setAlignment(Pos.CENTER);
        btn.getStyleClass().add(CLASS_CAMPAIGN_LIST_ENTRY);

        {
            if (c instanceof GameCampaign) {
                GameCampaign<T, I> ca = (GameCampaign<T, I>) c;
                ObservableValue<Node> prop = gi.getGuiFactory().createImage(ca);
                prop.addListener((change, o, n) -> {
                    Platform.runLater(() -> {
                        btn.getChildren().set(0, prop.getValue());
                    });
                });
                Node w = prop.getValue();
                btn.getChildren().add(w);
            } else {
                Node l = new FontIcon();
                l.getStyleClass().add(CLASS_FOLDER);
                l.getStyleClass().add(CLASS_TAG_ICON);
                btn.getChildren().add(l);
            }
        }

        VBox info = new VBox();
        info.setFillWidth(true);
        info.setAlignment(Pos.CENTER_LEFT);
        {
            HBox top = new HBox();
            top.setAlignment(Pos.CENTER);

            JFXTextField name = new JFXTextField(c.getName());
            name.getStyleClass().add(CLASS_TEXT_FIELD);
            name.textProperty().bindBidirectional(c.nameProperty());
            top.getChildren().add(name);

            Button del = new JFXButton();
            del.setGraphic(new FontIcon());
            del.getStyleClass().add("delete-button");
            del.setOnMouseClicked((m) -> {
                if (DialogHelper.showCampaignDeleteDialog()) {
                    SavegameManagerState.<T, I>get().current().getSavegameCache().delete(c);
                }
            });
            del.setAlignment(Pos.CENTER);
            top.getChildren().add(del);

            info.getChildren().add(top);
        }

        {
            HBox bottom = new HBox();

            if (c instanceof GameCampaign) {
                GameCampaign<T, I> ca = (GameCampaign<T, I>) c;
                Label date = new Label();
                date.textProperty().bind(gi.getGuiFactory().createInfoString(ca));
                date.getStyleClass().add(CLASS_DATE);
                bottom.getChildren().add(date);
            }

            Region spacer = new Region();
            bottom.getChildren().add(spacer);
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label count = new Label("[" + c.getSavegames().size() + "]");
            count.getStyleClass().add(CLASS_DATE);
            count.setAlignment(Pos.CENTER_LEFT);
            c.getSavegames().addListener((SetChangeListener<GameCampaignEntry<?, ?>>) change -> {
                Platform.runLater(() -> {
                    count.setText("[" + change.getSet().size() + "]");
                });
            });
            bottom.getChildren().add(count);

            info.getChildren().add(bottom);
        }
        btn.getChildren().add(info);

        setupDragAndDrop(c, btn);
        return btn;
    }

    private static <T, I extends SavegameInfo<T>> void setupDragAndDrop(SavegameCollection<T, I> c, HBox btn) {
        btn.setOnDragOver(event -> {
            if (event.getGestureSource() != btn && event.getSource() instanceof Node) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                btn.getChildren().get(0).setEffect(new Glow(1));
            }

            event.consume();
        });

        btn.setOnDragExited(event -> {
            btn.getChildren().get(0).setEffect(null);
        });

        btn.setOnDragDropped(de -> {
            Node src = (Node) de.getGestureSource();
            @SuppressWarnings("unchecked")
            GameCampaignEntry<T, I> entry = (GameCampaignEntry<T, I>) src.getProperties().get("list-item");
            SavegameActions.moveEntry(c, entry);
        });
    }

}