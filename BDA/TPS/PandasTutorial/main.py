import pandas as pd
import os
import numpy as np
from typing import List


def process_command(command: str, data: pd.DataFrame) -> None:
    """
    Processes the command to either print the data or show table info.

    :param command: The command to process ('print' or 'info').
    :param data: pandas DataFrame containing the data.
    :return: None
    """

    match command:
        case 'print':
            print_data(data)
        case 'info':
            print_table_info(data)
        case 'missing':
            print_missing_data_summary(data)
        case 'columns':
            cols = input("Enter column names separated by commas: ").strip().split(',')
            cols = [col.strip() for col in cols]
            limit_input = input("Enter number of rows to display (default 5): ").strip()
            limit = int(limit_input) if limit_input.isdigit() else 5
            print_columns(data, cols, limit)
        case 'filter_plx':
            print_obj_with_positive_plx(data)
        case 'sort_age':
            limit_input = input("Enter number of rows to display (default 5): ").strip
            limit = int(limit_input) if limit_input.isdigit() else 5
            sort_by_age(data, limit)
        case 'stats':
            col_name = input("Enter the column name for statistical summary: ").strip()
            statistical_summary(data, col_name)
        case 'group_pm':
            group_by_PM(data)
        case 'distmod':
            create_distmod(data)
        case 'save':
            save_data(data)
        case _:
            print("Invalid command. Please enter 'print', 'info', 'missing', 'columns', 'filter_plx', 'sort_age', 'stats', 'group_pm', 'distmod', 'save', or 'exit'.")


def read_data(file_path: str) -> pd.DataFrame:
    """
    Reads data from a CSV file and returns it as a pandas DataFrame.

    :param file_path: Path to the CSV file.
    :return: pandas DataFrame containing the data from the CSV file.
    """

    if not os.path.exists(file_path):
        raise FileNotFoundError(f"The file {file_path} does not exist.")

    data = pd.read_csv(file_path)
    return data


def print_data(data: pd.DataFrame) -> None:
    """
    Prints the entire DataFrame.

    :param data: pandas DataFrame to be printed.
    :return: None
    """

    print(data)


def print_table_info(data: pd.DataFrame) -> None:
    """
    Prints information about the DataFrame including data types and missing values.

    :param data: pandas DataFrame to analyze.
    :return: None
    """

    print(data.info())


def print_missing_data_summary(data: pd.DataFrame) -> None:
    """
    Prints a summary of missing data in the DataFrame.

    :param data: pandas DataFrame to analyze.
    :return: None
    """

    total_cells = data.size

    missing_values = data.isnull().sum()
    missing_percent = (missing_values / total_cells) * 100
    missing_df = pd.DataFrame({'Missing Values': missing_values, 'Percentage': missing_percent})
    print("\nMissing Values and Percentage:\n", missing_df)

    total_missing = missing_values.sum()
    missing_percentage = (total_missing / total_cells) * 100

    print(f"Total Missing Values: {total_missing}")
    print(f"Total Cells: {total_cells}")
    print(f"Percentage of Missing Values: {missing_percentage:.2f}%")


def print_columns(data: pd.DataFrame, columns_names: List[str], limit: int = 5) -> None:
    """
    Prints a specific column or columns from the DataFrame.

    :param data: pandas DataFrame to analyze.
    :param columns_names: List of column names to print.
    :param limit: Number of rows to print from the specified columns.
    :return: None
    """

    print(f"Requested columns: {columns_names}")

    if not all(col in data.columns for col in columns_names):
        print("One or more specified columns do not exist in the DataFrame.")
        return

    print(data[columns_names].head(limit))


def print_obj_with_positive_plx(data: pd.DataFrame) -> None:
    """
    Filters the DataFrame based on a specific column and value.

    :param data: pandas DataFrame to filter.
    :return: Filtered pandas DataFrame.
    """

    filtered_data = data[data['Plx'] > 1]

    print(filtered_data[['name', 'Plx', 'dist_PLX']])


def sort_by_age(data: pd.DataFrame, limit: int = 5) -> None:
    """
    Sorts the DataFrame by the 'Age' column in ascending order.

    :param data: pandas DataFrame to sort.
    :param limit: Number of rows to return from the sorted DataFrame.
    :return: None
    """

    sorted_data = data.sort_values(by='age', ascending=True)

    print(sorted_data.head(limit))


def statistical_summary(data: pd.DataFrame, column_name: str) -> None:
    """
    Prints statistical summary of numerical columns in the DataFrame.

    :param data: pandas DataFrame to analyze.
    :param column_name: Column name to analyze.
    :return: None
    """

    if column_name not in data.columns:
        print(f"Column '{column_name}' does not exist in the DataFrame.")
        return

    scope = data[[column_name]]
    mean = scope.mean()
    median = scope.median()
    standard_deviation = scope.std()

    print("Statistical Summary:")
    print(f"Mean:\n{mean}\n")
    print(f"Median:\n{median}\n")
    print(f"Standard Deviation:\n{standard_deviation}\n")


def group_by_PM(data: pd.DataFrame) -> None:
    """
    Groups the DataFrame by the 'flagdispPM' column and prints the grouped data.

    :param data: pandas DataFrame to group.
    :return: None
    """

    grouped_data = data.groupby('flagdispPM')
    average_pm = grouped_data['sigPM'].mean()

    print("Average 'sigPM' grouped by 'flagdispPM':")
    print(average_pm)


def create_distmod(data: pd.DataFrame) -> None:
    """
    Creates a new column 'DistMod' that estimates the distance modulus using 'dist_PLX'.

    :param data: pandas DataFrame to modify.
    :return: None
    """

    data['DistMod'] = 5 * np.log10(data['dist_PLX']) - 5
    print("New column 'DistMod' added to the DataFrame.")
    print(data[['name', 'dist_PLX', 'DistMod']].head())


def save_data(data: pd.DataFrame) -> None:
    """
    Saves the DataFrame to a CSV file with specific columns.

    :param data: pandas DataFrame to save.
    :return: None
    """

    data.to_csv('subset.csv', columns=['RA_ICRS', 'DE_ICRS', 'dist_PLX', 'Diam_pc'], index=False)
    print(f"Data saved.")


if __name__ == '__main__':
    print("Reading data from 'dias_catalogue.csv'...")
    data = read_data('dias_catalogue.csv')
    print("Data read successfully.\n")

    while 1:
        command = input("Enter command: ").strip().lower()
        if command == 'exit':
            print("Exiting the program.")
            break
        else:
            process_command(command, data)
