package com.citra.reverie.service

import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.citra.reverie.player.MusicPlayerManager

class MusicService : MediaSessionService() {

    override fun onCreate() {
        super.onCreate()
        val session = MusicPlayerManager.getInstance(applicationContext).getMediaSession()
        if (session != null) {
            addSession(session)
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return MusicPlayerManager.getInstance(applicationContext).getMediaSession()
    }

    @OptIn(UnstableApi::class)
    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = MusicPlayerManager.getInstance(applicationContext).getExoPlayer()
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        val session = MusicPlayerManager.getInstance(applicationContext).getMediaSession()
        if (session != null) {
            removeSession(session)
        }
        super.onDestroy()
    }
}
