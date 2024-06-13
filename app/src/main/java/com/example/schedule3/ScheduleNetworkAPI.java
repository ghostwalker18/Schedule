package com.example.schedule3;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ScheduleNetworkAPI {
   @GET("9006/#gallery-1")
   Call<ResponseBody> getMondayTimes();

   @GET("9006/#gallery-2")
   Call<ResponseBody> getOtherTimes();

   @GET()
   Call<ResponseBody> getScheduleFile();
}