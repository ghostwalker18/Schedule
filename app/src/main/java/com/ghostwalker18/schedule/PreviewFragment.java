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

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Этот класс используется для отображения пролистываемого списка изображений
 * с возможностью удаления текущего.
 *
 * @author Ипатов Никита
 * @since 3.2
 */
public  class PreviewFragment
        extends Fragment {
   public interface DeleteListener{
      void onPhotoDelete(int position);
   }
   private boolean isEditable = false;
   private int currentItem = 0;
   private ArrayList<Uri> photoUris;
   private DeleteListener listener;
   private View view;

   @Nullable
   @Override
   public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
      return inflater.inflate(R.layout.fragment_preview, container, false);
   }

   @Override
   public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);
      this.view = view;
      if(isEditable)
         view.findViewById(R.id.delete).setVisibility(View.VISIBLE);
      else
         view.findViewById(R.id.delete).setVisibility(View.GONE);
      view.findViewById(R.id.delete).setOnClickListener(view1 -> deletePhoto());
      view.findViewById(R.id.previous).setOnClickListener(view1 -> showPreviousPhoto());
      view.findViewById(R.id.next).setOnClickListener(view1 -> showNextPhoto());
   }

   /**
    * Этот метод используется для задания списка URI отображаемых фотографий.
    * @param uris отображаемые фотографии
    */
   public void setPhotosIDs(ArrayList<Uri> uris){
      photoUris = uris;
   }

   /**
    * Этот метод задает слушателя события удаления фото.
    */
   public void setListener(DeleteListener listener){
      this.listener = listener;
   }

   public void setEditable(boolean editable){
      isEditable = editable;
      if(isEditable)
         view.findViewById(R.id.delete).setVisibility(View.VISIBLE);
      else
         view.findViewById(R.id.delete).setVisibility(View.GONE);
   }

   /**
    * Этот метод используется для отображения следущего фото.
    */
   private void showNextPhoto(){
      currentItem++;
      if(currentItem > photoUris.size())
         currentItem = 0;
      ((ImageView) view.findViewById(R.id.preview)).setImageURI(photoUris.get(currentItem));
   }

   /**
    * Этот метод используется для отображения предыдущего фото
    */
   private void showPreviousPhoto(){
      currentItem--;
      if(currentItem < 0)
         currentItem = photoUris.size() - 1;
      ((ImageView) view.findViewById(R.id.preview)).setImageURI(photoUris.get(currentItem));
   }

   /**
    * Этот метод используется для удаления текущей фотографии из списка
    */
   private void deletePhoto(){
      if(isEditable){
         photoUris.remove(currentItem);
         if(currentItem < photoUris.size())
            ((ImageView) view.findViewById(R.id.preview))
                    .setImageURI(photoUris.get(currentItem));
         if(listener != null)
            listener.onPhotoDelete(currentItem);
      }
   }
}