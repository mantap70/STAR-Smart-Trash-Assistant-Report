package com.mantao.star;

/**
 * Model sederhana untuk satu pesan dalam percakapan chatbot.
 */
public class ChatMessage {

    private final String text;
    private final boolean fromUser;
    private final long timestamp;

    public ChatMessage(String text, boolean fromUser) {
        this.text = text;
        this.fromUser = fromUser;
        this.timestamp = System.currentTimeMillis();
    }

    public String getText() {
        return text;
    }

    public boolean isFromUser() {
        return fromUser;
    }

    public long getTimestamp() {
        return timestamp;
    }
}