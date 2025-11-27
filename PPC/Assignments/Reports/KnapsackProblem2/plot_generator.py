import matplotlib.pyplot as plt

data = {
    "Kotlin": {
        "Sequential": 42888,
        "Coroutine": 9513,
        "Channel": 34285,
        "Actor": 36464
    },
    "Go": {
        "Sequential": 72728.21,
        "Goroutine": 15390.88,
        "Channel": 32535.76,
        "Actor": 32849.94
    },
    "Rust": {
        "Sequential": 58100.710099,
        "Channel": 40296.727424,
        "Coroutine": 11641.160699
    }
}

# -------------------------------------------------------
# Graph 1 — Bars by Language
# -------------------------------------------------------
for lang, results in data.items():
    labels = list(results.keys())
    values = list(results.values())

    plt.figure(figsize=(8, 5))
    bars = plt.bar(labels, values)
    plt.title(f"Execution Times — {lang}")
    plt.ylabel("Time (ms)")
    plt.xticks(rotation=45)

    # Add values on top of bars
    for bar in bars:
        height = bar.get_height()
        plt.text(bar.get_x() + bar.get_width() / 2, height, f'{height:.2f}', ha='center', va='bottom')

    plt.tight_layout()
    plt.show()

# -------------------------------------------------------
# Graph 2 — Global Comparison by Technique
# -------------------------------------------------------
# Aggregate data by technique
techniques = {}
for lang, results in data.items():
    for tech, value in results.items():
        techniques.setdefault(tech, []).append(value)

# Averages by technique
avg_techniques = {tech: sum(vals) / len(vals) for tech, vals in techniques.items()}

plt.figure(figsize=(8, 5))
bars = plt.bar(avg_techniques.keys(), avg_techniques.values())
plt.title("Global Comparison — Average Times by Technique")
plt.ylabel("Average Time (ms)")
plt.xticks(rotation=45)

# Add values on top of bars
for bar in bars:
    height = bar.get_height()
    plt.text(bar.get_x() + bar.get_width() / 2, height, f'{height:.2f}', ha='center', va='bottom')

plt.tight_layout()
plt.show()
