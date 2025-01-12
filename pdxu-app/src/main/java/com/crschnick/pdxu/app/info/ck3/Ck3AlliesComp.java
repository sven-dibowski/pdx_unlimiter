package com.crschnick.pdxu.app.info.ck3;

import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.lang.PdxuI18n;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.model.ck3.Ck3Tag;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.List;

import static com.crschnick.pdxu.app.gui.game.GameImage.CK3_ICON_ALLY;

public class Ck3AlliesComp extends Ck3DiplomacyRowComp {

    @Override
    protected List<Ck3Tag> getTags(SavegameContent content, SavegameData<?> data) {
        var tags = new ArrayList<Ck3Tag>();
        for (var rel : content.get().getNodeForKey("relations").getNodeForKey("active_relations").getNodeArray()) {
            if (!rel.hasKey("alliances")) {
                continue;
            }

            var first = rel.getNodeForKey("first").getLong();
            var second = rel.getNodeForKey("second").getLong();
            if (first == data.ck3().getTag().getId()) {
                Ck3Tag.getTag(data.ck3().getAllTags(), second).ifPresent(t -> tags.add(t));
            }
            if (second == data.ck3().getTag().getId()) {
                Ck3Tag.getTag(data.ck3().getAllTags(), first).ifPresent(t -> tags.add(t));
            }
        }
        return tags;
    }

    @Override
    protected String getStyleClass() {
        return "alliance";
    }

    @Override
    protected String getTooltip() {
        return PdxuI18n.get("ALLIES");
    }

    @Override
    protected Image getIcon() {
        return CK3_ICON_ALLY;
    }
}
