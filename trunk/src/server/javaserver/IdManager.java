package server.javaserver;

class IdManager {

    private static long id = 1L;

    static long nextId() {
        return id++;
    }
}
