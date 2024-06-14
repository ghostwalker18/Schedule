package com.example.schedule3;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ScheduleNetworkAPI {
   @GET("https://r1.nubex.ru/s1748-17b/47698615b7_fit-in~1280x800~filters:no_upscale()__f44488_08.jpg")
   Call<ResponseBody> getMondayTimes();

   @GET("https://r1.nubex.ru/s1748-17b/320e9d2d69_fit-in~1280x800~filters:no_upscale()__f44489_bb.jpg")
   Call<ResponseBody> getOtherTimes();

   @GET()
   Call<ResponseBody> getScheduleFile();
}