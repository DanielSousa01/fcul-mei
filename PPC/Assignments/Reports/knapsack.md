# Assignment #1: Genetic Algorithm for the Knapsack Problem

## What were the parallelization strategies used? How did you implement them and why?

In this assignment, three distinct parallelization strategies were implemented:

1. **Master-Worker Pattern with Poison Pill**
2. **Scatter-Gather Pattern**
3. **Fork-Join Pattern**

All these strategies apply parallelization to each generation step of the algorithm, specifically targeting the methods that evaluate the fitness of individuals in the population. This focus is strategic since fitness evaluation is the most computationally intensive task and benefits significantly from parallel execution.

### Master-Worker Pattern with Poison Pill

The Master-Worker uses a master thread to be responsible for distributing tasks to multiple worker threads. This implementation creates a thread pool with a number of workers equal to the available processor cores (`Runtime.getRuntime().availableProcessors()`). The master divides the population into equal chunks based on a calculated chunk size (`POP_SIZE / numWorkers`), ensuring balanced workload distribution among workers.

The implementation consists of three key components:

**Core Classes:**

- `Worker` class that implements `Runnable` - represents a worker thread that continuously polls the task queue for work
- `Task` class - encapsulates the work to be performed, containing a `TaskType` and a `Runnable` with the actual logic
- `TaskType` enum - distinguishes between regular tasks (`RUNNABLE`) and termination signals (`POISON_PILL`)

**Architecture:**
The master maintains a thread-safe `LinkedBlockingQueue<Task>` that serves as the communication channel between the master and workers. Workers are initialized once at the beginning of the algorithm and remain active throughout all generations, continuously processing tasks as they become available.

**Parallelized Operations:**
This pattern is applied to four critical genetic algorithm operations:

1. **Fitness Calculation** (`calculateFitness()`): Each worker evaluates the fitness of individuals in its assigned chunk of the population
2. **Best Individual Selection** (`bestOfPopulation()`): Workers find the best individual in their chunk using `AtomicReference` with compare-and-swap operations to safely update the global best
3. **Population Reproduction** (`calculateBestPopulation()`): Workers perform tournament selection and crossover operations to generate new individuals
4. **Mutation** (`mutate()`): Workers apply mutation to individuals in their assigned range based on the mutation probability

**Synchronization:**
Each parallelized step utilizes a `CountDownLatch` initialized with the number of workers. As each worker completes its assigned chunk of work, it calls `countDown()`, while the master waits using `await()` before proceeding to the next step. This mechanism ensures proper synchronization between generations.

**Lifecycle Management:**
Workers are initialized once at the beginning (`startWorkers()`) and terminated at the end by enqueueing a poison pill for each worker (`stopWorkers()`). This approach minimizes thread creation overhead and maintains consistent performance across generations.

### Scatter-Gather Pattern

The Scatter-Gather pattern divides the population into smaller chunks (scattered) across multiple threads for parallel processing, then collects the results (gathered) after completion. This implementation utilizes Java's `ExecutorService` with a fixed thread pool to manage parallel execution efficiently.

**Core Architecture:**

- Utilizes `Executors.newFixedThreadPool(maxThreads)` where `maxThreads` equals the number of available processor cores
- Employs a centralized `computeFutures()` method that handles the scatter-gather logic for all parallelized operations
- Creates `Future` objects to track and synchronize the completion of parallel tasks

**Scatter Phase:**
The `computeFutures()` method divides the work range into equal chunks based on `chunkSize = total operations / maxThreads`. Each thread receives a specific range to process:

- Thread 0: processes indices `[start, start + chunkSize)`
- Thread 1: processes indices `[start + chunkSize, start + 2 * chunkSize)`
- Last thread: processes remaining indices to handle any remainder from integer division

**Gather Phase:**
After submitting all tasks to the thread pool, the method calls `futures.forEach { it.get() }` to wait for all threads to complete their work before proceeding. This ensures synchronization between parallel operations.

**Parallelized Operations:**

1. **Fitness Calculation** (`calculateFitness()`): Each thread evaluates fitness for individuals in its assigned range
2. **Best Individual Selection** (`bestOfPopulation()`): Threads find local best individuals and use `AtomicReference` with compare-and-swap to update the global best
3. **Population Reproduction** (`calculateBestPopulation()`): Each thread performs tournament selection and crossover for its assigned range
4. **Mutation** (`mutate()`): Threads apply mutation to individuals in their assigned range based on mutation probability

**Thread Management:**
The thread pool is created once per algorithm execution and reused across all generations, minimizing thread creation overhead. The pool is properly shut down in a `finally` block to ensure resource cleanup.

### Fork-Join Pattern

The Fork-Join pattern utilizes Java's Fork-Join framework to recursively divide tasks into smaller subtasks until they reach a manageable size (threshold), then processes them in parallel and combines the results. This implementation leverages `ForkJoinPool` and `RecursiveAction` to achieve work-stealing parallelism.

**Core Architecture:**

- Utilizes `ForkJoinPool(maxThreads)` where `maxThreads` equals the number of available processor cores
- Implements `RecursiveAction` for each genetic algorithm operation requiring parallelization
- Employs a configurable threshold (default 1000) to determine when to stop subdividing tasks
- Uses `invokeAll()` to fork subtasks and automatically join their results

**Fork Phase (Task Subdivision):**
The `computeRange()` method implements the recursive subdivision logic:

- If the work range `(end - start)` is smaller than or equal to the threshold, executes the task directly
- Otherwise, splits the range in half at the midpoint: `mid = (start + end) / 2`
- Creates two `RecursiveAction` subtasks: left half `[start, mid)` and right half `[mid, end)`
- Uses `invokeAll(left, right)` to fork both subtasks for parallel execution

**Join Phase (Result Combination):**
The Fork-Join framework automatically handles the join phase through the `invokeAll()` method, which:

- Executes subtasks in parallel using available worker threads
- Implements work-stealing where idle threads can steal work from busy threads' queues
- Waits for all subtasks to complete before returning control to the caller

**Parallelized Operations:**

1. **Fitness Calculation** (`calculateFitness()`): Recursively divides the population range and evaluates fitness for individuals in parallel
2. **Best Individual Selection** (`bestOfPopulation()`): Finds local best individuals in parallel chunks and uses `AtomicReference` with compare-and-swap to update the global best
3. **Population Reproduction** (`calculateBestPopulation()`): Performs tournament selection and crossover operations in parallel across population segments
4. **Mutation** (`mutate()`): Applies mutation to individuals in parallel based on the mutation probability

**Work-Stealing Benefits:**
The Fork-Join framework provides automatic load balancing through work-stealing, where threads that complete their work early can steal tasks from other threads' work queues, maximizing CPU utilization and minimizing idle time.

**Threshold Optimization:**
The configurable threshold parameter allows fine-tuning the granularity of parallelization - smaller thresholds create more parallelism but increase overhead, while larger thresholds reduce overhead but may limit parallelization opportunities.

## How much better was the performance in parallel, compared to the sequential version?

## What was your experimental setup? Include all relevant details