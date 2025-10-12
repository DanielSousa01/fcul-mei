import time
import pandas as pd

def get_dataframe_from_csv():
    return pd.read_csv('dias_catalogue.csv')

def read_data_csv(dataframe):
    time_ip = time.time()
    results = dataframe[dataframe.RA_ICRS > 50]
    time_fp = time.time()
    print('total time Pandas = ', time_fp-time_ip)

def get_stars_by_query(dataframe):
    time_i = time.time()

    results = dataframe[
        (dataframe.name.str.startswith('A')) &
        (dataframe.RA_ICRS < 180) &
        ((dataframe.DE_ICRS > 60) | (dataframe.DE_ICRS < -60))
    ]
    time_f = time.time()

    print('Total time Pandas to get stars by query = ', time_f - time_i)

def get_stars_filtered_sorted(dataframe):
    time_i = time.time()
    query = (dataframe['age'] > 4) & (dataframe['FeH'] < 0)

    results = dataframe[query].sort_values(by='RA_ICRS', ascending=True)
    time_f = time.time()
    print('Total time Pandas to get stars filtered and sorted = ', time_f - time_i)

def get_stars_by_range(dataframe):
    time_i = time.time()
    results = dataframe[
        (dataframe.Diam_pc >= 5) &
        (dataframe.Diam_pc <= 20) &
        (dataframe.age < 9)
    ].head(10)
    time_f = time.time()
    print('Total time Pandas to get stars by range = ', time_f - time_i)


def get_stars_by_combined_operators(dataframe):
    time_i = time.time()
    results = dataframe[
        (
            (dataframe.RA_ICRS >= 100) & (dataframe.RA_ICRS <= 200) & (dataframe.DE_ICRS > 0)
        ) |
        (
            (dataframe.RA_ICRS < 50) & (dataframe.DE_ICRS < -30)
        )
    ]
    time_f = time.time()
    print('Total time Pandas to get stars by combined operators = ', time_f - time_i)


def get_stars_by_aggregated_statistics(dataframe):
    time_i = time.time()

    bins = [-float('inf'), -0.5, 0, float('inf')]
    labels = ['Low (< -0.5)', 'Medium (-0.5 to 0)', 'High (>= 0)']
    dataframe['FeH_bin'] = pd.cut(dataframe['FeH'], bins=bins, labels=labels)

    stats = dataframe.groupby('FeH_bin')['Diam_pc'].agg(['mean', 'max']).reset_index()
    
    time_f = time.time()
    print('Total time Pandas to get aggregated statistics = ', time_f - time_i)


if __name__ == '__main__':
    dataframe = get_dataframe_from_csv()
    read_data_csv(dataframe)
    get_stars_by_query(dataframe)
    get_stars_filtered_sorted(dataframe)
    get_stars_by_range(dataframe)
    get_stars_by_combined_operators(dataframe)
    get_stars_by_aggregated_statistics(dataframe)
