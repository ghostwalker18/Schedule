package com.example.schedule3;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class TimesFragment extends Fragment {
    private ScheduleRepository repository;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = ScheduleApp.getInstance().getRepository();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
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