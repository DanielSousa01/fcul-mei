import mysql.connector as mysql
import numpy as np
import pandas as pd
import os


def get_dataframe_from_csv(file_path):
    """
    Reads a CSV file and returns a pandas DataFrame.

    :param file_path: Path to the CSV file.
    :return: DataFrame containing the CSV data.
    :raises FileNotFoundError: If the file does not exist.
    """

    if not os.path.exists(file_path):
        raise FileNotFoundError(f"The file {file_path} does not exist.")

    data = pd.read_csv(file_path)
    return data


def connect_to_database():
    """
    Establishes a connection to the MySQL database.
    :return: Database connection object or None if connection fails.
    """

    try:
        connection = mysql.connect(
            host='localhost',
            port=3306,
            user='root',
            password='password',
            database='mysql_db'
        )
        if connection.is_connected():
            print("Successfully connected to the database")
            return connection
    except mysql.Error as err:
        print(f"Error: {err}")
        return None


def map_sql_type(dataframe):
    """
    Maps pandas DataFrame dtypes to MySQL data types.

    :param dataframe: DataFrame whose dtypes need to be mapped.
    :return: A string representing the column name and its corresponding SQL data type.
    """

    type_mapping = {
        'int64': 'INT',
        'float64': 'FLOAT',
        'bool': 'BOOLEAN',
    }

    sql_columns = ""
    for column, dtype in dataframe.dtypes.items():
        sql_type = type_mapping.get(str(dtype), 'VARCHAR(255)')
        sql_columns += f",{column} {sql_type}"

    return sql_columns


def create_table(connection, dataframe):
    """
    Creates a table in the database based on the DataFrame structure.

    :param connection: Database connection object.
    :param dataframe: DataFrame containing the data structure.
    """

    print("Creating star_clusters table...")

    cursor = connection.cursor()

    drop_table_query = "DROP TABLE IF EXISTS star_clusters;"

    create_table_query = f"""
        CREATE TABLE star_clusters (
            id INT AUTO_INCREMENT PRIMARY KEY {map_sql_type(dataframe)} 
        );
    """

    try:
        cursor.execute(drop_table_query)
        cursor.execute(create_table_query)
        connection.commit()
    finally:
        cursor.close()

    print(f"{dataframe.columns.size + 1} columns created in the star_clusters table")


# TODO: Handle inf values properly ask
def get_values(dataframe, num_rows):
    """
    Extracts values from the DataFrame for insertion into the database.

    :param dataframe: DataFrame containing the data.
    :param num_rows: Number of rows to extract.
    :return: A list of values ready for insertion.
    """
    values = []

    for row in range(min(num_rows, len(dataframe))):
        current_row = dataframe.iloc[row]

        row_values = []
        for val in current_row.values:
            if pd.isna(val):
                row_values.append(None)
            elif hasattr(val, 'item'):
                row_values.append(val.item())
            else:
                row_values.append(val)

        values.append(tuple(row_values))
    return values


def insert_rows(connection, dataframe, num_rows=1):
    """
    Inserts rows into the star_clusters table.

    :param connection: Database connection object.
    :param dataframe: DataFrame containing the data to be inserted.
    :param num_rows: Number of rows to insert (default is 1).
    """

    if num_rows > len(dataframe):
        num_rows = len(dataframe)

    print(f"Inserting {num_rows} rows into star_clusters table...")
    cursor = connection.cursor()

    truncate_query = "TRUNCATE TABLE star_clusters;"

    insert_rows_query = f"""
        INSERT INTO star_clusters ({', '.join(dataframe.columns)})
        VALUES ({', '.join(['%s'] * len(dataframe.columns))});
    """

    rows_data = get_values(dataframe, num_rows)

    try:
        cursor.execute(truncate_query)
        cursor.executemany(insert_rows_query, rows_data)
        connection.commit()
    finally:
        cursor.close()

    print(f"{num_rows} rows inserted into star_clusters table")


def print_rows(connection):
    """
    Prints the rows where DiamMax_pc is greater than 10.

    :param connection: Database connection object.
    """

    print("Fetching rows where DiamMax_pc > 10...")
    cursor = connection.cursor()

    select_query = "SELECT * FROM star_clusters WHERE DiamMax_pc > 10;"

    try:
        cursor.execute(select_query)
        results = cursor.fetchall()
        for row in results:
            print(row)
    finally:
        cursor.close()


def print_specific_columns(connection):
    """
    Prints RA_ICRS, DE_ICRS, and Diam_pc where Plx is greater than 1.

    :param connection: Database connection object.
    """

    print("Fetching specific rows where Plx > 1...")
    cursor = connection.cursor()

    select_query = "SELECT RA_ICRS, DE_ICRS, Diam_pc FROM star_clusters WHERE Plx > 1;"

    try:
        cursor.execute(select_query)
        results = cursor.fetchall()
        for row in results:
            print(row)
    finally:
        cursor.close()


def update_age(connection, name, new_age):
    """
    Updates the age of a star cluster based on its name.

    :param connection: Database connection object.
    :param name: Name of the star cluster to update.
    :param new_age: New age value to set.
    """

    print("Updating age of the star cluster...")
    cursor = connection.cursor()

    update_query = "UPDATE star_clusters SET age = %s WHERE Name = %s;"

    params = (new_age, name)

    try:
        cursor.execute(update_query, params)
        connection.commit()
        print(f"Rows affected: {cursor.rowcount}")
    finally:
        cursor.close()


def delete_row(connection, name):
    """
    Deletes a star cluster based on its name.

    :param connection: Database connection object.
    :param name: Name of the star cluster to delete.
    """

    print("Deleting the star cluster...")
    cursor = connection.cursor()

    delete_query = "DELETE FROM star_clusters WHERE name = %s;"

    params = (name,)

    try:
        cursor.execute(delete_query, params)
        connection.commit()
        print(f"Rows affected: {cursor.rowcount}")
    finally:
        cursor.close()


def find_row(connection, name):
    """
    Finds a star cluster based on its name and prints its name and dist_PLX.

    :param connection: Database connection object.
    :param name: Name of the star cluster to find.
    """

    print("Finding the star cluster...")
    cursor = connection.cursor()

    name += '%'
    find_query = "SELECT name, dist_PLX FROM star_clusters WHERE name LIKE %s;"

    params = (name,)

    try:
        cursor.execute(find_query, params)
        results = cursor.fetchall()
        for row in results:
            print(row)
        print(f"Rows found: {cursor.rowcount}")
    finally:
        cursor.close()


def count_rows(connection):
    """
    Counts the total number of rows in the star_clusters table that have FeH less than 0.

    :param connection: Database connection object.
    :return: Total number of rows in the star_clusters table.
    """

    cursor = connection.cursor()

    count_query = "SELECT COUNT(*) FROM star_clusters WHERE FeH < 0;"

    try:
        cursor.execute(count_query)
        result = cursor.fetchone()
        return result[0] if result else 0
    finally:
        cursor.close()


if __name__ == '__main__':
    dataFrame = get_dataframe_from_csv('dias_catalogue.csv')
    db_connection = connect_to_database()

    if db_connection:
        print("\n--- Creating table ---")
        create_table(db_connection, dataFrame)

        print("\n--- Inserting rows ---")
        insert_rows(db_connection, dataFrame, 600)

        print("\n--- Printing rows where DiamMax_pc > 10 ---")
        print_rows(db_connection)

        print("\n--- Printing RA_ICRS, DE_ICRS, Diam_pc where Plx > 1 ---")
        print_specific_columns(db_connection)

        print("\n--- Updating age of a star cluster ---")
        update_age(db_connection, 'ASCC_10', 7.000)

        print("\n--- Deleting a star cluster ---")
        delete_row(db_connection, 'ASCC_10')

        print("\n--- Finding a star cluster ---")
        find_row(db_connection, 'FSR')

        print("\n--- Counting rows where FeH < 0 ---")
        total_rows = count_rows(db_connection)
        print(f"Total rows with FeH < 0: {total_rows}")
        
        print("\n--- Closing connection ---")
        db_connection.close()
        print("Database connection closed")
