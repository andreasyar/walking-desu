package wand6.client;

import wand6.client.messages.TextCloudMessage;

class MessageManager {

    private static MessageManager self = null;

    static MessageManager getInstance() {
        if (self == null) {
            self = new MessageManager();
        }

        return self;
    }

    private MessageManager() {}

    TextCloudMessage getTextCloudMessage(long playerId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
