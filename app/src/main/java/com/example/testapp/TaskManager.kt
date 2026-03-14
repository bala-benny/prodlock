package com.example.testapp

data class Task(
    val name: String,
    val reward: Int
)

object TaskManager {

    val tasks = listOf(
        Task("Read 10 pages", 10),
        Task("Exercise for 10 minutes", 15),
        Task("Clean your desk", 5),
        Task("Solve 5 math problems", 20)
    )

}