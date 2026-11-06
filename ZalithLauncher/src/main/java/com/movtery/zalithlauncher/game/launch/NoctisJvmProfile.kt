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

package com.movtery.zalithlauncher.game.launch

import android.app.ActivityManager
import android.content.Context
import com.movtery.zalithlauncher.game.multirt.Runtime
import com.movtery.zalithlauncher.utils.device.Architecture
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Noctis automatic JVM profile for the game.
 *
 * Decides, from the phone's hardware, three things that are added to the game's JVM arguments:
 *  1. a safe ceiling for the Java heap (-Xmx), so Android's low-memory killer does not close the game;
 *  2. the initial heap (-Xms): on weak phones the heap starts at half and grows on demand;
 *  3. garbage-collector flags tuned for Minecraft (G1 with a 50 ms pause target).
 *
 * Nothing here touches the launcher UI. Everything chosen is written to the game log.
 */
object NoctisJvmProfile {

    /** What the profile decided for one launch. */
    class Result(
        val heapMb: Int,
        val initialHeapMb: Int,
        val extraArgs: List<String>,
        val logLines: List<String>
    )

    private class Device(
        val totalMb: Int,
        val availableMb: Int,
        val lowRamFlag: Boolean,
        val cores: Int
    ) {
        /** "Weak phone" = 4 GB or less (phones report a bit less than the box says) or flagged low-RAM by Android. */
        val isWeak: Boolean get() = lowRamFlag || totalMb < 4096
    }

    /**
     * Memory the game needs OUTSIDE the Java heap: JVM metaspace and JIT code, LWJGL/OpenGL driver
     * buffers, native libraries and thread stacks. Rough value for a modded client on Android.
     */
    private const val NATIVE_OVERHEAD_MB = 600

    private fun readDevice(context: Context): Device {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val info = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }
        return Device(
            totalMb = (info.totalMem / (1024 * 1024)).toInt(),
            availableMb = (info.availMem / (1024 * 1024)).toInt(),
            lowRamFlag = am.isLowRamDevice,
            cores = java.lang.Runtime.getRuntime().availableProcessors()
        )
    }

    /**
     * Largest heap that still leaves room for Android itself, by the RAM the phone reports
     * (a "4 GB" phone reports about 3.6–3.8 GB, a "6 GB" one about 5.5 GB, and so on).
     */
    private fun safeHeapCapMb(device: Device, is64Bit: Boolean): Int {
        if (!is64Bit) return 1024 // 32-bit processes cannot address much more
        val t = device.totalMb
        return when {
            t < 2048 -> 1024
            t < 3072 -> 1280
            t < 4096 -> 1792
            t < 6144 -> 3072
            t < 8192 -> 4096
            else -> (t * 0.55f).roundToInt()
        }
    }

    /** G1 region size scaled to the heap, so small heaps still get enough regions to work with. */
    private fun regionSizeMb(heapMb: Int): Int = when {
        heapMb < 2048 -> 4
        heapMb < 4096 -> 8
        else -> 16
    }

    private val customGcPrefixes = listOf(
        "-XX:+UseG1GC", "-XX:+UseZGC", "-XX:+UseShenandoahGC", "-XX:+UseParallelGC",
        "-XX:+UseSerialGC", "-XX:+UseConcMarkSweepGC", "-XX:MaxGCPauseMillis", "-XX:G1"
    )

    /**
     * @param requestedMb memory chosen in the launcher (global or per-instance setting)
     * @param runtime Java runtime used for this launch
     * @param currentArgs arguments already collected (the user's own JVM arguments are in here)
     */
    fun build(
        context: Context,
        requestedMb: Int,
        runtime: Runtime,
        currentArgs: List<String>
    ): Result {
        val device = readDevice(context)
        val log = mutableListOf<String>()
        val is64Bit = runtime.arch?.contains("64") ?: Architecture.is64BitsDevice

        log += "Noctis JVM profile: device RAM ${device.totalMb} MB, available now ${device.availableMb} MB, " +
                "${device.cores} cores, ${if (device.isWeak) "weak-phone mode" else "normal mode"}, " +
                "Java ${runtime.javaVersion}, ${if (is64Bit) "64" else "32"}-bit"

        // 1) Heap ceiling
        val cap = safeHeapCapMb(device, is64Bit)
        val heapMb = if (requestedMb > cap) {
            log += "Noctis: memory lowered from $requestedMb MB to $cap MB (safe limit for this phone, " +
                    "so Android does not close the game)"
            cap
        } else requestedMb

        // Only a warning: "available" undercounts memory Android can still reclaim from background apps
        val neededNow = heapMb + NATIVE_OVERHEAD_MB
        if (neededNow > device.availableMb) {
            log += "Noctis: warning, the game may need about $neededNow MB but only ${device.availableMb} MB " +
                    "is free right now. Closing other apps helps."
        }

        // 2) Initial heap: weak phones start small and grow on demand (less RAM taken at start)
        val initialHeapMb = if (device.isWeak) max(256, heapMb / 2) else heapMb

        // 3) Garbage collector
        val extra = mutableListOf<String>()
        val userHasGcArgs = currentArgs.any { arg -> customGcPrefixes.any { arg.startsWith(it) } }
        when {
            userHasGcArgs -> log += "Noctis: custom garbage-collector arguments found, keeping yours"
            !is64Bit -> log += "Noctis: 32-bit Java, keeping the default garbage collector"
            else -> {
                extra += listOf(
                    // An unknown flag must never stop the game from starting
                    "-XX:+IgnoreUnrecognizedVMOptions",
                    "-XX:+UseG1GC",
                    "-XX:MaxGCPauseMillis=50",
                    "-XX:+UnlockExperimentalVMOptions",
                    "-XX:G1NewSizePercent=20",
                    "-XX:G1ReservePercent=20",
                    "-XX:G1HeapRegionSize=${regionSizeMb(heapMb)}M",
                    "-XX:+ParallelRefProcEnabled",
                    // Mods that call System.gc() get a background cycle instead of a full freeze
                    "-XX:+ExplicitGCInvokesConcurrent",
                    // Fewer duplicate strings in memory (helps big modpacks)
                    "-XX:+UseStringDeduplication"
                )
                if (device.isWeak && runtime.javaVersion >= 17) {
                    // When the game is idle (menus, paused) give unused heap back to Android
                    extra += "-XX:G1PeriodicGCInterval=30000"
                }
                log += "Noctis: G1 garbage collector, 50 ms pause target, ${regionSizeMb(heapMb)} MB regions"
            }
        }

        log += "Noctis: heap -Xms${initialHeapMb}M -Xmx${heapMb}M"
        return Result(
            heapMb = heapMb,
            initialHeapMb = min(initialHeapMb, heapMb),
            extraArgs = extra,
            logLines = log
        )
    }
}
