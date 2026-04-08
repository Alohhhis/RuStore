package com.alyona.rustore.ui.theme

data class Category(
    val name: String,
    val iconRes: Int,  // Иконка категории
    var count: Int = 0 // Динамическое количество приложений
)