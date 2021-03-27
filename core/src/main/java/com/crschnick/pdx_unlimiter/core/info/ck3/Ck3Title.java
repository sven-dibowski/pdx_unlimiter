package com.crschnick.pdx_unlimiter.core.info.ck3;

import com.crschnick.pdx_unlimiter.core.info.GameColor;
import com.crschnick.pdx_unlimiter.core.node.ColorNode;
import com.crschnick.pdx_unlimiter.core.node.Node;
import com.crschnick.pdx_unlimiter.core.node.ValueNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Ck3Title {

    private String key;
    private String name;
    private GameColor color;
    private Ck3CoatOfArms coatOfArms;
    private int claimCount;

    public Ck3Title() {
    }

    public Ck3Title(String key, String name, GameColor color, Ck3CoatOfArms coatOfArms, int claimCount) {
        this.key = key;
        this.name = name;
        this.color = color;
        this.coatOfArms = coatOfArms;
        this.claimCount = claimCount;
    }

    public static Map<Long, Ck3Title> createTitleMap(Node node, Map<Long, Ck3CoatOfArms> coaMap) {
        var tts = node.getNodeForKey("landed_titles").getNodeForKey("landed_titles");
        var map = new HashMap<Long, Ck3Title>();
        tts.forEach((k, v) -> {
            fromNode(v, coaMap).ifPresent(t -> {
                map.put(Long.parseLong(k), t);
            });
        });
        return map;
    }

    private static Optional<Ck3Title> fromNode(Node n, Map<Long, Ck3CoatOfArms> coaMap) {
        // If node is "none"
        if (n instanceof ValueNode) {
            return Optional.empty();
        }

        var claims = n.getNodeForKeyIfExistent("claims").map(cn -> cn.getNodeArray().size()).orElse(0);
        var name = n.getNodeForKey("name").getString();
        var key = n.getNodeForKey("key").getString();
        var coaId = n.getNodeForKey("coat_of_arms_id").getLong();
        var color = n.getNodeForKeyIfExistent("color")
                .map(Node::getColorNode)
                .map(GameColor::fromColorNode)
                .orElse(null);
        var coatOfArms = coaMap.get(coaId);
        return Optional.of(new Ck3Title(key, name, color, coatOfArms, claims));
    }

    public String getName() {
        return name;
    }

    public Ck3CoatOfArms getCoatOfArms() {
        return coatOfArms;
    }

    public Optional<GameColor> getColor() {
        return Optional.ofNullable(color);
    }

    public String getKey() {
        return key;
    }

    public int getClaimCount() {
        return claimCount;
    }
}
