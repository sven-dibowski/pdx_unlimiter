package com.crschnick.pdx_unlimiter.eu4.savegame;

import com.crschnick.pdx_unlimiter.eu4.format.Namespace;
import com.crschnick.pdx_unlimiter.eu4.format.NodeSplitter;
import com.crschnick.pdx_unlimiter.eu4.parser.Eu4IronmanParser;
import com.crschnick.pdx_unlimiter.eu4.parser.Eu4NormalParser;
import com.crschnick.pdx_unlimiter.eu4.parser.GamedataParser;
import com.crschnick.pdx_unlimiter.eu4.parser.Node;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Eu4RawSavegame extends RawSavegame {

    private static final GamedataParser normalParser = new Eu4NormalParser();
    private static final String[] META_NODES = new String[]{"date", "save_game", "player",
            "displayed_country_name", "savegame_version", "savegame_versions", "dlc_enabled",
            "multi_player", "not_observer", "campaign_id", "campaign_length", "campaign_stats",
            "is_random_new_world", "ironman"};
    private static final String[] AI_NODES = new String[]{"ai"};

    private Node gamestate;
    private Node ai;
    private Node meta;

    public Eu4RawSavegame(String checksum, Node gamestate, Node ai, Node meta) {
        super(checksum);
        this.gamestate = gamestate;
        this.ai = ai;
        this.meta = meta;
    }

    public static boolean isIronman(Path file) throws IOException {
        var in = Files.newInputStream(file);
        boolean isZipped = new ZipInputStream(in).getNextEntry() != null;
        in.close();
        if (isZipped) {
            ZipFile zipFile = new ZipFile(file.toFile());
            ZipEntry gamestate = zipFile.getEntry("gamestate");
            if (gamestate == null) {
                return false;
            }

            var stream = zipFile.getInputStream(gamestate);
            boolean b = new Eu4IronmanParser(Namespace.EMPTY).validateHeader(stream);
            stream.close();
            return b;
        } else {
            return false;
        }
    }

    public static Eu4RawSavegame fromFile(Path file) throws Exception {
        var in = Files.newInputStream(file);
        boolean isZipped = new ZipInputStream(in).getNextEntry() != null;
        in.close();

        MessageDigest d = MessageDigest.getInstance("MD5");
        d.update(Files.readAllBytes(file));
        StringBuilder c = new StringBuilder();
        ByteBuffer b = ByteBuffer.wrap(d.digest());
        for (int i = 0; i < 16; i++) {
            var hex = String.format("%02x", b.get());
            c.append(hex);
        }
        String checksum = c.toString();

        if (isZipped) {
            ZipFile zipFile = new ZipFile(file.toFile());
            ZipEntry gamestate = zipFile.getEntry("gamestate");
            ZipEntry meta = zipFile.getEntry("meta");
            ZipEntry ai = zipFile.getEntry("ai");

            Optional<Node> gamestateNode = new Eu4IronmanParser(Namespace.EU4_GAMESTATE).parse(zipFile.getInputStream(gamestate));
            if (gamestateNode.isPresent()) {
                Node metaNode = new Eu4IronmanParser(Namespace.EU4_META).parse(zipFile.getInputStream(meta)).get();
                Node aiNode = new Eu4IronmanParser(Namespace.EU4_AI).parse(zipFile.getInputStream(ai)).get();
                zipFile.close();
                return new Eu4RawSavegame(checksum, gamestateNode.get(), aiNode, metaNode);
            } else {
                var s = new Eu4RawSavegame(checksum, normalParser.parse(zipFile.getInputStream(gamestate)).get(),
                        normalParser.parse(zipFile.getInputStream(ai)).get(),
                        normalParser.parse(zipFile.getInputStream(meta)).get());
                zipFile.close();
                return s;
            }
        } else {
            Optional<Node> node = normalParser.parse(Files.newInputStream(file));
            if (node.isPresent()) {
                Node meta = new NodeSplitter(META_NODES).splitFromNode(node.get());
                Node ai = new NodeSplitter(AI_NODES).splitFromNode(node.get());
                return new Eu4RawSavegame(checksum, node.get(), ai, meta);
            }
            throw new IOException("Invalid savegame: " + file.toString());
        }
    }

    public void write(String fileName, boolean txtSuffix) throws IOException {
        File f = new File(fileName);
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(f));
        ZipEntry e1 = new ZipEntry("gamestate" + (txtSuffix ? ".txt" : ""));
        out.putNextEntry(e1);
        byte[] b1 = gamestate.toString(0).getBytes();
        out.write(b1, 0, b1.length);
        out.closeEntry();

        ZipEntry e2 = new ZipEntry("meta" + (txtSuffix ? ".txt" : ""));
        out.putNextEntry(e2);
        byte[] b2 = meta.toString(0).getBytes();
        out.write(b2, 0, b2.length);
        out.closeEntry();

        ZipEntry e3 = new ZipEntry("ai" + (txtSuffix ? ".txt" : ""));
        out.putNextEntry(e3);
        byte[] b3 = ai.toString(0).getBytes();
        out.write(b3, 0, b3.length);
        out.closeEntry();
        out.close();
    }

    public Node getGamestate() {
        return gamestate;
    }

    public Node getAi() {
        return ai;
    }

    public Node getMeta() {
        return meta;
    }
}