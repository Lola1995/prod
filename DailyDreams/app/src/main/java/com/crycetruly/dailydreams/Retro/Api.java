package com.crycetruly.dailydreams.Retro;

import com.crycetruly.dailydreams.models.Day;

import java.util.List;


import io.reactivex.Observable;
import retrofit2.http.GET;

public interface Api {
    @GET("days")
    Observable<List<Day>> getDays();
}
