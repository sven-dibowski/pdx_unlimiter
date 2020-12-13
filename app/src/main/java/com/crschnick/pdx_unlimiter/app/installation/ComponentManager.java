package com.crschnick.pdx_unlimiter.app.installation;

import com.crschnick.pdx_unlimiter.app.PdxuApp;
import com.crschnick.pdx_unlimiter.app.achievement.AchievementManager;
import com.crschnick.pdx_unlimiter.app.game.GameAppManager;
import com.crschnick.pdx_unlimiter.app.game.GameInstallation;
import com.crschnick.pdx_unlimiter.app.game.GameIntegration;
import com.crschnick.pdx_unlimiter.app.gui.GameImage;
import com.crschnick.pdx_unlimiter.app.gui.GuiLayout;
import com.crschnick.pdx_unlimiter.app.savegame.FileImporter;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCache;
import javafx.application.Platform;
import org.jnativehook.GlobalScreen;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

public class ComponentManager {

    public static void initialSetup(String[] args) {
        try {
            PdxuInstallation.init();
            LogManager.init();
            ErrorHandler.init();

            LoggerFactory.getLogger(PdxuApp.class).info("Running pdxu with arguments: " + Arrays.toString(args));
            Arrays.stream(args)
                    .map(Path::of)
                    .forEach(FileImporter::addToImportQueue);

            if (!PdxuInstallation.shouldStart()) {
                System.exit(0);
            }
        } catch (Exception e) {
            ErrorHandler.handleTerminalException(e);
        }
    }

    public static void additionalSetup() {
        Platform.runLater(() -> ErrorHandler.registerThread(Thread.currentThread()));
        TaskExecutor.getInstance().start();
        TaskExecutor.getInstance().submitTask(ComponentManager::init);
    }

    public static void reloadSettings() {
        TaskExecutor.getInstance().stopAndWait();
        TaskExecutor.getInstance().start();
        TaskExecutor.getInstance().submitTask(() -> {
            reset();
            Settings.getInstance().apply();
            init();
        });
    }

    public static void finalTeardown() {
        TaskExecutor.getInstance().submitTask(ComponentManager::reset);
        TaskExecutor.getInstance().stopAndWait();
    }

    private static void init() {
        try {
            Settings.init();

            GuiLayout.init();

            GameInstallation.init();
            GameImage.init();
            AchievementManager.init();
            SavegameCache.init();

            GameAppManager.init();
            FileImporter.init();

            GameIntegration.init();

            FileWatchManager.init();

            if (PdxuInstallation.getInstance().isNativeHookEnabled()) {
                GlobalScreen.unregisterNativeHook();
            }
        } catch (Exception e) {
            ErrorHandler.handleTerminalException(e);
        }
    }

    private static void reset() {
        try {
            FileWatchManager.reset();
            GameIntegration.reset();

            // Sync with platform thread after GameIntegration reset
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(latch::countDown);
            latch.await();

            SavegameCache.reset();
            GameInstallation.reset();
            if (PdxuInstallation.getInstance().isNativeHookEnabled()) {
                GlobalScreen.unregisterNativeHook();
            }
        } catch (Exception e) {
            ErrorHandler.handleException(e);
        }

    }
}