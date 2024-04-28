package io.github.OreshkinIV.utils

import java.util.Locale

object OsUtils {

    fun getOs(): OS? {
        val os = System.getProperty("os.name").lowercase(Locale.ROOT)

        return when {
            os.contains("win") -> {
                OS.WINDOWS
            }

            os.contains("mac") -> {
                OS.MAC
            }

            (os.contains("nix") || os.contains("nux") || os.contains("aix")) -> {
                OS.LINUX
            }

            else -> null
        }
    }

    enum class OS {
        WINDOWS,
        LINUX,
        MAC,
    }
}
