package wand6.server;

import wand6.common.messages.Message;
import wand6.common.exceptions.MessageManagerException;
import wand6.server.exceptions.JavaServerException;
import wand6.server.messages.*;

final class MessageManager {
    private JavaServer server;
    private PlayerManager players;
    void init(JavaServer server, PlayerManager players) { this.server = server; this.players = players; }
    void send(long recipient, Message message) {
        try { server.sendMessage(message, recipient); }
        catch (JavaServerException ex) { throw new IllegalStateException(ex); }
    }
    void sendWelcome(long id) throws MessageManagerException {
        try {
            send(id, new WelcomeMessage(id, players.getPlayerName(id), players.getPlayerSpriteSetName(id),
                    players.getPlayerCurX(id), players.getPlayerCurY(id)));
        } catch (wand6.server.exceptions.PlayerManagerException ex) { throw new IllegalStateException(ex); }
    }
    void sendMapFragment(long id, SMapFragment fragment) throws MessageManagerException {
        send(id, new MapFragmentMessage(fragment.getIdX(), fragment.getIdY(), fragment.getHmap()));
    }
}
