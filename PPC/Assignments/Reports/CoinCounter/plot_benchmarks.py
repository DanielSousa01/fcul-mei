import json
import matplotlib.pyplot as plt
import numpy as np

# Load data from JSON files
with open('benchmark_results_par.json', 'r') as f:
    par_data = json.load(f)

with open('benchmark_results_seq.json', 'r') as f:
    seq_data = json.load(f)

# Extract sequential data
seq_results = {}
for entry in seq_data:
    coins_size = int(entry['params']['coinsSize'])
    score = entry['primaryMetric']['score']
    error = entry['primaryMetric']['scoreError']
    seq_results[coins_size] = {'score': score, 'error': error}

# Extract parallel data
par_results = {}
for entry in par_data:
    benchmark = entry['benchmark']
    threshold = int(entry['params']['threshold'])
    score = entry['primaryMetric']['score']
    error = entry['primaryMetric']['scoreError']
    
    # Extract problem size from benchmark name
    if 'Par20' in benchmark:
        size = 20
    elif 'Par30' in benchmark:
        size = 30
    elif 'Par50' in benchmark:
        size = 50
    elif 'Par60' in benchmark:
        size = 60
    else:
        continue
    
    if size not in par_results:
        par_results[size] = {}
    par_results[size][threshold] = {'score': score, 'error': error}

# Configure plot style
plt.style.use('seaborn-v0_8-darkgrid')
colors = plt.cm.Set2(np.linspace(0, 1, 8))

# Create comparison plot
fig, axes = plt.subplots(2, 2, figsize=(15, 12))
fig.suptitle('Performance Comparison: Sequential vs Parallel', fontsize=16, fontweight='bold')

sizes = sorted(par_results.keys())
for idx, size in enumerate(sizes):
    ax = axes[idx // 2, idx % 2]
    
    thresholds = sorted(par_results[size].keys())
    par_scores = [par_results[size][t]['score'] for t in thresholds]
    par_errors = [par_results[size][t]['error'] for t in thresholds]
    
    # Plot parallel version with different thresholds
    x_pos = np.arange(len(thresholds))
    bars = ax.bar(x_pos, par_scores, yerr=par_errors, capsize=5, 
                   color=colors[idx], alpha=0.7, label='Parallel')
    
    # Add value labels on top of bars
    for i, (score, error) in enumerate(zip(par_scores, par_errors)):
        ax.text(i, score + error, f'{score:.2f}', 
                ha='center', va='bottom', fontsize=9, fontweight='bold')
    
    # Add horizontal line for sequential version (if exists)
    if size in seq_results:
        seq_score = seq_results[size]['score']
        seq_error = seq_results[size]['error']
        ax.axhline(y=seq_score, color='red', linestyle='--', linewidth=2, label='Sequential')
        ax.fill_between([-0.5, len(thresholds)-0.5], 
                        seq_score - seq_error, 
                        seq_score + seq_error, 
                        color='red', alpha=0.2)
        
        # Add sequential time label
        ax.text(len(thresholds) - 0.5, seq_score, f' {seq_score:.2f}', 
                ha='left', va='center', fontsize=9, fontweight='bold', color='red')
    
    ax.set_xlabel('Threshold', fontsize=11)
    ax.set_ylabel('Time (ms)', fontsize=11)
    ax.set_title(f'Problem with {size} coins', fontsize=12, fontweight='bold')
    ax.set_xticks(x_pos)
    ax.set_xticklabels(thresholds)
    ax.legend()
    ax.grid(True, alpha=0.3)

plt.tight_layout()
plt.savefig('benchmark_comparison.png', dpi=300, bbox_inches='tight')
print("✓ Graph 'benchmark_comparison.png' generated successfully!")

# Summary table
print("\n" + "="*80)
print("RESULTS SUMMARY")
print("="*80)

for size in sizes:
    print(f"\n{size} coins:")
    print("-" * 60)
    
    if size in seq_results:
        seq_time = seq_results[size]['score']
        print(f"  Sequential: {seq_time:.2f} ± {seq_results[size]['error']:.2f} ms")
    
    print("  Parallel:")
    for threshold in sorted(par_results[size].keys()):
        par_time = par_results[size][threshold]['score']
        par_error = par_results[size][threshold]['error']
        
        speedup = "N/A"
        if size in seq_results:
            speedup = f"{seq_results[size]['score'] / par_time:.2f}x"
        
        print(f"    Threshold {threshold:2d}: {par_time:8.2f} ± {par_error:6.2f} ms | Speedup: {speedup}")

print("\n" + "="*80)