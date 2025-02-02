/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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