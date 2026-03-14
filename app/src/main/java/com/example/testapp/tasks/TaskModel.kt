

package com.example.testapp.tasks

data class TaskModel(
    val title: String,
    val rewardMinutes: Int,
    var completed: Boolean = false
)