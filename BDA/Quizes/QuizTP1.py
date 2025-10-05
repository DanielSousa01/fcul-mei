import pandas as pd

def read_data() -> pd.DataFrame:
    data = pd.read_csv('dias_catalogue.csv')
    data = data[['age', 'FeH']].dropna()
    return data


def group_by_age(data: pd.DataFrame) -> None:
    bins = [0, 100, 1000, float('inf')]
    labels = ['Young', 'Intermediate', 'Old']

    data['AgeClass'] = pd.cut(data['age'], bins=bins, labels=labels)

    summary = data.groupby('AgeClass')['FeH'].agg(['mean', 'std', 'count'])
    summary.columns = ['Mean_FeH', 'Std_FeH', 'Number_of_clusters']
    summary = summary.sort_values(by='Mean_FeH', ascending=False)
    summary.dropna(inplace=True)

    print(summary)

    summary.to_csv('summary.csv', index=True)


if __name__ == '__main__':
    data = read_data()
    group_by_age(data)
