package common;

/** Shared by the login dialog, automated clients and the server's text protocol. */
public final class PlayerName {
    private PlayerName() { }
    public static boolean isValid(String name) {
        return name != null && name.length() >= 3 && name.length() <= 10
                && name.indexOf('"') < 0 && name.indexOf('\n') < 0 && name.indexOf('\r') < 0;
    }
    public static String requireValid(String name) {
        if (!isValid(name)) throw new IllegalArgumentException("Nick must be 3-10 symbols long, without quotes or line breaks.");
        return name;
    }
}
