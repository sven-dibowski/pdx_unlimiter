package com.crschnick.pdx_unlimiter.app.installation;

import com.crschnick.pdx_unlimiter.app.lang.Language;
import com.crschnick.pdx_unlimiter.app.lang.LanguageManager;
import com.crschnick.pdx_unlimiter.app.util.JsonHelper;
import com.crschnick.pdx_unlimiter.core.info.GameVersion;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface GameInstallType {

    GameInstallType EU4 = new StandardInstallType("eu4") {
        @Override
        public Path chooseBackgroundImage(Path p) {
            int i = new Random().nextInt(30) + 1;
            return p.resolve("gfx").resolve("loadingscreens").resolve("load_" + i + ".dds");
        }

        @Override
        public List<String> getLaunchArguments() {
            return List.of("-continuelastsave");
        }

        @Override
        public Optional<GameVersion> getVersion(String versionString) {
            Matcher m = Pattern.compile("\\w+\\s+v(\\d)\\.(\\d+)\\.(\\d+)\\.(\\d+)\\s+(\\w+)\\.\\w+\\s.+")
                    .matcher(versionString);
            if (m.find()) {
                return Optional.of(new GameVersion(
                        Integer.parseInt(m.group(1)),
                        Integer.parseInt(m.group(2)),
                        Integer.parseInt(m.group(3)),
                        Integer.parseInt(m.group(4)),
                        m.group(5)));
            } else {
                return Optional.empty();
            }
        }

        @Override
        public void writeLaunchConfig(Path userDir, String name, Instant lastPlayed, Path path) throws IOException {
            var sgPath = FilenameUtils.separatorsToUnix(userDir.relativize(path).toString());
            ObjectNode n = JsonNodeFactory.instance.objectNode()
                    .put("title", name)
                    .put("desc", "")
                    .put("date", lastPlayed.toString())
                    .put("filename", sgPath);
            JsonHelper.write(n, userDir.resolve("continue_game.json"));
        }
    };

    GameInstallType HOI4 = new StandardInstallType("hoi4") {
        @Override
        public Path chooseBackgroundImage(Path p) {
            int i = new Random().nextInt(8) + 1;
            return p.resolve("gfx").resolve("loadingscreens").resolve("load_" + i + ".dds");
        }

        @Override
        public List<String> getLaunchArguments() {
            return List.of("-gdpr-compliant", "--continuelastsave");
        }

        @Override
        public Optional<GameVersion> getVersion(String versionString) {
            Matcher m = Pattern.compile("(\\w+)\\s+v(\\d)\\.(\\d+)\\.(\\d+)").matcher(versionString);
            if (m.find()) {
                return Optional.of(new GameVersion(
                        Integer.parseInt(m.group(2)),
                        Integer.parseInt(m.group(3)),
                        Integer.parseInt(m.group(4)),
                        0,
                        m.group(1)));
            } else {
                return Optional.empty();
            }
        }

        @Override
        public Optional<String> debugModeSwitch() {
            return Optional.of("-debug");
        }

        @Override
        public void writeLaunchConfig(Path userDir, String name, Instant lastPlayed, Path path) throws IOException {
            SimpleDateFormat d = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy");
            ObjectNode n = JsonNodeFactory.instance.objectNode()
                    .put("title", name)
                    .put("desc", "")
                    .put("date", d.format(new Date(lastPlayed.toEpochMilli())) + "\n")
                    .put("filename", userDir.resolve("save games").relativize(path).toString())
                    .put("is_remote", false);
            JsonHelper.write(n, userDir.resolve("continue_game.json"));
        }

        @Override
        public String getModId(Path userDir, GameMod mod) {
            return mod.getName();
        }
    };

    GameInstallType STELLARIS = new StandardInstallType("stellaris") {
        @Override
        public Path chooseBackgroundImage(Path p) {
            int i = new Random().nextInt(16) + 1;
            return p.resolve("gfx").resolve("loadingscreens").resolve("load_" + i + ".dds");
        }

        @Override
        public List<String> getLaunchArguments() {
            return List.of("-gdpr-compliant", "--continuelastsave");
        }

        @Override
        public Optional<GameVersion> getVersion(String versionString) {
            Matcher m = Pattern.compile("(\\w)+\\s+v?(\\d)\\.(\\d+)\\.(\\d+).+").matcher(versionString);
            if (m.find()) {
                return Optional.of(new GameVersion(
                        Integer.parseInt(m.group(2)),
                        Integer.parseInt(m.group(3)),
                        Integer.parseInt(m.group(4)),
                        0,
                        m.group(1)));
            } else {
                return Optional.empty();
            }
        }

        @Override
        public void writeLaunchConfig(Path userDir, String name, Instant lastPlayed, Path path) throws IOException {
            var sgPath = FilenameUtils.getBaseName(
                    FilenameUtils.separatorsToUnix(userDir.relativize(path).toString()));
            ObjectNode n = JsonNodeFactory.instance.objectNode()
                    .put("title", sgPath)
                    .put("desc", name)
                    .put("date", "");
            JsonHelper.write(n, userDir.resolve("continue_game.json"));
        }
    };

    GameInstallType CK3 = new StandardInstallType("binaries/ck3") {
        @Override
        public Path chooseBackgroundImage(Path p) {
            String[] bgs = new String[] {"assassin", "baghdad", "castle", "council", "duel"};
            return p.resolve("game").resolve("gfx").resolve("interface").resolve("illustrations")
                    .resolve("loading_screens").resolve(bgs[new Random().nextInt(bgs.length)] + ".dds");
        }

        @Override
        public Optional<String> debugModeSwitch() {
            return Optional.of("-debug_mode");
        }

        @Override
        public List<String> getLaunchArguments() {
            return List.of("-gdpr-compliant", "--continuelastsave");
        }

        @Override
        public Optional<GameVersion> getVersion(String versionString) {
            Matcher m = Pattern.compile("(\\d)\\.(\\d+)\\.(\\d+)\\s+\\((\\w+)\\)").matcher(versionString);
            if (m.find()) {
                return Optional.of(new GameVersion(
                        Integer.parseInt(m.group(1)),
                        Integer.parseInt(m.group(2)),
                        Integer.parseInt(m.group(3)),
                        0,
                        m.group(4)));
            } else {
                return Optional.empty();
            }
        }

        @Override
        public Path getDlcPath(Path p) {
            return p.resolve("game").resolve("dlc");
        }

        @Override
        public Optional<Language> determineLanguage(Path dir, Path userDir) throws Exception {
            var sf = userDir.resolve("pdx_settings.txt");
            if (!Files.exists(sf)) {
                return Optional.empty();
            }

            var node = TextFormatParser.textFileParser().parse(sf);
            var langId = node.getNodeForKey("\"System\"")
                    .getNodeForKey("\"language\"").getNodeForKey("value").getString();
            return Optional.ofNullable(LanguageManager.getInstance().byId(langId));
        }

        public Path getSteamAppIdFile(Path p) {
            return p.resolve("binaries").resolve("steam_appid.txt");
        }

        public Path getLauncherDataPath(Path p) {
            return p.resolve("launcher");
        }

        public Path getModBasePath(Path p) {
            return p.resolve("game");
        }

        @Override
        public void writeLaunchConfig(Path userDir, String name, Instant lastPlayed, Path path) throws IOException {
            SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            var date = d.format(new Date(lastPlayed.toEpochMilli()));
            var sgPath = FilenameUtils.getBaseName(
                    FilenameUtils.separatorsToUnix(userDir.resolve("save games").relativize(path).toString()));
            ObjectNode n = JsonNodeFactory.instance.objectNode()
                    .put("title", sgPath)
                    .put("desc", "")
                    .put("date", date);
            JsonHelper.write(n, userDir.resolve("continue_game.json"));
        }
    };

    GameInstallType CK2 = new StandardInstallType("CK2game") {

        @Override
        public Optional<Path> getLauncherExecutable(Path p) {
            return Optional.of(getExecutable(p));
        }

        @Override
        public Optional<String> debugModeSwitch() {
            return Optional.of("-debug_mode");
        }

        @Override
        public Path chooseBackgroundImage(Path p) {
            return null;
        }

        @Override
        public List<String> getLaunchArguments() {
            return List.of();
        }

        @Override
        public void writeLaunchConfig(Path userDir, String name, Instant lastPlayed, Path path) throws IOException {

        }
    };

    Path chooseBackgroundImage(Path p);

    default Optional<GameVersion> determineVersionFromInstallation(Path p) {
        return Optional.empty();
    }

    List<String> getLaunchArguments();

    Path getExecutable(Path p);

    default Optional<GameVersion> getVersion(String versionString) {
        throw new UnsupportedOperationException();
    }

    default Optional<Language> determineLanguage(Path dir, Path userDir) throws Exception {
        return Optional.empty();
    }

    default Path getDlcPath(Path p) {
        return p.resolve("dlc");
    }

    default String getModId(Path userDir, GameMod mod) {
        return userDir.relativize(mod.getModFile()).toString();
    }

    public void writeLaunchConfig(Path userDir, String name, Instant lastPlayed, Path path) throws IOException;

    public default Path getSteamAppIdFile(Path p) {
        return p.resolve("steam_appid.txt");
    }

    public default Path getLauncherDataPath(Path p) {
        return p;
    }

    public default Path getModBasePath(Path p) {
        return p;
    }

    default Optional<Path> getLauncherExecutable(Path p) {
        return Optional.empty();
    }

    public default Optional<String> debugModeSwitch() {
        return Optional.empty();
    }

    public static abstract class StandardInstallType implements GameInstallType {

        private final String executableName;

        public StandardInstallType(String executableName) {
            this.executableName = executableName;
        }

        @Override
        public Path getExecutable(Path p) {
            return p.resolve(executableName + (SystemUtils.IS_OS_WINDOWS ? ".exe" : ""));
        }

        @Override
        public Optional<Language> determineLanguage(Path dir, Path userDir) throws Exception {
            var sf = userDir.resolve("settings.txt");
            if (!Files.exists(sf)) {
                return Optional.empty();
            }

            var node = TextFormatParser.textFileParser().parse(sf);
            var langId = node.getNodeForKey("language").getString();
            return Optional.ofNullable(LanguageManager.getInstance().byId(langId));
        }
    }
}
