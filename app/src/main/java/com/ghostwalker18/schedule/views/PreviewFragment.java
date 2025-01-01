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

package com.ghostwalker18.schedule.views;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ghostwalker18.schedule.OnSwipeListener;
import com.ghostwalker18.schedule.R;
import java.util.ArrayList;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Этот класс используется для отображения пролистываемого списка изображений
 * с возможностью удаления текущего.
 *
 * @author Ипатов Никита
 * @since 4.0
 */
public  class PreviewFragment
        extends Fragment {
   /**
    * Этот интерфейс задает слушателя события удаления изображения из галереи превью.
    */
   public interface DeleteListener{
      void onPhotoDelete(Uri uri);
   }
   private boolean isEditable = false;
   private int currentItem = 0;
   private ArrayList<Uri> photoUris;
   private DeleteListener listener;
   private ImageView preview;
   private ImageButton previousButton;
   private ImageButton nextButton;
   private ImageButton deleteButton;
   private TextView imageCounterView;

   @Nullable
   @Override
   public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
      return inflater.inflate(R.layout.fragment_preview, container, false);
   }

   @SuppressLint("ClickableViewAccessibility")
   @Override
   public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);
      preview = view.findViewById(R.id.preview);
      if(savedInstanceState != null){
         photoUris = savedInstanceState.getParcelableArrayList("uris");
         currentItem = savedInstanceState.getInt("current_item");
         isEditable = savedInstanceState.getBoolean("is_editable");
      }
      preview.setOnTouchListener(new OnSwipeListener(requireContext()){
         @Override
         public void onSwipeTop(){
            deletePhoto();
         }
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
            Intent intent = new Intent(requireActivity(), PhotoViewActivity.class);
            intent.putExtra("photo_uri", photoUris.get(currentItem).toString());
            ActivityOptions options = ActivityOptions
                    .makeSceneTransitionAnimation(getActivity(), preview, "photo");
            startActivity(intent, options.toBundle());
         }
      });
      deleteButton = view.findViewById(R.id.delete);
      if(isEditable)
         deleteButton.setVisibility(View.VISIBLE);
      else
         deleteButton.setVisibility(View.GONE);
      deleteButton.setOnClickListener(view1 -> deletePhoto());
      previousButton = view.findViewById(R.id.previous);
      nextButton = view.findViewById(R.id.next);
      previousButton.setOnClickListener(view1 -> showPreviousPhoto());
      nextButton.setOnClickListener(view1 -> showNextPhoto());
      imageCounterView = view.findViewById(R.id.image_counter);
      invalidatePreview();
   }

   @Override
   public void onSaveInstanceState(@NonNull Bundle outState) {
      outState.putParcelableArrayList("uris", photoUris);
      outState.putInt("current_item", currentItem);
      outState.putBoolean("is_editable", isEditable);
      super.onSaveInstanceState(outState);
   }

   /**
    * Этот метод используется для задания списка URI отображаемых фотографий.
    * @param uris отображаемые фотографии
    */
   public void setImageIDs(ArrayList<Uri> uris){
      photoUris = uris;
      invalidatePreview();
   }

   /**
    * Этот метод задает слушателя события удаления фото.
    */
   public void setListener(DeleteListener listener){
      this.listener = listener;
   }

   /**
    * Этот метод задает возможность удаления фотографий из списка.
    * @param editable возможность удаления
    */
   public void setEditable(boolean editable){
      isEditable = editable;
   }

   private void invalidatePreview(){
      if(isVisible()){
         if(photoUris != null && photoUris.size() > 0){
            preview.setImageURI(photoUris.get(photoUris.size() - 1));
            prepareImagesCounterView();
            if(isEditable)
               deleteButton.setVisibility(View.VISIBLE);
         }
         else
            deleteButton.setVisibility(View.GONE);
      }
      if(photoUris != null && photoUris.size() > 1){
         previousButton.setVisibility(View.VISIBLE);
         nextButton.setVisibility(View.VISIBLE);
      }
      else{
         previousButton.setVisibility(View.INVISIBLE);
         nextButton.setVisibility(View.INVISIBLE);
      }
   }

   /**
    * Этот метод используется для отображения следущего фото.
    */
   private void showNextPhoto(){
      if(photoUris.size() != 0){
         currentItem++;
         if(currentItem >= photoUris.size())
            currentItem = 0;
         preview.setImageURI(photoUris.get(currentItem));
         prepareImagesCounterView();
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
         preview.setImageURI(photoUris.get(currentItem));
         prepareImagesCounterView();
      }
   }

   /**
    * Этот метод используется для удаления текущей фотографии из списка
    */
   private void deletePhoto(){
      if(isEditable && photoUris.size() != 0){
         Uri deletedUri = photoUris.remove(currentItem);
         if(photoUris.size() == 0){
            preview.setImageResource(R.drawable.baseline_no_photography_72);
            deleteButton.setVisibility(View.GONE);
            return;
         }
         currentItem--;
         if(currentItem < 0)
            currentItem = photoUris.size() - 1;
         if(currentItem < photoUris.size())
            preview.setImageURI(photoUris.get(currentItem));
         if(listener != null)
            listener.onPhotoDelete(deletedUri);
      }
   }

   /**
    * Этот метод используется для обновления отображаемой информации
    * о количестве фото и текущей фотографии.
    */
   private void prepareImagesCounterView(){
      imageCounterView.setText(String.format(
              new Locale("ru"),"%d/%d",
              currentItem + 1, photoUris.size()));
   }
}