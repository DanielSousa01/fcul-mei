import java.util.concurrent.BlockingQueue

class Worker(val taskQueue: BlockingQueue<Task>): Runnable {

    override fun run() {
        while (true) {
            val task = taskQueue.take()
            if (task.taskType == TaskType.RUNNABLE) {
                require(task.runnable != null) { "Runnable task cannot be null" }
                task.runnable.run()
            } else {
                break
            }
        }
    }

}