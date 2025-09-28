# Synchronization

The synchronization of concurrent processes or threads is crucial to ensure data consistency and to prevent race conditions. Race conditions occur when multiple threads access shared data simultaneously, leading to unpredictable results. To avoid these issues, synchronization mechanisms are employed to control the access of multiple threads to shared resources. In addition, synchronization is also used to prevent deadlocks, which occur when two or more threads are waiting indefinitely for each other to release resources, livelocks, where threads continuously change their state in response to each other without making progress, and resource starvation, where a thread is perpetually denied access to resources it needs to proceed.

## Critical Section

A critical section is a part of the code that accesses shared resources and must not be executed by more than one thread at a time. To protect critical sections, synchronization mechanisms such as mutexes (mutual exclusion locks) are used. A mutex allows only one thread to enter the critical section at a time, ensuring that shared data is accessed in a controlled manner.

### Properties of Critical Section Solutions

For any critical section synchronization mechanism to be correct and efficient, it must satisfy three fundamental properties:

1. Mutual Exclusion (Safety Property): **Only one thread per critical section** - At most one thread can be executing in the critical section at any given time. 
2. Progress (Liveness Property): **If the critical section is free with threads waiting, a thread should enter at some point** - The system should not get stuck in a state where the critical section is available but no waiting thread is allowed to enter.
3. Bounded Waiting (Fairness Property): **Every thread that wants to go in should be able to at some point** - There should be a bound on the number of times other threads can enter the critical section after a thread has made a request and before that thread is granted access.

## Low-Level Synchronization Primitives

### Atomicity

Atomicity refers to operations that are completed as a single, indivisible step. In the context of synchronization, atomic operations ensure that a sequence of operations on shared data is executed without interruption, preventing other threads from interfering during the operation. Examples of atomic operations include reading or writing a single variable, incrementing a counter, or performing compare-and-swap (CAS) operations.

### Spinlocks

A spinlock is a simple synchronization mechanism that uses busy-waiting to protect critical sections. When a thread attempts to acquire a spinlock, it continuously checks (or "spins") until the lock becomes available. Spinlocks are efficient for short critical sections where the wait time is expected to be minimal, as they avoid the overhead of context switching associated with traditional locks. However, they can lead to wasted CPU cycles if the wait time is long.

### Mutexes

A mutex (mutual exclusion) is a synchronization primitive that allows only one thread to access a critical section at a time. When a thread locks a mutex, other threads attempting to lock the same mutex will be blocked until the mutex is unlocked. Mutexes are suitable for protecting longer critical sections where the overhead of blocking and context switching is justified. This mechanism doesn’t waste CPU cycles like spinlocks do but is computationally more expensive because of the context switching involved.

### Futexes

A futex (fast userspace mutex) is a hybrid synchronization mechanism that combines the efficiency of spinlocks with the blocking capabilities of mutexes. Futexes allow threads to perform fast, non-competitive locking in user space without kernel intervention. When competition occurs, the futex falls back to a kernel-level wait, allowing the thread to sleep until the lock becomes available. This approach minimizes the overhead of context switching while still providing robust synchronization for critical sections.

## Higher-Level Synchronization Mechanisms

### Semaphores

A semaphore is a synchronization primitive that maintains a count to control access to shared resources. Similar to a lock but more general, semaphores use a counter to control access to up to N concurrent accesses. It can be used to manage multiple instances of a resource, allowing a specified number of threads to access the resource concurrently.

Semaphores support two primary operations:

- **wait() or acquire()**: Decrements the counter (counter--). If the counter reaches zero, the thread blocks until a resource becomes available.
- **signal() or release()**: Increments the counter (counter++), potentially waking up a blocked thread waiting for access.

Semaphores can be binary (count of 1, equivalent to a mutex) or counting (count greater than 1, allowing multiple concurrent accesses).

### Barriers

A barrier is a synchronization primitive that coordinates a specific group of threads to meet at a common point using a multi-way synchronization mechanism. Unlike other synchronization primitives that typically involve one-to-one or one-to-many coordination, barriers enable many-to-many synchronization.

Every thread in the group will wait for the others to reach the barrier before any of them can proceed. Threads call `await()` as a message that a specific checkpoint was reached. An important characteristic of barriers is that there is no need for a central coordinating figure - the threads can coordinate and synchronize by themselves in a distributed manner.

This is particularly useful in parallel computing scenarios where threads need to synchronize at certain points in their execution, such as after completing a phase of computation before moving on to the next phase.

### Conditional Variables

The conditional variable is a synchronization primitive that enables threads to wait for certain conditions to be met before proceeding. It is typically used in conjunction with mutexes to allow threads to wait for specific states or events in a safe manner. A conditional variable provides two main operations:

- **wait()**: This operation releases the associated mutex and puts the thread to sleep until another thread signals the condition variable. When the thread wakes up, it re-acquires the mutex before returning from the wait.
- **notify() / notify_all()**: These operations wake up one or all threads that are waiting on the condition variable, respectively. The notifying thread must hold the associated mutex when calling these operations.

### Blocking Queues

A blocking queue is a thread-safe data structure that allows multiple threads to add and remove elements concurrently while managing synchronization internally. It is particularly useful in producer-consumer scenarios, where one or more threads (producers) generate data and place it into the queue, while other threads (consumers) retrieve and process the data.

### Reentrant Locks

A reentrant lock, also known as a recursive lock, is a synchronization primitive that allows the same thread to acquire the lock multiple times without causing a deadlock. This addresses a fundamental problem: **What if a thread holding a lock (or mutex) tries to get the same lock again?** With regular locks, this would result in a self-deadlock, where the thread blocks indefinitely waiting for itself to release the lock.

A reentrant lock allows a thread to get a lock it already owns. To accomplish this, the lock needs to maintain two key pieces of information:

- **The thread that holds it**: The lock must track which specific thread currently owns it
- **The hold count**: The number of times the owning thread has acquired the lock

This is particularly useful in scenarios where a thread may need to enter a critical section that it has already locked, such as when calling a method that also requires the same lock. The thread must release the lock the same number of times it acquired it before other threads can obtain the lock.

### Monitors

A monitor is a high-level synchronization construct that combines mutual exclusion and condition variables to manage access to shared resources. It encapsulates shared data and the procedures that operate on that data, ensuring that only one thread can execute any of the monitor's procedures at a time.

**Key characteristics of monitors:**

- **Automatic synchronization**: Monitors automatically handle the locking and unlocking of the associated mutex, simplifying the synchronization process for developers
- **Higher-level abstraction**: This mechanism is not a primitive but rather a higher-level abstraction built on top of lower-level synchronization primitives like mutexes and condition variables
- **Language support**: Many languages such as Java provide built-in support for monitors through synchronized methods or blocks
- **Reentrant by default**: Monitors are typically reentrant, meaning that a thread can acquire the same monitor multiple times without causing a deadlock

In Java, every object has an implicit monitor associated with it. When a thread enters a synchronized method or block, it acquires the object's monitor, and when it exits, it releases the monitor. This ensures that only one thread can execute the synchronized code at a time, providing mutual exclusion.

This means that every object in Java can be used as a monitor (effectively combining mutex + condition variable functionality), and the `synchronized` keyword is used to define critical sections that require exclusive access.

## Modern Synchronization Solutions

### Executors Service

The Executors framework in Java provides a high-level abstraction for managing threads and asynchronous tasks. It simplifies the process of creating and managing thread pools, allowing developers to focus on the business logic of their tasks rather than the complexities of underlying thread management.

**Key benefits of the Executors framework:**

- **Resource optimization**: Improves resource utilization and application performance by reusing threads for multiple tasks
- **Reduced overhead**: Minimizes the costly overhead associated with frequent thread creation and destruction
- **Lifecycle management**: Handles the complete lifecycle management of threads automatically
- **Task abstraction**: Separates task submission from task execution, providing flexibility in how tasks are processed
- **Built-in thread pools**: Offers various pre-configured thread pool implementations for different use cases

This approach significantly improves application scalability and reduces the complexity of concurrent programming by abstracting away low-level thread management details.

### Future

The `Future` interface in Java represents the result of an asynchronous computation. It provides methods to check if the computation is complete, to wait for its completion, and to retrieve the result of the computation once it is available. The `Future` interface is typically used in conjunction with the Executors framework to manage and retrieve results from tasks executed in separate threads.

The `Future` interface includes several key methods:

- **get()**: Waits if necessary for the computation to complete and then retrieves its result. This method can throw exceptions if the computation failed or was canceled.
- **isDone()**: Returns `true` if the computation is complete, whether it completed successfully, failed, or was canceled.
- **cancel()**: Attempts to cancel the computation. If the computation has not started, it will be canceled. If it is already running, it may or may not be canceled, depending on the implementation.
- **isCancelled()**: Returns `true` if the computation was canceled before it completed normally.

### CompletableFuture

The `CompletableFuture` class in Java is an extension of the `Future` interface that allows for more flexible and powerful asynchronous programming. It provides a way to write non-blocking code by using a callback-based approach, enabling developers to compose and chain asynchronous tasks easily.

**Core Purpose:**

- **Pipeline building**: Used to build pipelines of asynchronous operations, where each stage can transform or process data as it flows through the chain
- **Reactive programming**: Defines what should happen when data becomes available, rather than blocking and waiting for results
- **Composition support**: Enables easy composition and chaining of multiple asynchronous computations

The key methods of `CompletableFuture` include:

- **runAsync()**: Returns a Completable future with the task to be done by the Executor
- **thenApply()**: Adds a callback to be executed when the future completes successfully, transforming the result
- **thenAccept()**: Adds a callback to be executed when the future completes successfully, consuming the result without transforming it
- **thenCompose()**: Adds a callback that returns another CompletableFuture, allowing for chaining of asynchronous tasks
- **allOf()**: Combines multiple CompletableFutures into a single future that completes when all of them complete
- **anyOf()**: Combines multiple CompletableFutures into a single future that completes when any of them complete

This approach transforms traditional blocking, sequential code into efficient, non-blocking, reactive pipelines that can handle complex asynchronous workflows with ease.

### Java Streams API

The Java Streams API provides a functional-style API for processing sequences of elements (such as collections) built on top of the Fork/Join and Spliterator frameworks. It allows developers to perform complex data processing operations in a declarative manner, making code more readable and maintainable.

**Pipeline Operations:**

Streams operate through a pipeline of operations divided into two categories:

- **Intermediate operations**: `filter()`, `map()`, `sorted()`, `distinct()`, `limit()` - these are lazy and return a new stream
- **Terminal operations**: `collect()`, `forEach()`, `reduce()`, `count()`, `findFirst()` - these trigger the pipeline execution and produce a result

**Key Characteristics:**

- **Lazy evaluation**: Streams are lazy and only compute when a terminal operation is invoked, allowing for optimization and efficient processing
- **Easy parallelism**: Simple access to parallel processing with `.parallel()` or `.parallelStream()` methods
- **Automatic handling**: Java automatically manages data splitting, chunk processing, and result combining in parallel streams
- **Immutable**: Stream operations don't modify the original data source but create new streams with transformations

Because most of the work is done automatically, context is essential for effective parallel stream usage:

- **CPU-bound tasks**: Operations that require significant computational work benefit most from parallelization
- **Large enough data**: Dataset must be sufficiently large to overcome the overhead of parallel processing
- **Non-trivial work per element**: Each element should require substantial processing to justify thread coordination costs
- **Embarrassingly parallel**: Tasks that can be easily divided with minimal interdependence work best
- **Stateless lambdas**: Operations should be stateless and free of side effects to avoid race conditions

**Potential Pitfalls:**

- **Blocking operations**: Will block a worker thread and can lead to performance degradation
- **Starvation and deadlock**: Can occur when threads compete for limited resources or wait on each other
- **Overhead costs**: Small datasets or simple operations may perform worse in parallel due to coordination overhead

The best way to know if streams are a good solution is through **comparison + metrics** - always benchmark parallel vs sequential performance for your specific use case and data size.
