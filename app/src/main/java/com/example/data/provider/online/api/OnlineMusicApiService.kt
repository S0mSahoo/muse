package com.example.data.provider.online.api

import com.example.data.provider.online.dto.ITunesSearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface OnlineMusicApiService {

    @GET("search")
    suspend fun searchSongs(
        @Query("term") term: String,
        @Query("entity") entity: String = "song",
        @Query("media") media: String = "music",
        @Query("limit") limit: Int = 30
    ): ITunesSearchResponseDto

    @GET("lookup")
    suspend fun lookupById(
        @Query("id") id: String,
        @Query("entity") entity: String = "song",
        @Query("limit") limit: Int = 20
    ): ITunesSearchResponseDto
}
