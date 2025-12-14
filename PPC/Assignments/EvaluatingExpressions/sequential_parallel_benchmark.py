import numpy as np
import pandas as pd
import torch
import time
import os
import glob

TASK_BATCH = 32
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
torch.set_grad_enabled(False)

OPS = {
    "sinf": torch.sin,
    "cosf": torch.cos,
    "tanf": torch.tan,
    "sqrtf": torch.sqrt,
    "expf": torch.exp
}

def benchmark_sequential(csv_file, functions_file):
    df = pd.read_csv(csv_file)
    funs = [line.strip() for line in open(functions_file).readlines()]
    
    def score(line):
        for u in ["sinf", "cosf", "tanf", "sqrtf", "expf"]:
            line = line.replace(u, f"np.{u[:-1]}")
        for c in df.columns:
            line = line.replace(f"_{c}_", f"(df[\"{c}\"].values)")
        a = eval(line)
        b = df["y"]
        e = np.square(np.subtract(a, b)).mean()
        return e
    
    start = time.time()
    r = min([(score(line), line) for line in funs])
    elapsed = time.time() - start
    
    return elapsed, r[0], r[1]

def generate_kernel_code(expr, input_cols):
    kernel_expr = expr
    for k in OPS:
        kernel_expr = kernel_expr.replace(k, f"OPS['{k}']")
    
    for c in input_cols:
        kernel_expr = kernel_expr.replace(f"_{c}_", f"X['{c}']")
    
    kernel_code = f"""
def kernel_func(X, OPS):
    return {kernel_expr}
"""
    return kernel_code

def compile_kernel(expr, input_cols):
    kernel_code = generate_kernel_code(expr, input_cols)
    env = {}
    exec(kernel_code.strip(), {}, env)
    return env["kernel_func"]

def benchmark_parallel(csv_file, functions_file):
    df = pd.read_csv(csv_file)
    funs = [line.strip() for line in open(functions_file).readlines()]
    
    cols = list(df.columns)
    target_col = cols[-1]
    input_cols = cols[:-1]
    
    X = {c: torch.tensor(df[c].values, dtype=torch.float64, device=device)
         for c in cols}
    y = X[target_col]
    
    compiled = [compile_kernel(f, input_cols) for f in funs]
    
    start = time.time()
    
    best_err = float('inf')
    best_expr = None
    
    for i in range(0, len(compiled), TASK_BATCH):
        block = compiled[i:i + TASK_BATCH]
        preds = torch.stack([kernel(X, OPS) for kernel in block])
        errs = torch.mean((preds - y) ** 2, dim=1)
        
        for j, err in enumerate(errs):
            idx_global = i + j
            err_val = err.item()

            if err_val < best_err:
                best_err = err_val
                best_expr = funs[idx_global]
        
        del preds, errs
        torch.cuda.empty_cache()
    
    elapsed = time.time() - start
    
    return elapsed, best_err, best_expr


print("=" * 100)
print(f"CPU vs GPU Benchmark - Device: {device}")
print("=" * 100 + "\n")

test_files = sorted(glob.glob("test_cases/data_*.csv"))

if not test_files:
    print("ERROR: No test files found in test_cases/")
    print("Run 'python generate_inputs.py' first!")
    exit(1)

results = []

for csv_file in test_files:
    name = os.path.basename(csv_file).replace("data_", "").replace(".csv", "")
    functions_file = csv_file.replace("data_", "functions_").replace(".csv", ".txt")
    
    df = pd.read_csv(csv_file)
    n_rows = len(df)
    n_functions = sum(1 for _ in open(functions_file))
    
    print(f"Testing: {name}")
    print(f"  Rows: {n_rows:,}, Functions: {n_functions:,}")
    
    # CPU
    print("  Running CPU version...", end=" ", flush=True)
    cpu_time, cpu_mse, cpu_expr = benchmark_sequential(csv_file, functions_file)
    print(f"✓ {cpu_time:.4f}s")
    
    # GPU
    print("  Running GPU version...", end=" ", flush=True)
    gpu_time, gpu_mse, gpu_expr = benchmark_parallel(csv_file, functions_file)
    print(f"✓ {gpu_time:.4f}s")
    
    # Speedup
    speedup = cpu_time / gpu_time
    winner = "GPU" if speedup > 1.0 else "CPU"
    
    print(f"  Speedup: {speedup:.2f}x ({winner} wins!)")
    print()
    
    results.append({
        "name": name,
        "rows": n_rows,
        "functions": n_functions,
        "cpu_time": cpu_time,
        "gpu_time": gpu_time,
        "speedup": speedup,
        "winner": winner,
        "mse": cpu_mse
    })


print("=" * 100)
print("BENCHMARK RESULTS SUMMARY")
print("=" * 100)
print(f"{'Test Case':<20} {'Rows':<10} {'Funcs':<8} {'CPU(s)':<10} {'GPU(s)':<10} {'Speedup':<10} {'Winner'}")
print("-" * 100)

for r in results:
    print(f"{r['name']:<20} {r['rows']:<10,} {r['functions']:<8,} "
          f"{r['cpu_time']:<10.4f} {r['gpu_time']:<10.4f} "
          f"{r['speedup']:<10.2f}x {r['winner']}")

print("\n" + "=" * 100)
print("KEY INSIGHTS:")
print("=" * 100)

cpu_wins = sum(1 for r in results if r['winner'] == 'CPU')
gpu_wins = sum(1 for r in results if r['winner'] == 'GPU')

print(f"CPU wins: {cpu_wins}/{len(results)}")
print(f"GPU wins: {gpu_wins}/{len(results)}")
print(f"Best speedup: {max(r['speedup'] for r in results):.2f}x ({max(results, key=lambda x: x['speedup'])['name']})")
print(f"Worst speedup: {min(r['speedup'] for r in results):.2f}x ({min(results, key=lambda x: x['speedup'])['name']})")

transition = None
for i, r in enumerate(results):
    if i > 0 and results[i-1]['winner'] == 'CPU' and r['winner'] == 'GPU':
        transition = r
        break

if transition:
    print(f"\nTransition point: {transition['name']}")
    print(f"  → {transition['rows']:,} rows × {transition['functions']:,} functions")
    print(f"  → Complexity: {transition['rows'] * transition['functions']:,}")

print("=" * 100)
