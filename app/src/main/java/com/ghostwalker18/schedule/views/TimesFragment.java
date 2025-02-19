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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.ghostwalker18.schedule.R;
import com.ghostwalker18.schedule.ScheduleApp;
import com.ghostwalker18.schedule.models.ScheduleRepository;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Этот класс представляет собой элемент интерфейса для отображения
 * расписания звонков.
 *
 * @author  Ипатов Никита
 * @since 1.0
 */
public class TimesFragment
        extends Fragment {
    private ScheduleRepository repository;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = ScheduleApp.getInstance().getScheduleRepository();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_times, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository.getMondayTimes().observe(getViewLifecycleOwner(), bitmap -> {
            if(bitmap != null){
                ((ImageView)view.findViewById(R.id.monday_times)).setImageBitmap(bitmap);
                view.findViewById(R.id.monday_times).setVisibility(View.VISIBLE);
                view.findViewById(R.id.monday_times_stub).setVisibility(View.GONE);
            }
            else{
                view.findViewById(R.id.monday_times).setVisibility(View.GONE);
                view.findViewById(R.id.monday_times_stub).setVisibility(View.VISIBLE);
            }
        });
        repository.getOtherTimes().observe(getViewLifecycleOwner(), bitmap -> {
            if(bitmap != null){
                ((ImageView)view.findViewById(R.id.other_times)).setImageBitmap(bitmap);
                view.findViewById(R.id.other_times).setVisibility(View.VISIBLE);
                view.findViewById(R.id.other_times_stub).setVisibility(View.GONE);
            }
            else{
                view.findViewById(R.id.other_times).setVisibility(View.GONE);
                view.findViewById(R.id.other_times_stub).setVisibility(View.VISIBLE);
            }
        });
    }
}