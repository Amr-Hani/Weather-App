package com.example.witherapp

sealed class ApiState<T> {
    data class Success<T>(val data: T) : ApiState<T>()
    data class Failure<T>(val message: String) : ApiState<T>()
    class Loading<T> : ApiState<T>()
}