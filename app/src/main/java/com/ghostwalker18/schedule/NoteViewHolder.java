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

package com.ghostwalker18.schedule;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_MEDIA_IMAGES;
import static android.Manifest.permission.READ_MEDIA_VIDEO;
import static android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Этот класс служит для работы с элементом списка.
 *
 * @author Ипатов Никита
 * @since 3.2
 */
public class NoteViewHolder
        extends RecyclerView.ViewHolder {
   private Context context;
   public boolean isSelected = false;
   private int currentItem = 0;
   private ArrayList<Uri> photoUris;
   private final TextView theme, text, date, error;
   private final ImageView checked, preview;

   @SuppressLint("ClickableViewAccessibility")
   public NoteViewHolder(@NonNull View itemView, Context context) {
      super(itemView);
      this.context = context;
      date = itemView.findViewById(R.id.date);
      theme = itemView.findViewById(R.id.theme);
      text = itemView.findViewById(R.id.text);
      error = itemView.findViewById(R.id.error);
      checked = itemView.findViewById(R.id.checked);
      preview = itemView.findViewById(R.id.preview);
      preview.setOnTouchListener(new OnSwipeListener(context){
         @Override
         public void onSwipeLeft() {
            showNextPhoto();
         }
         @Override
         public void onSwipeRight() {
            showPreviousPhoto();
         }
      });
      preview.setOnClickListener(view1 -> {
         if(photoUris != null && photoUris.size() != 0){
            Intent intent = new Intent(context, PhotoViewActivity.class);
            intent.putExtra("photo_uri", photoUris.get(currentItem).toString());
            ActivityOptions options = ActivityOptions
                    .makeSceneTransitionAnimation((AppCompatActivity)context, preview, "photo");
            context.startActivity(intent, options.toBundle());
         }
      });
      itemView.findViewById(R.id.previous).setOnClickListener(view1 -> showPreviousPhoto());
      itemView.findViewById(R.id.next).setOnClickListener(view1 -> showNextPhoto());
   }

   public void setSelected(boolean selected) {
      isSelected = selected;
      if(isSelected)
         checked.setVisibility(View.VISIBLE);
      else
         checked.setVisibility(View.GONE);
   }

   /**
    * Этот метод используется для задания заметки для отображения.
    * @param note отображаемая заметка
    */
   public void setNote(Note note){
      date.setText(DateConverters.toString(note.date));
      theme.setText(note.theme);
      text.setText(note.text);
      photoUris = note.photoIDs;
      if(photoUris != null && photoUris.size() > 1){
         itemView.findViewById(R.id.previous).setVisibility(View.VISIBLE);
         itemView.findViewById(R.id.next).setVisibility(View.VISIBLE);
      }
      else{
         itemView.findViewById(R.id.previous).setVisibility(View.INVISIBLE);
         itemView.findViewById(R.id.next).setVisibility(View.INVISIBLE);
      }
      if(checkPhotoAccess()){
         try {
            if(photoUris != null && photoUris.size() > 0)
               preview.setImageURI(photoUris.get(photoUris.size() - 1));
         }
         catch (Exception e) {
            error.setText(context.getString(R.string.photo_error));
         }
      }
      if(note.photoIDs != null && !checkPhotoAccess())
         error.setText(R.string.gallery_access_denied);
   }

   /**
    * Этот метод используется для отображения следущего фото.
    */
   private void showNextPhoto(){
      if(photoUris.size() != 0){
         currentItem++;
         if(currentItem >= photoUris.size())
            currentItem = 0;
         try{
            preview.setImageURI(photoUris.get(currentItem));
         }
         catch (Exception e){
            error.setText(context.getString(R.string.photo_error));
         }
      }
   }

   /**
    * Этот метод используется для отображения предыдущего фото
    */
   private void showPreviousPhoto(){
      if(photoUris.size() != 0){
         currentItem--;
         if(currentItem < 0)
            currentItem = photoUris.size() - 1;
         try{
            preview.setImageURI(photoUris.get(currentItem));
         }
         catch (Exception e){
            error.setText(context.getString(R.string.photo_error));
         }
      }
   }

   /**
    * Этот метод используется для проверки, есть ли у приложения доступ к фото в галерее.
    * @return наличие доступа к фото
    */
   private boolean checkPhotoAccess() {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
              (
                      ContextCompat.checkSelfPermission(context, READ_MEDIA_IMAGES) == PERMISSION_GRANTED ||
                              ContextCompat.checkSelfPermission(context, READ_MEDIA_VIDEO) == PERMISSION_GRANTED
              )
      ) {
         return true;
      } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
              ContextCompat.checkSelfPermission(context, READ_MEDIA_VISUAL_USER_SELECTED) == PERMISSION_GRANTED
      ) {
         return true;
      }  else return ContextCompat.checkSelfPermission(context, READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED;
   }
}