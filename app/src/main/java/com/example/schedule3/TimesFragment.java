package com.example.schedule3;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;

public class TimesFragment extends Fragment {
    private ScheduleRepository repository;
    private LiveData<Bitmap> mondayTimes;
    private LiveData<Bitmap> otherTimes;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = ScheduleApp.getInstance().getRepository();
        mondayTimes = repository.getMondayTimes();
        otherTimes = repository.getOtherTimes();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_times, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mondayTimes.observe(getViewLifecycleOwner(), bitmap -> {
            if(bitmap != null){
                ImageView mondayTimesView = view.findViewById(R.id.monday_times);
                mondayTimesView.setImageBitmap(bitmap);
                mondayTimesView.setVisibility(View.VISIBLE);
                view.findViewById(R.id.monday_times_stub).setVisibility(View.GONE);
            }
            else{
                view.findViewById(R.id.monday_times).setVisibility(View.GONE);
                view.findViewById(R.id.monday_times_stub).setVisibility(View.VISIBLE);
            }
        });
        otherTimes.observe(getViewLifecycleOwner(), bitmap -> {
            if(bitmap != null){
                ImageView otherTimesView = view.findViewById(R.id.other_times);
                otherTimesView.setImageBitmap(bitmap);
                otherTimesView.setVisibility(View.VISIBLE);
                view.findViewById(R.id.other_times_stub).setVisibility(View.GONE);
            }
            else{
                view.findViewById(R.id.other_times).setVisibility(View.GONE);
                view.findViewById(R.id.other_times_stub).setVisibility(View.VISIBLE);
            }
        });
    }
}