package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.MediaUpload

import ru.netology.nmedia.dto.User

interface AuthRepository {

    suspend fun authenticateUser(login: String, passwd: String) : User

    suspend fun registerUser(login: String, passwd: String, name: String) : User

    suspend fun registerWithPhoto(login: String, passwd: String, name: String, upload: MediaUpload): User


}