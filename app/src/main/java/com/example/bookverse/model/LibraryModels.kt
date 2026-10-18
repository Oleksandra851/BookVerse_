package com.example.bookverse.model

enum class BookStatus {
    READING,
    COMPLETED,
    PLANNED
}

data class Book(
    val id: String,
    val title: String,
    val author: String,
    val currentPages: Int,
    val totalPages: Int,
    val status: BookStatus,
    val rating: Int,
    val note: String = ""
)

data class Quote(
    val id: String,
    val text: String,
    val bookTitle: String,
    val author: String
)

data class QuizQuestion(
    val quote: String,
    val correctAuthor: String,
    val options: MutableList<String>
)