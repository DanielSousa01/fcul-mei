# Synchronization

The synchronization of concurrent processes or threads is crucial to ensure data consistency and to prevent race conditions. Race conditions occur when multiple threads access shared data simultaneously, leading to unpredictable results. To avoid these issues, synchronization mechanisms are employed to control the access of multiple threads to shared resources. In addition, synchronization is also used to prevent deadlocks, which occur when two or more threads are waiting indefinitely for each other to release resources, livelocks, where threads continuously change their state in response to each other without making progress, and resource starvation, where a thread is perpetually denied access to resources it needs to proceed.

## Critical Section

A critical section is a part of the code that accesses shared resources and must not be executed by more than one thread at a time. To protect critical sections, synchronization mechanisms such as mutexes (mutual exclusion locks) are used. A mutex allows only one thread to enter the critical section at a time, ensuring that shared data is accessed in a controlled manner.

### Properties of Critical Section Solutions

For any critical section synchronization mechanism to be correct and efficient, it must satisfy three fundamental properties:

1. Mutual Exclusion (Safety Property): **Only one thread per critical section** - At most one thread can be executing in the critical section at any given time. 
2. Progress (Liveness Property): **If the critical section is free with threads waiting, a thread should enter at some point** - The system should not get stuck in a state where the critical section is available but no waiting thread is allowed to enter.
3. Bounded Waiting (Fairness Property): **Every thread that wants to go in should be able to at some point** - There should be a bound on the number of times other threads can enter the critical section after a thread has made a request and before that thread is granted access.

## Low-Level Primitives

### Atomicity 

Atomicity refers to operations that are completed as a single, indivisible step. In the context of synchronization, atomic operations ensure that a sequence of operations on shared data is executed without interruption, preventing other threads from interfering during the operation. Examples of atomic operations include reading or writing a single variable, incrementing a counter, or performing compare-and-swap (CAS) operations.

### Spinlocks

A spinlock is a simple synchronization mechanism that uses busy-waiting to protect critical sections. When a thread attempts to acquire a spinlock, it continuously checks (or "spins") until the lock becomes available. Spinlocks are efficient for short critical sections where the wait time is expected to be minimal, as they avoid the overhead of context switching associated with traditional locks. However, they can lead to wasted CPU cycles if the wait time is long.

### Mutexes

A mutex (mutual exclusion) is a synchronization primitive that allows only one thread to access a critical section at a time. When a thread locks a mutex, other threads attempting to lock the same mutex will be blocked until the mutex is unlocked. Mutexes are suitable for protecting longer critical sections where the overhead of blocking and context switching is justified. This mechanism doesn’t waste CPU cycles like spinlocks do but is computationally more expensive because of the context switching involved.

### Futexes

A futex (fast userspace mutex) is a hybrid synchronization mechanism that combines the efficiency of spinlocks with the blocking capabilities of mutexes. Futexes allow threads to perform fast, non-competitive locking in user space without kernel intervention. When competition occurs, the futex falls back to a kernel-level wait, allowing the thread to sleep until the lock becomes available. This approach minimizes the overhead of context switching while still providing robust synchronization for critical sections.

