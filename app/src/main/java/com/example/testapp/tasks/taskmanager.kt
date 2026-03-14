package com.example.testapp.tasks

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