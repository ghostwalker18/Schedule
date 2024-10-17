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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Этот класс служит для отображения списка заметок.
 *
 * @author Ипатов Никита
 * @since 3.0
 */
public class NoteAdapter
        extends RecyclerView.Adapter<NoteAdapter.ViewHolder> {
   private final Note[] notes;

   public NoteAdapter(Note[] notes) {
      this.notes = notes;
   }

   @NonNull
   @Override
   public NoteAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      LayoutInflater inflater = LayoutInflater.from(parent.getContext());
      View view = inflater.inflate(R.layout.fragment_note, parent, false);
      return new ViewHolder(view);
   }

   @Override
   public void onBindViewHolder(@NonNull NoteAdapter.ViewHolder holder, int position) {
      Note note = notes[position];
      holder.date.setText(DateConverters.toString(note.date));
      holder.theme.setText(note.theme);
      holder.text.setText(note.text);
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
      private final TextView theme, text, date;
      private final ImageView photo;

      public ViewHolder(@NonNull View itemView) {
         super(itemView);
         date = itemView.findViewById(R.id.date);
         theme = itemView.findViewById(R.id.theme);
         text = itemView.findViewById(R.id.text);
         photo = itemView.findViewById(R.id.image);
      }
   }
}