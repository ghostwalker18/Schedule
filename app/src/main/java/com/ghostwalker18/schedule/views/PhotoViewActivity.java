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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import com.ghostwalker18.schedule.R;
import java.io.File;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

/**
 * Этот класс используется для просмотра фото заметки в отдельном экране.
 *
 * @author Ипатов Никита
 * @since 4.0
 */
public class PhotoViewActivity
        extends AppCompatActivity {
    private Uri photoUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view);
        Bundle bundle = getIntent().getExtras();
        if(bundle == null){
            bundle = savedInstanceState;
        }
        photoUri = Uri.parse(bundle.getString("photo_uri"));
        ((ImageView) findViewById(R.id.photo)).setImageURI(photoUri);
        findViewById(R.id.back_button).setOnClickListener(view -> finishAfterTransition());
        findViewById(R.id.share_button).setOnClickListener(view -> sharePhoto());
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("photo_uri", photoUri.toString());
        super.onSaveInstanceState(outState);
    }

    /**
     * Этот метод используетсяя чтобы поделиться отображаемым фото.
     */
    private void sharePhoto(){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        Uri shareUri = FileProvider.getUriForFile(this,
                "com.ghostwalker18.schedule.timefilesprovider", new File(photoUri.getPath()));
        shareIntent.putExtra(Intent.EXTRA_STREAM, shareUri);
        startActivity(Intent.createChooser(shareIntent, null));
    }
}