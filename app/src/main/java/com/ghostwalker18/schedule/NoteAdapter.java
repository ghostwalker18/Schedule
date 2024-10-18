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

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_MEDIA_IMAGES;
import static android.Manifest.permission.READ_MEDIA_VIDEO;
import static android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Этот класс служит для отображения списка заметок.
 *
 * @author Ипатов Никита
 * @since 3.0
 */
public class NoteAdapter
        extends RecyclerView.Adapter<NoteAdapter.ViewHolder> {
   private final Note[] notes;
   private Context context;
   private boolean canAccessPhoto = false;

   public NoteAdapter(Note[] notes) {
      this.notes = notes;
   }

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
      }  else if (ContextCompat.checkSelfPermission(context, READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED) {
         return true;
      } else {
         return false;
      }
   }

   @NonNull
   @Override
   public NoteAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      context = parent.getContext();
      LayoutInflater inflater = LayoutInflater.from(context);
      View view = inflater.inflate(R.layout.fragment_note, parent, false);
      return new ViewHolder(view);
   }

   @Override
   public void onBindViewHolder(@NonNull NoteAdapter.ViewHolder holder, int position) {
      Note note = notes[position];
      holder.date.setText(DateConverters.toString(note.date));
      holder.theme.setText(note.theme);
      holder.text.setText(note.text);
      canAccessPhoto = checkPhotoAccess();
      if(note.photoID != null && canAccessPhoto){
         ContentResolver contentResolver = context.getContentResolver();
         try {
            holder.photo.setImageBitmap(BitmapFactory.decodeStream(
                    contentResolver.openInputStream(Uri.parse(note.photoID))));
         } catch (Exception ignored) {
            holder.error.setText(context.getString(R.string.photo_error));
         }
      }
   }

   @Override
   public int getItemCount() {
      return notes.length;
   }

   /**
    * Этот класс служит для работы с элементом списка.
    *
    * @author Ипатов Никита
    * @since 3.0
    */
   public static class ViewHolder
           extends RecyclerView.ViewHolder {
      private final TextView theme, text, date, error;
      private final ImageView photo;

      public ViewHolder(@NonNull View itemView) {
         super(itemView);
         date = itemView.findViewById(R.id.date);
         theme = itemView.findViewById(R.id.theme);
         text = itemView.findViewById(R.id.text);
         error = itemView.findViewById(R.id.error);
         photo = itemView.findViewById(R.id.image);
      }
   }
}