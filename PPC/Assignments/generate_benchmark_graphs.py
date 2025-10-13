import json
import matplotlib.pyplot as plt
import seaborn as sns
import pandas as pd
import numpy as np
from pathlib import Path
import os

graphs_dir = 'images'
if not os.path.exists(graphs_dir):
    os.makedirs(graphs_dir)

plt.style.use('default')
sns.set_palette("husl")
plt.rcParams['figure.figsize'] = (12, 8)
plt.rcParams['font.size'] = 10
plt.rcParams['axes.titlesize'] = 14
plt.rcParams['axes.labelsize'] = 12
plt.rcParams['xtick.labelsize'] = 10
plt.rcParams['ytick.labelsize'] = 10
plt.rcParams['legend.fontsize'] = 10

def get_implementation_order():
    """Define the specific order for implementations"""
    return ['forkjoin1k', 'forkjoin5k', 'forkjoin10k', 'masterworker', 'scattergather']

def load_benchmark_data():
    """Load and process benchmark data"""
    with open('benchmark_result.json', 'r') as f:
        data = json.load(f)
    
    processed_data = []
    
    for result in data:
        benchmark_type = "Sequential" if "Sequential" in result["benchmark"] else "Parallel"
        
        record = {
            'benchmark_type': benchmark_type,
            'score': result['primaryMetric']['score'],
            'scoreError': result['primaryMetric']['scoreError'],
            'scoreUnit': result['primaryMetric']['scoreUnit']
        }
        
        if benchmark_type == "Parallel":
            params = result.get('params', {})
            record['implementation'] = params.get('implementation', 'unknown')
            record['nThreads'] = int(params.get('nThreads', 1))
        else:
            record['implementation'] = 'sequential'
            record['nThreads'] = 1
            
        processed_data.append(record)
    
    return pd.DataFrame(processed_data)

def create_performance_comparison_chart(df):
    """Bar chart comparing performance between implementations and threads"""
    plt.figure(figsize=(16, 8))
    
    sequential_df = df[df['implementation'] == 'sequential']
    parallel_df = df[df['implementation'] != 'sequential']
    
    if not parallel_df.empty:
        performance_pivot = parallel_df.pivot_table(values='score', index='implementation', 
                                                  columns='nThreads', aggfunc='mean')
        error_pivot = parallel_df.pivot_table(values='scoreError', index='implementation', 
                                            columns='nThreads', aggfunc='mean')
        
        ordered_implementations = get_implementation_order()
        available_implementations = [impl for impl in ordered_implementations if impl in performance_pivot.index]
        performance_pivot = performance_pivot.reindex(available_implementations)
        error_pivot = error_pivot.reindex(available_implementations)
        
        ax = performance_pivot.plot(kind='bar', width=0.8, alpha=0.8, edgecolor='black', 
                                   yerr=error_pivot, capsize=3, error_kw={'elinewidth': 1, 'capthick': 1})
        
        for container in ax.containers:
            if hasattr(container, 'datavalues'):
                ax.bar_label(container, labels=[f'{v:.0f}' for v in container.datavalues], 
                            rotation=90, padding=5, fontsize=8, fontweight='bold')
    
    if not sequential_df.empty:
        seq_score = sequential_df['score'].mean()
        seq_error = sequential_df['scoreError'].mean()
        n_implementations = len(parallel_df['implementation'].unique()) if not parallel_df.empty else 0
        
        seq_position = n_implementations - 1 + 0.75
        
        n_threads = len(parallel_df['nThreads'].unique()) if not parallel_df.empty else 1
        bar_width = 0.8 / n_threads if n_threads > 0 else 0.8
        
        plt.bar(seq_position, seq_score, width=bar_width, alpha=0.8, 
                color='red', edgecolor='black', label='Sequential', 
                yerr=seq_error, capsize=3, error_kw={'elinewidth': 1, 'capthick': 1})
        
        plt.text(seq_position, seq_score + seq_error + seq_score * 0.01,
                f'{seq_score:.0f}', ha='center', va='bottom', 
                fontsize=8, fontweight='bold')
        
        current_labels = list(performance_pivot.index) if not parallel_df.empty else []
        current_labels.append('Sequential')
        
        tick_positions = list(range(n_implementations)) + [seq_position]
        plt.xticks(tick_positions, current_labels, rotation=45, ha='right')
        
        plt.xlim(-0.5, seq_position + 0.5)
    
    plt.xlabel('Implementation', fontweight='bold')
    plt.ylabel('Time (ms/op)', fontweight='bold')
    plt.title('Performance Comparison: All Implementations by Number of Threads\n(Lower is Better)', fontweight='bold', pad=20)
    plt.grid(axis='y', alpha=0.3, linestyle='--')
    
    plt.legend(title='Threads', bbox_to_anchor=(1.05, 1), loc='upper left')
    plt.tight_layout()
    plt.savefig(os.path.join(graphs_dir, 'performance_comparison.png'), dpi=300, bbox_inches='tight')
    

def create_speedup_analysis(df):
    """Speedup analysis compared to sequential implementation"""
    sequential_score = df[df['implementation'] == 'sequential']['score'].mean()
    
    if pd.isna(sequential_score):
        print("No sequential implementation found to calculate speedup")
        return
    
    parallel_df = df[df['benchmark_type'] == 'Parallel'].copy()
    parallel_df['speedup'] = sequential_score / parallel_df['score']
    
    plt.figure(figsize=(14, 8))
    
    speedup_pivot = parallel_df.pivot_table(values='speedup', index='nThreads', 
                                          columns='implementation', aggfunc='mean')
    
    ordered_implementations = get_implementation_order()
    available_implementations = [impl for impl in ordered_implementations if impl in speedup_pivot.columns]
    speedup_pivot = speedup_pivot.reindex(columns=available_implementations)
    
    ax = speedup_pivot.plot(kind='bar', width=0.8, alpha=0.8, edgecolor='black')
    
    for container in ax.containers:
        if hasattr(container, 'datavalues'):
            ax.bar_label(container, labels=[f'{v:.2f}x' for v in container.datavalues], 
                        rotation=90, padding=10, fontsize=9, fontweight='bold')
    
    plt.xlabel('Number of Threads', fontweight='bold')
    plt.ylabel('Speedup (x times faster)', fontweight='bold')
    plt.title('Speedup Analysis vs Sequential Implementation', fontweight='bold', pad=20)
    plt.ylim(0, 5)
    plt.legend(bbox_to_anchor=(1.05, 1), loc='upper left')
    plt.grid(axis='y', alpha=0.3)
    plt.xticks(rotation=0)
    
    plt.tight_layout()
    plt.savefig(os.path.join(graphs_dir, 'speedup_analysis.png'), dpi=300, bbox_inches='tight')
    

def create_efficiency_analysis(df):
    """Efficiency analysis as bar chart grouped by threads (speedup/nThreads)"""
    sequential_score = df[df['implementation'] == 'sequential']['score'].mean()
    
    if pd.isna(sequential_score):
        return
        
    parallel_df = df[df['benchmark_type'] == 'Parallel'].copy()
    parallel_df['speedup'] = sequential_score / parallel_df['score']
    parallel_df['efficiency'] = parallel_df['speedup'] / parallel_df['nThreads'] * 100

    
    plt.figure(figsize=(14, 8))
    
    efficiency_pivot = parallel_df.pivot_table(values='efficiency', index='nThreads', 
                                             columns='implementation', aggfunc='mean')
    
    ordered_implementations = get_implementation_order()
    available_implementations = [impl for impl in ordered_implementations if impl in efficiency_pivot.columns]
    efficiency_pivot = efficiency_pivot.reindex(columns=available_implementations)
    
    ax = efficiency_pivot.plot(kind='bar', width=0.8, alpha=0.8, edgecolor='black')
    
    for container in ax.containers:
        if hasattr(container, 'datavalues'):
            ax.bar_label(container, labels=[f'{v:.1f}%' for v in container.datavalues], 
                        rotation=90, padding=5, fontsize=9, fontweight='bold')
        
    plt.xlabel('Number of Threads', fontweight='bold')
    plt.ylabel('Efficiency (%)', fontweight='bold')
    plt.ylim(0, 100)
    plt.title('Efficiency by Number of Threads and Implementation\n(Speedup / Number of Threads)', fontweight='bold', pad=20)
    plt.xticks(rotation=0)
    plt.legend(bbox_to_anchor=(1.05, 1), loc='upper left')
    plt.grid(axis='y', alpha=0.3)
    
    plt.tight_layout()
    plt.savefig(os.path.join(graphs_dir, 'efficiency_analysis.png'), dpi=300, bbox_inches='tight')
    

def create_summary_statistics_table(df):
    """Create summary table with statistics"""
    summary_stats = []
    
    ordered_implementations = get_implementation_order() + ['sequential']
    available_implementations = [impl for impl in ordered_implementations if impl in df['implementation'].unique()]
    
    for impl in available_implementations:
        impl_data = df[df['implementation'] == impl]
        
        stats = {
            'Implementation': impl,
            'Average (ms)': impl_data['score'].mean(),
            'Std Deviation': impl_data['score'].std(),
            'Minimum (ms)': impl_data['score'].min(),
            'Maximum (ms)': impl_data['score'].max(),
            'Median (ms)': impl_data['score'].median(),
            'Coef. Variation (%)': (impl_data['score'].std() / impl_data['score'].mean()) * 100
        }
        
        summary_stats.append(stats)
    
    summary_df = pd.DataFrame(summary_stats)
    summary_df = summary_df.round(2)
    
    summary_df.to_csv(os.path.join(graphs_dir, 'benchmark_summary_statistics.csv'), index=False)
    print("\nSummary Statistics:")
    print(summary_df.to_string(index=False))

if __name__ == "__main__":
    """Main function"""
    print("Loading benchmark data...")
    df = load_benchmark_data()
    
    print(f"Data loaded: {len(df)} records")
    print(f"Implementations found: {df['implementation'].unique()}")
    print(f"Thread numbers: {sorted(df['nThreads'].unique())}")
    
    print("\nGenerating graphs...")
    
    create_performance_comparison_chart(df)
    create_speedup_analysis(df)
    create_efficiency_analysis(df)
    create_summary_statistics_table(df)
    
    print("\nAll graphs have been generated and saved!")
    print(f"Created files in '{graphs_dir}/' directory:")
    print("- performance_comparison.png")
    print("- speedup_analysis.png")
    print("- efficiency_analysis.png")
    print("- benchmark_summary_statistics.csv")
