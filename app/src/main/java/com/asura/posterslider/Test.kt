package com.asura.posterslider

fun main() {
    val str1 = "https://www.youtube.com/watch?v=2Tt_InhLQ5Q"
    val str2 = "https://www.youtube.com"
    val result = str1.contains(str2)
    println("hasil stri 1 mengandung str dengan: $result")
    if (result) {
        println("ada")
    } else {
        println("tidak ada.")
    }
}