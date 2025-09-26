# Parallel Programming

The main goal of parallel programming is to improve the performance of applications by dividing tasks into smaller sub-tasks that can be executed simultaneously across multiple threads. This approach means that this sub-tasks have little (or no) comunication between them or dependencies, so they can be executed in parallel without waiting for each other to complete. The only comunication that can happen is at the start to assign work to each thread, and at the end to combine the results.

## Context Switching

Context switching is the process of storing the state of a thread or process so that it can be resumed later. This allows multiple threads or processes to share a single CPU, giving the illusion of parallelism. However, context switching comes with overhead, as the CPU must save and load the state of each thread or process, which can lead to performance degradation if done excessively.

## Task Granularity

Task granularity refers to the size of the tasks that are being executed in parallel. Fine-grained tasks are small and numerous, while coarse-grained tasks are larger and fewer in number. The choice of task granularity can impact performance, as fine-grained tasks may lead to increased overhead due to context switching, while coarse-grained tasks may not fully utilize the available parallelism.

## Data Parallelism vs Task Parallelism

**Data parallelism** involves distributing data across multiple threads or processes, with each thread performing the same operation on its assigned data. This approach is often used in scenarios where the same computation needs to be applied to a large dataset, such as in matrix operations or image processing.

**Task parallelism**, on the other hand, involves dividing a program into distinct tasks that can be executed concurrently. Each task may perform different operations and may not necessarily operate on the same data. This approach is often used in scenarios where different parts of a program can be executed independently, such as in web servers handling multiple requests simultaneously.

**SIMD (Single Instruction, Multiple Data)** is a type of data parallelism where a single instruction is applied to multiple data points simultaneously. This is often implemented using vectorized instructions in modern CPUs, allowing for efficient processing of large datasets.

**MIMD (Multiple Instruction, Multiple Data)** is a type of parallelism where multiple processors execute different instructions on different data points simultaneously. This approach is often used in scenarios where tasks are independent and can be executed in parallel, such as in distributed computing environments.

### Parallel Programming Design Patterns

#### Loop level Parallelism

Loop-level parallelism involves dividing the iterations of a loop across multiple threads or processes. This approach is often used in scenarios where each iteration of the loop is independent and can be executed concurrently.

#### Master-Worker Pattern

The master-worker pattern is a parallel programming model where a master process distributes tasks to multiple worker processes. The master is responsible for dividing the workload and assigning tasks to workers, while the workers execute the tasks and return the results to the master. This pattern is often used in scenarios where tasks can be processed independently and in parallel, such as in batch processing or data analysis workloads.

#### Poison Pill Pattern

The poison pill pattern is a technique used in concurrent programming to gracefully shut down worker threads. A "poison pill" is a special message or signal that is sent to a worker thread to indicate that it should stop processing and terminate. When a worker thread receives the poison pill, it completes any ongoing work and then exits. This pattern is often used in scenarios where worker threads are processing tasks from a queue, and the poison pill is added to the queue to signal the workers to stop.

The workers will run on a while(true), sleeping, waiting for work to do. When they receive the poison pill, they will break the loop and exit.

#### Fork-Join Pattern

The fork-join pattern is a parallel programming model where a task is recursively divided into smaller sub-tasks (forked) that can be executed in parallel. Once the sub-tasks are completed, their results are combined (joined) to produce the final result. This pattern is often used in scenarios where tasks can be broken down into smaller, independent units of work, such as in divide-and-conquer algorithms or parallel sorting algorithms.

##### Fork-Join with Work Stealing

Work stealing is an optimization technique used in the fork-join pattern to improve load balancing among worker threads. In a work-stealing algorithm, each worker thread maintains its own deque (double-ended queue) of tasks to execute. When a worker thread completes its tasks and becomes idle, it can "steal" tasks from the deques of other worker threads that are still busy. This helps to ensure that all worker threads remain busy and that the workload is evenly distributed, leading to improved performance and reduced idle time.

#### Scatter-Gather Pattern

The scatter-gather pattern is a parallel programming model where a task is divided into smaller sub-tasks (scattered) that can be executed in parallel, and the results of these sub-tasks are then collected (gathered) to produce the final result. This pattern is often used in scenarios where a large dataset needs to be processed in parallel, such as in distributed computing or data processing applications.

##### Scatter-Gather vs Master-Worker

The scatter-gather pattern is similar to the master-worker pattern, but there are some key differences. In the master-worker pattern, the master process is responsible for dividing the workload and assigning tasks to workers, while in the scatter-gather pattern, the workload is divided into smaller sub-tasks that can be executed independently. Additionally, in the master-worker pattern, workers typically return results to the master process, while in the scatter-gather pattern, results are collected from all sub-tasks to produce the final result. 

The scatter-gather pattern is often used in scenarios where tasks can be processed independently and in parallel, while the master-worker pattern is often used in scenarios where tasks need to be coordinated and managed by a central process.

#### Map-Reduce Pattern

The map-reduce pattern is a powerful parallel programming model that was popularized by Google and is widely used in distributed computing frameworks. It provides a simple yet effective way to process large datasets across distributed clusters by breaking down complex computations into two fundamental operations: mapping and reducing.

The pattern follows a functional programming approach where:

- **Map**: Transforms input data into intermediate key-value pairs
- **Reduce**: Aggregates and combines intermediate results to produce final output

This approach ensures that the computation can be easily parallelized and distributed across multiple machines while maintaining fault tolerance and scalability.

The typical workflow of the map-reduce pattern involves the following steps:

1. **Input Splitting Phase**:
   - The large input dataset is automatically divided into smaller, manageable chunks
   - Each chunk is typically sized to fit within a single processing node's memory
   - Chunks are distributed across available worker nodes

2. **Map Phase**:
   - Each mapper processes one chunk of input data independently
   - Mappers apply a user-defined function to transform input records into intermediate key-value pairs
   - Multiple mappers run in parallel across different nodes
   - Output: Set of intermediate (key, value) pairs

3. **Shuffle and Sort Phase** (often called Shuffle Phase):
   - Intermediate key-value pairs are redistributed across the network
   - All values with the same key are grouped together and sent to the same reducer
   - Data is sorted by key to optimize reducer processing
   - This phase involves significant network I/O and is often the bottleneck

4. **Reduce Phase**:
   - Each reducer processes all values associated with specific keys
   - Reducers apply a user-defined function to combine/aggregate values for each key
   - Multiple reducers can run in parallel, each handling different key ranges
   - Output: Final results written to distributed storage
