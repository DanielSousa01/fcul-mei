import numpy as np
import pandas as pd
import random as rd
import os

FEATURES = 10
cols = "abcdefghijkmnopqrstuv"
columns = list(cols)[:FEATURES]

test_configs = [
    (1000, 100, 2, "small_simple"),
    (5000, 200, 2, "small_medium"),
    
    (10000, 500, 3, "medium_balanced"),
    (20000, 1000, 3, "medium_complex"),
    
    (50000, 1000, 4, "large_moderate"),
    (100000, 1000, 4, "large_standard"),
    (100000, 2000, 4, "large_heavy"),
    
    (200000, 2000, 5, "xlarge_extreme"),
]

unary_funs = ["sinf", "cosf", "sqrtf"]
operators = ["+", "-"]

def random_program(depth=4):
    """Gera expressão aleatória com profundidade controlada."""
    r = rd.randint(0, 100)
    if depth == 0 or r < 30:
        c = rd.choice(columns)
        return f"_{c}_"
    elif r < 80:
        c = rd.choice(unary_funs)
        r = random_program(depth - 1)
        return f"{c}({r})"
    else:
        c = rd.choice(operators)
        r1 = random_program(depth - 1)
        r2 = random_program(depth - 1)
        return f"({r1}) {c} ({r2})"

os.makedirs("test_cases", exist_ok=True)

print("=" * 80)
print("Generating test cases for CPU vs GPU comparison")
print("=" * 80 + "\n")

for n_rows, n_functions, depth, name in test_configs:
    print(f"Creating {name}:")
    print(f"  - Rows: {n_rows:,}")
    print(f"  - Functions: {n_functions:,}")
    print(f"  - Depth: {depth}")
    
    x = np.random.rand(n_rows, FEATURES)
    df = pd.DataFrame(x, columns=columns)
    df["y"] = np.sin(df["a"].values) + np.cos(df["b"].values) + np.random.rand(n_rows) * 0.001
    
    csv_file = f"test_cases/data_{name}.csv"
    df.to_csv(csv_file, index=False)
    print(f"  ✓ Created {csv_file}")
    
    functions = []
    for _ in range(n_functions):
        functions.append(random_program(depth))
    
    functions_file = f"test_cases/functions_{name}.txt"
    with open(functions_file, "w") as f:
        for func in functions:
            f.write(func + "\n")
    print(f"  ✓ Created {functions_file}")
    
    avg_len = sum(len(f) for f in functions) / len(functions)
    print(f"  ✓ Avg expression length: {avg_len:.1f} chars")
    print()

print("=" * 80)
print("Summary of test cases:")
print("=" * 80)
print(f"{'Name':<20} {'Rows':<12} {'Functions':<12} {'Complexity':<12} {'GPU Expected'}")
print("-" * 80)

for n_rows, n_functions, depth, name in test_configs:
    complexity = n_rows * n_functions
    gpu_wins = "✓ Yes" if complexity > 5_000_000 else "✗ No" if complexity < 1_000_000 else "? Maybe"
    print(f"{name:<20} {n_rows:<12,} {n_functions:<12,} {complexity:<12,} {gpu_wins}")

print("\n" + "=" * 80)
print("Test files created in ./test_cases/")
print("Use these files to benchmark CPU vs GPU performance")
print("=" * 80)

        


