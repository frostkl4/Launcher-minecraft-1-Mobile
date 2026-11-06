/*
 * Moonlight Launcher — unofficial modified version of Zalith Launcher 2
 * Copyright (C) 2025-2026 MovTery <movtery228@qq.com> and contributors
 * Modifications Copyright (C) 2026 frostkl4
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.moonlight

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Process
import coil3.SingletonImageLoader
import com.movtery.zalithlauncher.coroutine.TaskSystem
import com.movtery.zalithlauncher.utils.logging.Logger

private const val TAG = "MoonlightRelease"

/** Time for Android to deliver the "start the game" requests before this process goes away. */
private const val RELEASE_DELAY_MS = 3_000L

/** How many times to wait for the launch task to finish before giving up (keeps the process). */
private const val MAX_ATTEMPTS = 4

/**
 * Frees the launcher's memory while Minecraft is running.
 *
 * The game runs in its own process (":game"); the launcher screens live in the main process.
 * After the game starts, the launcher activity is already closed, but the main process would
 * stay in memory (accounts, settings, image cache, Compose runtime...). On phones with little
 * RAM that memory competes with the game, so once the game is up:
 *  1. decoded images are dropped from the image cache right away;
 *  2. if no download/installation is running and no launcher screen was reopened,
 *     the main process ends itself. Returning from the game simply starts the launcher again.
 */
object MoonlightLauncherRelease {
    private val handler = Handler(Looper.getMainLooper())

    @Volatile
    private var pending = false

    /** Call from the main process right after the game was started. */
    fun releaseForGame(app: Application) {
        runCatching { SingletonImageLoader.get(app).memoryCache?.clear() }
            .onFailure { Logger.warning(TAG, "Could not clear image cache", it) }

        if (pending) return
        pending = true

        // If any launcher screen opens during the wait (e.g. an error screen), keep the process
        var activityStarted = false
        val watcher = object : Application.ActivityLifecycleCallbacks {
            override fun onActivityStarted(activity: Activity) { activityStarted = true }
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) { activityStarted = true }
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        }
        app.registerActivityLifecycleCallbacks(watcher)

        fun finishWatching() {
            app.unregisterActivityLifecycleCallbacks(watcher)
            pending = false
        }

        // The launch itself runs as a task, so wait for it to finish; a real download keeps the process
        var attempt = 0
        val check = object : Runnable {
            override fun run() {
                attempt++
                when {
                    activityStarted -> {
                        finishWatching()
                        Logger.info(TAG, "A launcher screen was opened, keeping the process")
                    }
                    TaskSystem.tasksFlow.value.isNotEmpty() -> {
                        if (attempt < MAX_ATTEMPTS) {
                            handler.postDelayed(this, RELEASE_DELAY_MS)
                        } else {
                            finishWatching()
                            Logger.info(TAG, "Tasks are still running, keeping the launcher process")
                        }
                    }
                    else -> {
                        finishWatching()
                        Logger.info(TAG, "Game started: ending the launcher process to give its memory to the game")
                        Process.killProcess(Process.myPid())
                    }
                }
            }
        }
        handler.postDelayed(check, RELEASE_DELAY_MS)
    }
}
