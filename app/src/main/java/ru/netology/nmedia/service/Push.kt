package ru.netology.nmedia.service


data class Push (
    val content: String,
    val recipientId: Long?
        )