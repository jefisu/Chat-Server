package com.jefisu.data.util

import io.ktor.http.content.*
import java.io.File

fun PartData.FileItem.save(path: String, fileName: String): File {
    val folder = File(path)
    folder.mkdirs()
    val data = streamProvider().readBytes()
    return File("$path$fileName").apply {
        writeBytes(data)
    }
}