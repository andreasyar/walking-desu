package client;

import common.PlayerName;

/** Explicit opt-in; never changes interactive login by default. */
final class ClientTestMode {
    static String nickname() {
        if (!Boolean.getBoolean("walkingdesu.testMode")) return null;
        return PlayerName.requireValid(System.getProperty("walkingdesu.testNick"));
    }
}
