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
