package com.tools.rftool.util.graphics

object ColorMaps {
    private val maps = mapOf<Int, String>(
        0 to  "grayscale",
        1 to "heat",
        2 to "rainbow"
    )

    fun getColorMap(index: Int): String {
        return if(index in maps.entries.indices) {
            maps[index]!!
        } else {
            maps[0]!!
        }
    }
}