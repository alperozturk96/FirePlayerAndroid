package com.coolnexttech.fireplayer.db

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class PlaylistEntity(
    @Id var id: Long = 0,
    var title: String = "",
    var tracks: MutableList<String> = arrayListOf()
)