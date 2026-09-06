package com.example.data.provider.youtube.api

import com.example.data.provider.youtube.dto.YouTubeChannelListResponseDto
import com.example.data.provider.youtube.dto.YouTubePlaylistItemListResponseDto
import com.example.data.provider.youtube.dto.YouTubePlaylistListResponseDto
import com.example.data.provider.youtube.dto.YouTubeSearchListResponseDto
import com.example.data.provider.youtube.dto.YouTubeVideoListResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit definition for YouTube Data API v3 endpoints.
 * Operates purely on provider DTOs, isolating network and serialization contracts
 * from the MUSE domain models.
 */
interface YouTubeApiService {

    @GET("search")
    suspend fun search(
        @Query("part") part: String = "snippet",
        @Query("q") query: String,
        @Query("type") type: String = "video",
        @Query("videoCategoryId") videoCategoryId: String? = "10", // Category 10 is Music in YouTube
        @Query("maxResults") maxResults: Int = 20,
        @Query("key") apiKey: String
    ): YouTubeSearchListResponseDto

    @GET("videos")
    suspend fun getVideosById(
        @Query("part") part: String = "snippet,contentDetails,topicDetails",
        @Query("id") ids: String,
        @Query("key") apiKey: String
    ): YouTubeVideoListResponseDto

    @GET("videos")
    suspend fun getPopularMusicVideos(
        @Query("part") part: String = "snippet,contentDetails,topicDetails",
        @Query("chart") chart: String = "mostPopular",
        @Query("videoCategoryId") videoCategoryId: String = "10",
        @Query("maxResults") maxResults: Int = 25,
        @Query("key") apiKey: String
    ): YouTubeVideoListResponseDto

    @GET("channels")
    suspend fun getChannelById(
        @Query("part") part: String = "snippet,statistics",
        @Query("id") id: String,
        @Query("key") apiKey: String
    ): YouTubeChannelListResponseDto

    @GET("playlists")
    suspend fun getPlaylists(
        @Query("part") part: String = "snippet,contentDetails",
        @Query("id") id: String? = null,
        @Query("channelId") channelId: String? = null,
        @Query("maxResults") maxResults: Int = 20,
        @Query("key") apiKey: String
    ): YouTubePlaylistListResponseDto

    @GET("playlistItems")
    suspend fun getPlaylistItems(
        @Query("part") part: String = "snippet",
        @Query("playlistId") playlistId: String,
        @Query("maxResults") maxResults: Int = 50,
        @Query("key") apiKey: String
    ): YouTubePlaylistItemListResponseDto
}
