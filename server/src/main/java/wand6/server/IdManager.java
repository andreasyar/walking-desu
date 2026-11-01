package wand6.server;

class IdManager {

    private static long id = 1L;

    static long nextId() {
        return id++;
    }
}
