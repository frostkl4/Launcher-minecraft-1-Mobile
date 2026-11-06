/*
 * Noctis Launcher — unofficial modified version of Zalith Launcher 2
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

package com.movtery.zalithlauncher.noctis

/**
 * Facts about this fork that the rest of the code can check.
 */
object NoctisInfo {
    /** Upstream project this build is based on (GPL-3.0). */
    const val UPSTREAM_NAME = "Zalith Launcher 2"
    const val UPSTREAM_URL = "https://github.com/ZalithLauncher/ZalithLauncher2"
    const val UPSTREAM_COMMIT = "3ad5a78"

    /**
     * The built-in update checker reads the official Zalith release feed.
     * Offering that as an update inside this fork would replace it with a
     * different app, so the automatic check on start stays off.
     */
    const val CHECK_UPSTREAM_UPDATES_ON_START = false

    /**
     * Debug and trace log lines of the launcher and the file manager.
     * Off for release: only info, warnings and errors are written to the log files,
     * and only warnings and errors go to logcat. Turn on only to investigate a bug.
     */
    const val VERBOSE_LOGS = false
}
