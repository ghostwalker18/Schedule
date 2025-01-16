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

package com.ghostwalker18.schedule.system;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * Этот класс используется как адаптер интерфейса TextWatcher,
 * чтобы избежать реализации ненужных методов.
 *
 * @author Ипатов Никита
 * @since 4.0
 */
public class TextWatcherAdapter
        implements TextWatcher {
   @Override
   public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {/*To be implemented*/}

   @Override
   public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {/*To be implemented*/}

   @Override
   public void afterTextChanged(Editable editable) {/*To be implemented*/}
}