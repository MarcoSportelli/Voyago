package com.example.voyago.utils

// Estensione per formattazione nome campo
fun String.splitCamelCase() =
    replace(Regex("(?<=[a-z])(?=[A-Z])"), " ")
        .replaceFirstChar { it.uppercase() }