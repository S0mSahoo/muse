package com.example.data.provider.youtube.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class YouTubeSearchListResponseDto(
    @field:Json(name = "kind") val kind: String? = null,
    @field:Json(name = "etag") val etag: String? = null,
    @field:Json(name = "nextPageToken") val nextPageToken: String? = null,
    @field:Json(name = "prevPageToken") val prevPageToken: String? = null,
    @field:Json(name = "items") val items: List<YouTubeSearchResultDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class YouTubeSearchResultDto(
    @field:Json(name = "id") val id: YouTubeIdDto? = null,
    @field:Json(name = "snippet") val snippet: YouTubeSnippetDto? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeIdDto(
    @field:Json(name = "kind") val kind: String? = null,
    @field:Json(name = "videoId") val videoId: String? = null,
    @field:Json(name = "channelId") val channelId: String? = null,
    @field:Json(name = "playlistId") val playlistId: String? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeSnippetDto(
    @field:Json(name = "publishedAt") val publishedAt: String? = null,
    @field:Json(name = "channelId") val channelId: String? = null,
    @field:Json(name = "title") val title: String? = null,
    @field:Json(name = "description") val description: String? = null,
    @field:Json(name = "thumbnails") val thumbnails: YouTubeThumbnailsDto? = null,
    @field:Json(name = "channelTitle") val channelTitle: String? = null,
    @field:Json(name = "tags") val tags: List<String>? = null,
    @field:Json(name = "categoryId") val categoryId: String? = null,
    @field:Json(name = "resourceId") val resourceId: YouTubeIdDto? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeThumbnailsDto(
    @field:Json(name = "default") val default: YouTubeThumbnailDto? = null,
    @field:Json(name = "medium") val medium: YouTubeThumbnailDto? = null,
    @field:Json(name = "high") val high: YouTubeThumbnailDto? = null,
    @field:Json(name = "standard") val standard: YouTubeThumbnailDto? = null,
    @field:Json(name = "maxres") val maxres: YouTubeThumbnailDto? = null
) {
    fun getBestArtworkUrl(): String {
        return maxres?.url
            ?: standard?.url
            ?: high?.url
            ?: medium?.url
            ?: default?.url
            ?: ""
    }
}

@JsonClass(generateAdapter = true)
data class YouTubeThumbnailDto(
    @field:Json(name = "url") val url: String? = null,
    @field:Json(name = "width") val width: Int? = null,
    @field:Json(name = "height") val height: Int? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeVideoListResponseDto(
    @field:Json(name = "kind") val kind: String? = null,
    @field:Json(name = "etag") val etag: String? = null,
    @field:Json(name = "items") val items: List<YouTubeVideoDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class YouTubeVideoDto(
    @field:Json(name = "id") val id: String? = null,
    @field:Json(name = "snippet") val snippet: YouTubeSnippetDto? = null,
    @field:Json(name = "contentDetails") val contentDetails: YouTubeContentDetailsDto? = null,
    @field:Json(name = "topicDetails") val topicDetails: YouTubeTopicDetailsDto? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeContentDetailsDto(
    @field:Json(name = "duration") val duration: String? = null,
    @field:Json(name = "dimension") val dimension: String? = null,
    @field:Json(name = "definition") val definition: String? = null,
    @field:Json(name = "caption") val caption: String? = null,
    @field:Json(name = "licensedContent") val licensedContent: Boolean? = null,
    @field:Json(name = "regionRestriction") val regionRestriction: YouTubeRegionRestrictionDto? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeRegionRestrictionDto(
    @field:Json(name = "allowed") val allowed: List<String>? = null,
    @field:Json(name = "blocked") val blocked: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeTopicDetailsDto(
    @field:Json(name = "topicCategories") val topicCategories: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeChannelListResponseDto(
    @field:Json(name = "kind") val kind: String? = null,
    @field:Json(name = "items") val items: List<YouTubeChannelDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class YouTubeChannelDto(
    @field:Json(name = "id") val id: String? = null,
    @field:Json(name = "snippet") val snippet: YouTubeSnippetDto? = null,
    @field:Json(name = "statistics") val statistics: YouTubeChannelStatisticsDto? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeChannelStatisticsDto(
    @field:Json(name = "subscriberCount") val subscriberCount: String? = null,
    @field:Json(name = "videoCount") val videoCount: String? = null
)

@JsonClass(generateAdapter = true)
data class YouTubePlaylistListResponseDto(
    @field:Json(name = "kind") val kind: String? = null,
    @field:Json(name = "items") val items: List<YouTubePlaylistDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class YouTubePlaylistDto(
    @field:Json(name = "id") val id: String? = null,
    @field:Json(name = "snippet") val snippet: YouTubeSnippetDto? = null,
    @field:Json(name = "contentDetails") val contentDetails: YouTubePlaylistContentDetailsDto? = null
)

@JsonClass(generateAdapter = true)
data class YouTubePlaylistContentDetailsDto(
    @field:Json(name = "itemCount") val itemCount: Int? = null
)

@JsonClass(generateAdapter = true)
data class YouTubePlaylistItemListResponseDto(
    @field:Json(name = "kind") val kind: String? = null,
    @field:Json(name = "nextPageToken") val nextPageToken: String? = null,
    @field:Json(name = "items") val items: List<YouTubePlaylistItemDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class YouTubePlaylistItemDto(
    @field:Json(name = "id") val id: String? = null,
    @field:Json(name = "snippet") val snippet: YouTubeSnippetDto? = null
)
