import pandas as pd
import mysql.connector as mysql


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


def prinf_specific_columns(connection, columns):
    """
    Prints specific columns from the 'star_clusters' table.

    :param connection: Database connection object.
    :param columns: List of column names to retrieve.
    """
    cursor = connection.cursor()
    query = f"SELECT {', '.join(columns)} FROM star_clusters"
    dataframe = pd.read_sql(query, con=connection)

    print(dataframe)


if __name__ == "__main__":
    db_connection = connect_to_database()

    if db_connection:
        # dataframe = pd.read_sql_table(table_name='star_clusters', con=db_connection)
        try:
            print("\n--- Printing name, RA_ICRS, DE_ICRS, Vr, and Plx ---")
            prinf_specific_columns(db_connection, ['name', 'RA_ICRS', 'DE_ICRS', 'Vr', 'Plx'])
        finally:
            print("\n--- Closing connection ---")
            db_connection.close()
            print("Database connection closed")
