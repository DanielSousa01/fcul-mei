package worker

data class Task(val taskType: TaskType = TaskType.RUNNABLE, val runnable: Runnable? = null)

