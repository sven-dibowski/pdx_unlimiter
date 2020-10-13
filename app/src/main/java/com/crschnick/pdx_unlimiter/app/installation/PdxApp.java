package com.crschnick.pdx_unlimiter.app.installation;

import java.util.Optional;

public class PdxApp {

    public static enum Type {

        EU4("Europa Universalis IV");

        public final String name;

        Type(String name) {
            this.name = name;
        }
    }


    private static Optional<PdxApp> ACTIVE_APP = Optional.empty();

    static {
        Thread t = new Thread(() -> {
            while (true) {
                if (!ACTIVE_APP.isPresent()) {
                    Optional<ProcessHandle> h = ProcessHandle
                            .allProcesses()
                            .filter(p -> p.info().command().map(c -> c.contains("eu4")).orElse(false))
                            .findFirst();
                    if (h.isPresent()) {
                        ACTIVE_APP = Optional.of(new Eu4App(h.get()));
                    }
                } else {
                    if (!ACTIVE_APP.get().isAlive()) {
                        ACTIVE_APP = Optional.empty();
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public static Optional<PdxApp> getActiveApp() {
        return ACTIVE_APP;
    }

    private Type type;

    private ProcessHandle process;

    public PdxApp(ProcessHandle process, Type type) {
        this.process = process;
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public boolean isAlive() {
        return process.isAlive();
    }

    public void kill() {
        process.destroyForcibly();
    }
}