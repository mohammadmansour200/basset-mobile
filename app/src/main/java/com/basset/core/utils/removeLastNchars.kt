package com.basset.core.utils

fun String.removeLastNchars(n: Int): String {
    return substring(0, this.length - n)
}