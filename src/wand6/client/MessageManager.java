package wand6.client;

import wand6.client.exceptions.MessageManagerException;
import wand6.client.messages.TextCloudMessage;
import wand6.client.messages.MapFragmentRequestMessage;
import wand6.client.messages.MoveRequestMessage;

class MessageManager {

    private static ServerInteraction inter = null;

    private static void checkInteraction() throws MessageManagerException {
        if (inter == null) {
            throw new MessageManagerException("Server interaction not set!");
        }
    }

    static void init(ServerInteraction inter) {
        MessageManager.inter = inter;
    }

    static void sendTextCloudMessage() throws NullPointerException, MessageManagerException {
        checkInteraction();
        inter.sendMessage(new TextCloudMessage(PlayerManager.getInstance().getSelfPlayerText()));
    }

    static void sendMapFragmentRequest(int idX, int idY) throws MessageManagerException {
        checkInteraction();
        inter.sendMessage(new MapFragmentRequestMessage(idX, idY));
    }

    static void sendMoveRequest(int x, int y) throws MessageManagerException {
        checkInteraction();
        inter.sendMessage(new MoveRequestMessage(x, y));
    }
}
