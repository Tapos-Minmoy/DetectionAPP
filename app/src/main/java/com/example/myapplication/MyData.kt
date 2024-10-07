package com.example.myapplication;

data class MyData(
        val limit: Int,
        val quotes: List<Quote>,
        val skip: Int,
        val total: Int
)