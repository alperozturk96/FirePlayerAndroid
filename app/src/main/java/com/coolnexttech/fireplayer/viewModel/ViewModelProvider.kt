package com.coolnexttech.fireplayer.viewModel

object ViewModelProvider {
    private var homeViewModel: HomeViewModel? = null
    private var audioPlayerViewModel: AudioPlayerViewModel? = null

    fun getHomeViewModel(): HomeViewModel {
        if (homeViewModel == null) {
            homeViewModel = HomeViewModel()
        }

        return homeViewModel!!
    }

    fun getAudioPlayerViewModel(): AudioPlayerViewModel {
        if (audioPlayerViewModel == null) {
            audioPlayerViewModel = AudioPlayerViewModel()
        }

        return audioPlayerViewModel!!
    }


}