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

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.NonNull;

/**
 * Этот класс используется для реализации обработки смахивания вправо или влево.
 *
 * @author Ипатов Никита
 * @since 4.0
 */
public class OnSwipeListener
        implements View.OnTouchListener {
   private final GestureDetector gestureDetector;


   public OnSwipeListener(Context context) {
      gestureDetector = new GestureDetector(context, new GestureListener());
   }

   /**
    * Этот метод используется для обработки смахивания вверх.
    */
   public void onSwipeTop() {/*To override*/}

   /**
    * Этот метод используется для обработки смахивания вниз.
    */
   public void onSwipeBottom() {/*To override*/}

   /**
    * Этот метод используется для обработки смахивания влево.
    */
   public void onSwipeLeft() {/*To override*/}

   /**
    * Этот метод используется для обработки смахивания вправо.
    */
   public void onSwipeRight() {/*To override*/}

   public boolean onTouch(View v, MotionEvent event) {
      return gestureDetector.onTouchEvent(event);
   }

   private final class GestureListener
           extends GestureDetector.SimpleOnGestureListener {
      private static final int SWIPE_DISTANCE_THRESHOLD = 100;
      private static final int SWIPE_VELOCITY_THRESHOLD = 100;

      @Override
      public boolean onFling(@NonNull MotionEvent e1, @NonNull MotionEvent e2,
                             float velocityX, float velocityY) {
         float distanceX = e2.getX() - e1.getX();
         float distanceY = e2.getY() - e1.getY();
         if (Math.abs(distanceX) > Math.abs(distanceY)
                 && Math.abs(distanceX) > SWIPE_DISTANCE_THRESHOLD
                 && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
            if (distanceX > 0)
               onSwipeRight();
            else
               onSwipeLeft();
            return true;
         }
         if (Math.abs(distanceY) > Math.abs(distanceX)
                 && Math.abs(distanceY) > SWIPE_DISTANCE_THRESHOLD
                 && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
            if (distanceY > 0)
               onSwipeBottom();
            else
               onSwipeTop();
            return true;
         }
         return false;
      }
   }
}