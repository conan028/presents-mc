package com.conan.mods.presents.fabric.util

import com.google.gson.GsonBuilder
import java.io.File
import java.lang.reflect.Type
object ConfigFileHandler {

    private val gson = GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create()

    fun <T> loadOrInitializeFile(file: File, typeToken: Type, data: T) : T {
        return if (file.exists()) {
            gson.fromJson(file.reader(), typeToken)
        } else {
            file.parentFile.mkdirs()
            file.createNewFile()
            file.writeText(gson.toJson(data))
            data
        }
    }

}