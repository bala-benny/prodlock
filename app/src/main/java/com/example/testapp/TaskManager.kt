package com.example.testapp

import com.example.testapp.tasks.TaskModel

object TaskManager {

    val tasks = mutableListOf<TaskModel>()

    fun addTask(title: String, reward: Int) {
        tasks.add(TaskModel(title, reward))
    }

    fun completeTask(index: Int): Int {
        val task = tasks[index]
        task.completed = true
        return task.rewardMinutes
    }
}