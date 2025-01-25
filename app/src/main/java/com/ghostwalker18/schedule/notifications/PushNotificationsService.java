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

import java.util.List;
import androidx.annotation.NonNull;
import ru.rustore.sdk.pushclient.messaging.exception.RuStorePushClientException;
import ru.rustore.sdk.pushclient.messaging.model.RemoteMessage;
import ru.rustore.sdk.pushclient.messaging.service.RuStoreMessagingService;

/**
 * Этот класс используется для обработки Push-уведомленй.
 *
 * @author Ипатов Никита
 * @since 4.1
 */
public class PushNotificationsService
        extends RuStoreMessagingService {
    @Override
    public void onNewToken(@NonNull String token) {

    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {

    }

    @Override
    public void onDeletedMessages() {

    }

    @Override
    public void onError(@NonNull List<? extends RuStorePushClientException> errors) {

    }
}