package com.example.testapp

import android.content.Context
import java.util.UUID

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val reward: Int
)

object TaskManager {
    val tasks = mutableListOf<Task>()
    private const val PREFS_NAME = "prodlock_tasks_prefs"
    private const val KEY_TASKS = "tasks_list"

    fun load(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedTasks = prefs.getStringSet(KEY_TASKS, null)
        if (savedTasks != null) {
            tasks.clear()
            savedTasks.forEach { taskData ->
                val parts = taskData.split("|")
                if (parts.size == 3) {
                    tasks.add(Task(id = parts[0], name = parts[1], reward = parts[2].toInt()))
                }
            }
        }
    }

    private fun save(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val taskSet = tasks.map { "${it.id}|${it.name}|${it.reward}" }.toSet()
        prefs.edit().putStringSet(KEY_TASKS, taskSet).apply()
    }

    fun addTask(context: Context, name: String, reward: Int) {
        tasks.add(Task(name = name, reward = reward))
        save(context)
    }

    fun removeTask(context: Context, id: String) {
        tasks.removeAll { it.id == id }
        save(context)
    }
}