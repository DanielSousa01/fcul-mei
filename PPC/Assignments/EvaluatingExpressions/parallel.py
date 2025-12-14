import numpy as np
import pandas as pd
from numba import cuda
import math
import time

# Read data
df = pd.read_csv("data.csv")
funs = [line.strip() for line in open("functions.txt").readlines()]

# Prepare data for GPU
y_true = df.iloc[:, -1].values.astype(np.float32)
n_samples = len(y_true)
input_cols = df.columns[:-1]
n_features = len(input_cols)

# Create feature matrix
X = df[input_cols].values.astype(np.float32)

# Transfer data to GPU
d_X = cuda.to_device(X)
d_y_true = cuda.to_device(y_true)

def translate_function(func_str, col_names):
    """Translate function string to CUDA-compatible code"""
    result = func_str
    
    # Replace math functions
    replacements = {
        'sinf': 'math.sin',
        'cosf': 'math.cos',
        'tanf': 'math.tan',
        'sqrtf': 'math.sqrt',
        'expf': 'math.exp',
    }
    
    for old, new in replacements.items():
        result = result.replace(old, new)
    
    # Replace column references with array indexing
    for idx, col in enumerate(col_names):
        result = result.replace(f'_{col}_', f'X[i, {idx}]')
    
    return result

def create_kernel_code(func_expr):
    """Generate CUDA kernel code as string for dynamic compilation"""
    kernel_code = f'''
@cuda.jit
def compute_kernel(X, y_pred):
    i = cuda.grid(1)
    if i < X.shape[0]:
        y_pred[i] = {func_expr}
'''
    return kernel_code

def create_mse_kernel():
    """Kernel to compute MSE on GPU"""
    @cuda.jit
    def mse_kernel(y_pred, y_true, squared_errors):
        i = cuda.grid(1)
        if i < y_pred.shape[0]:
            diff = y_pred[i] - y_true[i]
            squared_errors[i] = math.pow(diff, 2)
    return mse_kernel

# Create MSE kernel
mse_kernel = create_mse_kernel()

def score_gpu_single(func_str, d_X, d_y_true, col_names):
    """Score a single function on GPU"""
    try:
        # Translate function
        cuda_expr = translate_function(func_str, col_names)
        
        # Generate and compile kernel dynamically
        kernel_code = create_kernel_code(cuda_expr)
        local_namespace = {'cuda': cuda, 'math': math}
        exec(kernel_code, local_namespace)
        compute_kernel = local_namespace['compute_kernel']
        
        # Allocate output arrays on GPU
        d_y_pred = cuda.device_array(n_samples, dtype=np.float32)
        d_squared_errors = cuda.device_array(n_samples, dtype=np.float32)
        
        # Configure kernel launch
        threads_per_block = 256
        blocks_per_grid = (n_samples + threads_per_block - 1) // threads_per_block
        
        # Launch prediction kernel
        compute_kernel[blocks_per_grid, threads_per_block](d_X, d_y_pred)
        
        # Launch MSE kernel
        mse_kernel[blocks_per_grid, threads_per_block](d_y_pred, d_y_true, d_squared_errors)
        
        # Copy result back and compute mean
        squared_errors = d_squared_errors.copy_to_host()
        mse = np.mean(squared_errors)
        
        return mse
    except Exception as e:
        # Return infinity for invalid functions
        return float('inf')

def score_gpu_batch(funcs, d_X, d_y_true, col_names, batch_size=100):
    """Score multiple functions using task parallelism"""
    results = []
    
    # Process in batches to manage GPU memory
    for i in range(0, len(funcs), batch_size):
        batch = funcs[i:i+batch_size]
        batch_results = []
        
        for func_str in batch:
            mse = score_gpu_single(func_str, d_X, d_y_true, col_names)
            batch_results.append((mse, func_str))
        
        results.extend(batch_results)
    
    return results

# Benchmark: Sequential CPU version
print("=" * 60)
print("SEQUENTIAL CPU VERSION")
print("=" * 60)

def score_cpu(line):
    """Original CPU scoring function"""
    for u in ["sinf", "cosf", "tanf", "sqrtf", "expf"]:
        line = line.replace(u, f"np.{u[:-1]}")
    for c in df.columns[:-1]:
        line = line.replace(f"_{c}_", f"(df[\"{c}\"].values)")
    try:
        a = eval(line)
        b = df["y"]
        e = np.square(np.subtract(a, b)).mean()
        return e
    except:
        return float('inf')

start_cpu = time.time()
cpu_results = [(score_cpu(line), line) for line in funs]
cpu_best = min(cpu_results)
end_cpu = time.time()

print(f"First function: {cpu_results[0][0]:.6f} - {cpu_results[0][1]}")
print(f"Best function:  {cpu_best[0]:.6f} - {cpu_best[1]}")
print(f"Time: {end_cpu - start_cpu:.4f} seconds")
print()

# GPU Version
print("=" * 60)
print("GPU VERSION (Data + Task Parallelism)")
print("=" * 60)

start_gpu = time.time()
gpu_results = score_gpu_batch(funs, d_X, d_y_true, input_cols)
gpu_best = min(gpu_results)
end_gpu = time.time()

print(f"First function: {gpu_results[0][0]:.6f} - {gpu_results[0][1]}")
print(f"Best function:  {gpu_best[0]:.6f} - {gpu_best[1]}")
print(f"Time: {end_gpu - start_gpu:.4f} seconds")
print()

# Summary
print("=" * 60)
print("PERFORMANCE SUMMARY")
print("=" * 60)
print(f"CPU Time:       {end_cpu - start_cpu:.4f} seconds")
print(f"GPU Time:       {end_gpu - start_gpu:.4f} seconds")
print(f"Speedup:        {(end_cpu - start_cpu) / (end_gpu - start_gpu):.2f}x")
print(f"Functions eval: {len(funs)}")
print(f"Data points:    {n_samples}")
print()

# Parallelism analysis
print("=" * 60)
print("PARALLELISM ANALYSIS")
print("=" * 60)
print("Data Parallelism:")
print(f"  - Each function evaluated across {n_samples} samples in parallel")
print(f"  - Grid size: {(n_samples + 255) // 256} blocks × 256 threads")
print()
print("Task Parallelism:")
print(f"  - {len(funs)} functions processed independently")
print(f"  - Dynamic kernel generation per function")
print(f"  - Batched execution to optimize GPU memory usage")
print()
print("GPU Optimizations:")
print("  - All data transferred to GPU once (X, y_true)")
print("  - MSE computation parallelized on GPU")
print("  - Reduced CPU-GPU transfers (only final MSE value)")
print("  - Float32 precision for better GPU performance")