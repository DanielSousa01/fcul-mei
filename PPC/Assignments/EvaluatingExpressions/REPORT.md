# CUDA Benchmark Report

## Parallelization approach
- Each candidate expression is compiled into a small Python callable (`kernel_func`) that maps input columns to torch ops. A batch of up to 32 compiled kernels (`TASK_BATCH`) is executed together with `torch.stack`, so PyTorch launches fused elementwise kernels per batch instead of per expression.
- All input columns and the target column are moved to device once per dataset and kept resident as torch tensors. The evaluation loop only invokes device-side math; host work is limited to bookkeeping the best MSE.

## Kernels and memory copies
- Per batch (<=32 expressions): one elementwise kernel to generate predictions, one elementwise kernel for MSE reduction ⇒ ~2 kernels × ceil(n_funcs / 32) per dataset.
- Host→device: one copy per CSV column (including target) at dataset load. Device→host: only scalar `.item()` reads when tracking the current best error.

## Minimizing kernel calls
- Batching expressions (size 32) amortizes launch overhead: multiple expressions are evaluated inside a single stacked tensor op.
- Pure tensor math; no per-row Python loops on GPU paths.

## Minimizing data transfers
- Inputs and target are transferred once and reused for all expressions. No intermediate host transfers; only scalar readback for best-MSE tracking.

## Threads and blocks selection
- Delegated to PyTorch CUDA backend; it auto-selects grid/block sizes for the elementwise ops. No manual `<<<grid, block>>>` configuration in this code.

## Shared memory usage
- Not explicitly used. Workload is simple elementwise math without a reuse pattern that benefits from shared memory; the backend relies on caches and register reuse.

## Branch divergence handling
- Expressions are branch-free arithmetic; there is no in-kernel control flow, so warp divergence is negligible. Branching exists only in Python when updating the best score.

## GPU vs CPU crossover
- The script reports winners and speedups per dataset and detects the first case where GPU overtakes CPU via the "Transition point" printout. Expect GPU to win when both row count and function count are moderate-to-large (dataset-dependent; check the printed transition for the provided inputs).

## Adapting for Genetic Programming (GP) loops
- Keep data on GPU across generations; avoid recreating tensors each generation.
- Cache compiled kernels for recurring expressions to skip repeated `exec`/string replacements.
- Evaluate populations in batches (reuse `TASK_BATCH` stacking); keep tensors reused to avoid allocations.
- Only read back scalar fitness values; keep predictions on device.
- Consider pre-parsing expressions into a small DSL mapped directly to torch ops to lower Python overhead.
- Optionally overlap population preparation and evaluation with CUDA streams to hide latency.

## File reference
- Core benchmark logic: [sequential_parallel_benchmark.py](sequential_parallel_benchmark.py)
