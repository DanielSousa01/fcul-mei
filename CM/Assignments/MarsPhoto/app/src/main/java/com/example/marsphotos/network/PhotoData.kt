package com.example.marsphotos.network

sealed interface PhotoData {
    val id: String
    val imgSrc: String
}