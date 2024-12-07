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

import android.app.Dialog;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

/**
 * Этот класс используется для скачивания файлов с подтверждением загрузки.
 *
 * @author Ипатов Никита
 * @since 3.2
 */
public class DownloadDialog
        extends DialogFragment {
   private String[] links;
   private String mimeTypeOfFilesToDownload;
   private String downloadTitle;
   private final DialogInterface.OnClickListener listener = (dialogInterface, which) -> {
      if(which == Dialog.BUTTON_POSITIVE){
         new Thread(() -> {
            DownloadManager downloadManager = requireActivity().getSystemService(DownloadManager.class);
            for(String link : links){
               DownloadManager.Request request = new DownloadManager.Request(Uri.parse(link))
                       .setMimeType(mimeTypeOfFilesToDownload)
                       .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                       .setTitle(downloadTitle)
                       .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                               Utils.getNameFromLink(link));
               downloadManager.enqueue(request);
            }
         }).start();
      }
      else{
         dismiss();
      }
   };

   @NonNull
   public Dialog onCreateDialog(Bundle savedInstanceState) {
      links = getArguments().getStringArray("links");
      downloadTitle = getArguments().getString("download_title");
      mimeTypeOfFilesToDownload = getArguments().getString("mime_type");
      int numberOfFiles = getArguments().getInt("number_of_files");
      AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
      return builder
              .setTitle(R.string.download_approvement)
              .setIcon(R.drawable.baseline_download_36)
              .setMessage(getResources().getString(R.string.download_notice, numberOfFiles))
              .setPositiveButton(R.string.download_ok, listener)
              .setNegativeButton(R.string.download_cancel, null)
              .create();
   }
}