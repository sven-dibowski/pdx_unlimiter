package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.eu4.savegame.RawSavegameVisitor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class FileImportTarget {

    private SavegameCache<?,?,?,?> savegameCache;
    protected Path path;

    public FileImportTarget(SavegameCache<?,?,?,?> savegameCache, Path path) {
        this.savegameCache = savegameCache;
        this.path = path;
    }

    public final boolean importTarget() {
        return savegameCache.importSavegame(path);
    }

    public Instant getLastModified() throws IOException {
        return Files.getLastModifiedTime(path).toInstant();
    }

    public abstract void delete() throws IOException;

    public abstract String getName();

    public Path getPath() {
        return path;
    }

    public static List<FileImportTarget> createTargets(Path toImport) {
        List<FileImportTarget> targets = new ArrayList<>();
        RawSavegameVisitor.vist(toImport, new RawSavegameVisitor() {
            @Override
            public void visitEu4(Path file) {
                targets.add(new StandardImportTarget(SavegameCache.EU4_CACHE, file));
            }

            @Override
            public void visitHoi4(Path file) {
                targets.add(new StandardImportTarget(SavegameCache.HOI4_CACHE, file));
            }

            @Override
            public void visitStellaris(Path file) {
                if (file.getFileName().toString().equals("ironman.sav")) {
                    targets.add(new StellarisIronmanImportTarget(file));
                } else {
                    targets.add(new StellarisNormalImportTarget(file));
                }
            }

            @Override
            public void visitCk3(Path file) {
                targets.add(new StandardImportTarget(SavegameCache.CK3_CACHE, file));
            }

            @Override
            public void visitOther(Path file) {
                if (Files.isDirectory(file)) {
                    try {
                        Files.list(file).forEach(f -> targets.addAll(FileImportTarget.createTargets(f)));
                    } catch (IOException e) {
                        ErrorHandler.handleException(e);
                    }
                }
            }
        });
        return targets;
    }

    public static final class StandardImportTarget extends FileImportTarget {

        public StandardImportTarget(SavegameCache<?, ?, ?, ?> savegameCache, Path path) {
            super(savegameCache, path);
        }

        @Override
        public void delete() throws IOException {
            Files.delete(path);
        }

        @Override
        public String getName() {
            return path.getFileName().toString();
        }
    }

    public static final class StellarisNormalImportTarget extends FileImportTarget {

        public StellarisNormalImportTarget(Path path) {
            super(SavegameCache.STELLARIS_CACHE, path);
        }

        @Override
        public void delete() throws IOException {
            Files.delete(path);
            if (Files.list(path.getParent()).count() == 0) {
                Files.delete(path.getParent());
            }
        }

        @Override
        public String getName() {
            return path.getParent().getFileName().toString() + " " + path.getFileName().toString();
        }
    }

    public static final class StellarisIronmanImportTarget extends FileImportTarget {

        public StellarisIronmanImportTarget(Path path) {
            super(SavegameCache.STELLARIS_CACHE, path);
        }

        @Override
        public void delete() throws IOException {
            Files.delete(path.getParent());
        }

        @Override
        public String getName() {
            return path.getParent().getFileName().toString();
        }
    }
}
