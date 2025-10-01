# Test 1 

## 1. Consider the following code that estimates PI using the monte-carlo method, in parallel.

```java
public static void main(String[] args) {
    int nPoints = 100000000;
    int chunkSize = nPoints / nThreads;
    int[] results = new int[nThreads];
    Thread[] ts = new Thread[nThreads];
    for (int i = 0; i < nThreads; i++) {
        int start = i * chunkSize;
        int end = (i == nThreads - 1) ? nPoints : (i + 1) * chunkSize;
        ﬁnal int index = i;
        ts[i] = new Thread ( () -> {
            for (int j = start; j < end; j++) {
                if (isInsideCircle(ThreadLocalRandom.current().nextDouble(), ThreadLocalRandom.
                current().nextDouble())) {
                    results[index] += 1;
                }
            }
        });
        ts[i].start();
    }
    int sum = 0;
    for (int val : results)
        sum += val;
    double result = 4.0 * (double)sum / (double)nPoints;
    System.out.println("Result is: " + result + " : " + sum);
}
```

### a) This program may not print a reasonable result (close to 3.14). Why?

The program may not print a reasonable result because the main thread does not wait for the worker threads to complete their execution before summing up the results. As a result, the `results` array may still contain zeros or incomplete counts when the sum is calculated, leading to an incorrect estimation of PI.

## 2. Consider a program that uses parallel streams to compute the minimum of a given, immediately available list.

### a) Can this operation be done in parallel? If no, why not. If yes, how is it parallelizable?

Yes, this operation can be done in parallel. This is because this list can be divided into smaller sublists for each thread to process independently. Each thread can compute the minimum of its assigned sublist, and then the overall minimum can be determined by comparing the minimums from each thread.

### b) Explain how the fork-join framework implements a M-N scheduler.

The fork-join framework implements an M-N scheduler by mapping M tasks to N threads. It uses a work-stealing algorithm where each thread maintains its own deque (double-ended queue) of tasks. When a thread finishes its tasks, it can "steal" tasks from the tail of another thread's deque, allowing for efficient load balancing and maximizing CPU utilization.

### c) Is there any advantage in using the ForkJoin framework for this example, comparing to an approach similar to question 1? Explain.

Yes, the ForkJoin framework offers significant advantages for finding the minimum of a list:

1. **Work Stealing**: Automatically balances load across threads when some finish early, unlike the static partitioning in question 1.
2. **Built-in Synchronization**: Handles thread coordination and result combination automatically, avoiding the error-prone manual management shown in question 1 (missing `join()` calls).
3. **Thread Pool Efficiency**: Reuses a pre-configured thread pool instead of creating new threads each time, reducing overhead for simple operations like finding a minimum.