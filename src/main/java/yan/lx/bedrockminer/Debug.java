package yan.lx.bedrockminer;

public class Debug {
    public static void info(String msg) {
        BedrockMinerMod.LOGGER.info(msg);
    }

    public static void info() {
        info("");
    }

    public static void info(String msgFormat, Object... args) {
        info(String.format(msgFormat, args));
    }

    public static void info(Object obj) {
        info(obj.toString());
    }
}
