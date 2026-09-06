package com.example.data.provider.online.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ITunesSearchResponseDto(
    @field:Json(name = "resultCount") val resultCount: Int = 0,
    @field:Json(name = "results") val results: List<ITunesTrackDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ITunesTrackDto(
    @field:Json(name = "wrapperType") val wrapperType: String? = null,
    @field:Json(name = "kind") val kind: String? = null,
    @field:Json(name = "artistId") val artistId: Long? = null,
    @field:Json(name = "collectionId") val collectionId: Long? = null,
    @field:Json(name = "trackId") val trackId: Long? = null,
    @field:Json(name = "artistName") val artistName: String? = null,
    @field:Json(name = "collectionName") val collectionName: String? = null,
    @field:Json(name = "trackName") val trackName: String? = null,
    @field:Json(name = "collectionCensoredName") val collectionCensoredName: String? = null,
    @field:Json(name = "trackCensoredName") val trackCensoredName: String? = null,
    @field:Json(name = "artistViewUrl") val artistViewUrl: String? = null,
    @field:Json(name = "collectionViewUrl") val collectionViewUrl: String? = null,
    @field:Json(name = "trackViewUrl") val trackViewUrl: String? = null,
    @field:Json(name = "previewUrl") val previewUrl: String? = null,
    @field:Json(name = "artworkUrl30") val artworkUrl30: String? = null,
    @field:Json(name = "artworkUrl60") val artworkUrl60: String? = null,
    @field:Json(name = "artworkUrl100") val artworkUrl100: String? = null,
    @field:Json(name = "releaseDate") val releaseDate: String? = null,
    @field:Json(name = "collectionExplicitness") val collectionExplicitness: String? = null,
    @field:Json(name = "trackExplicitness") val trackExplicitness: String? = null,
    @field:Json(name = "trackTimeMillis") val trackTimeMillis: Long? = null,
    @field:Json(name = "country") val country: String? = null,
    @field:Json(name = "currency") val currency: String? = null,
    @field:Json(name = "primaryGenreName") val primaryGenreName: String? = null,
    @field:Json(name = "isStreamable") val isStreamable: Boolean? = null
)
