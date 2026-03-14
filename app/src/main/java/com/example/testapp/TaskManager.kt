package com.example.testapp

import java.util.UUID

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val reward: Int
)

object TaskManager {

    val tasks = mutableListOf<Task>()

    fun addTask(name: String, reward: Int) {
        tasks.add(Task(name = name, reward = reward))
    }

    fun removeTask(id: String) {
        tasks.removeAll { it.id == id }
    }
}