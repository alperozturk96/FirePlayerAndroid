package com.coolnexttech.fireplayer.utils

import com.coolnexttech.fireplayer.appContext
import com.coolnexttech.fireplayer.player.AudioPlayer
import com.coolnexttech.fireplayer.ui.home.HomeViewModel
import com.coolnexttech.fireplayer.ui.playlists.PlaylistsViewModel

object VMProvider {
    var homeViewModel = HomeViewModel()
    val playlistViewModel = PlaylistsViewModel()
    val audioPlayer = AudioPlayer(appContext.get(), homeViewModel)
}
