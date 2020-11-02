package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.app.savegame.Eu4SavegameImporter;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

public class Eu4KeyListener implements NativeKeyListener {

    private Eu4App handle;

    public Eu4KeyListener(Eu4App handle) {
        this.handle = handle;
    }

    public void nativeKeyPressed(NativeKeyEvent e) {
        if ((e.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0 && (e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0) {
            if (e.getKeyCode() == NativeKeyEvent.VC_K) {
                handle.kill();
            }
            if (e.getKeyCode() == NativeKeyEvent.VC_I) {
                Eu4SavegameImporter.importLatestSavegame();
            }
        }
    }

    public void nativeKeyReleased(NativeKeyEvent e) {
    }

    public void nativeKeyTyped(NativeKeyEvent e) {
    }
}