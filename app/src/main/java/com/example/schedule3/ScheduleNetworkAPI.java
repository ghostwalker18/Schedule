package com.example.schedule3;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface ScheduleNetworkAPI {
   @GET(ScheduleApp.mondayTimesURL)
   Call<ResponseBody> getMondayTimes();

   @GET(ScheduleApp.otherTimesURL)
   Call<ResponseBody> getOtherTimes();

   @GET
   Call<ResponseBody> getScheduleFile(@Url String url);
}