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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.ghostwalker18.schedule.R;
import com.ghostwalker18.schedule.models.Note;
import com.ghostwalker18.schedule.views.NoteViewHolder;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Этот класс служит для отображения списка заметок.
 *
 * @author Ипатов Никита
 * @since 3.0
 * @see NoteViewHolder
 */
public class NoteAdapter
        extends RecyclerView.Adapter<NoteViewHolder> {
   public interface OnNoteClickListener {
      void onNoteSelected(Note note, int position);
      void onNoteUnselected(Note note, int position);
   }
   private final Note[] notes;
   private final OnNoteClickListener listener;
   private boolean isClickable = true;

   public NoteAdapter(Note[] notes, OnNoteClickListener listener) {
      this.notes = notes;
      this.listener = listener;
   }

   @NonNull
   @Override
   public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      LayoutInflater inflater = LayoutInflater.from(parent.getContext());
      View view = inflater.inflate(R.layout.fragment_note, parent, false);
      return new NoteViewHolder(view, parent.getContext());
   }

   @Override
   public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
      Note note = notes[position];
      holder.setNote(note);
      holder.itemView.setOnClickListener(v -> {
         if(!isClickable)
            return;
         holder.setSelected(!holder.isSelected);
         if(holder.isSelected){
            listener.onNoteSelected(note, position);
         }
         else{
            listener.onNoteUnselected(note, position);
         }
      });
   }

   public void setClickable(boolean clickable){
      isClickable = clickable;
   }

   @Override
   public int getItemCount() {
      return notes.length;
   }
}