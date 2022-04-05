package com.riyazuddin.zingplayer.data

data class SongTackNotification(
    val mediaItemIndex: Int,
    val title: String,
    var showPlay: Boolean,
    val showPrevious: Boolean,
    val showNext: Boolean
)