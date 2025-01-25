package com.ghostwalker18.schedule.notifications;

/**
 * Этот класс представляет собой модель Push-сообщения
 *
 * @author Ипатов Никита
 * @author RuStore
 * @since 4.1
 */
public final class AppNotification {
    private final int id;
    private final String title;
    private final String message;
    private final String channelId;
    private final String channelName;

    public AppNotification(int id, String title, String message, String channelId, String channelName) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.channelId = channelId;
        this.channelName = channelName;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getChannelName() {
        return channelName;
    }
}