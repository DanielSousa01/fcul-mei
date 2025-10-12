import time
import mysql.connector as mysql

def get_mysql_connection():
    try:
        return mysql.connect(
            host='localhost',
            port=3306,
            user='root',
            password='password',
            database='openclusters'
        )
    except mysql.Error as err:
        print(f"Error connecting to MySQL: {err}")
        return None
    
def get_stars_above_50(connection):
    cursor = connection.cursor()

    time_ip = time.time()

    cursor.execute("SELECT * FROM clusters WHERE RA_ICRS > 50")
    results = cursor.fetchall()

    time_fp = time.time()

    print('Total time MySQL to get stars above 50 RA_ICRS = ', time_fp - time_ip)
    cursor.close()

def get_stars_by_query(connection):
    cursor = connection.cursor()

    time_i = time.time()

    cursor.execute("""
        SELECT * FROM clusters 
        WHERE name LIKE 'A%' 
        AND RA_ICRS < 180 
        AND (DE_ICRS > 60 OR DE_ICRS < -60)
    """)
    results = cursor.fetchall()
    
    time_f = time.time()

    print('Total time MySQL to get stars by query = ', time_f - time_i)
    cursor.close()

def get_stars_filtered_sorted(connection):
    cursor = connection.cursor()

    time_i = time.time()

    cursor.execute("""
        SELECT * FROM clusters 
        WHERE age > 4 AND FeH < 0
        ORDER BY RA_ICRS ASC
    """)
    results = cursor.fetchall()
    
    time_f = time.time()

    print('Total time MySQL to get stars filtered and sorted = ', time_f - time_i)
    cursor.close()


def get_stars_by_range(connection):
    cursor = connection.cursor()

    time_i = time.time()

    cursor.execute("""
        SELECT * FROM clusters 
        WHERE Diam_pc BETWEEN 5 AND 20
        AND age < 9
        LIMIT 10
    """)
    results = cursor.fetchall()
    
    time_f = time.time()

    print('Total time MySQL to get stars by range = ', time_f - time_i)
    cursor.close()


def get_stars_by_combined_operators(connection):
    cursor = connection.cursor()
    
    time_i = time.time()
    
    cursor.execute("""
        SELECT * FROM clusters 
        WHERE (RA_ICRS BETWEEN 100 AND 200 AND DE_ICRS > 0)
        OR (RA_ICRS < 50 AND DE_ICRS < -30)
    """)
    results = cursor.fetchall()
    
    time_f = time.time()
    
    print('Total time MySQL to get stars by combined operators = ', time_f - time_i)
    cursor.close()

def get_stars_by_aggregated_statistics(collection):
    cursor = collection.cursor()

    time_i = time.time()

    cursor.execute("""
        SELECT 
            CASE 
                WHEN FeH < -0.5 THEN 'Low (< -0.5)'
                WHEN FeH >= -0.5 AND FeH < 0 THEN 'Medium (-0.5 to 0)'
                ELSE 'High (>= 0)'
            END AS feh_bin,
            AVG(Diam_pc) AS average_diam_pc,
            MAX(Diam_pc) AS max_diam_pc
        FROM clusters
        GROUP BY
            CASE 
                WHEN FeH < -0.5 THEN 'Low (< -0.5)'
                WHEN FeH >= -0.5 AND FeH < 0 THEN 'Medium (-0.5 to 0)'
                ELSE 'High (>= 0)'
            END
    """)
    stats = cursor.fetchall()

    time_f = time.time()

    print('Total time MySQL to get aggregated statistics = ', time_f - time_i)
    cursor.close()


if __name__ == '__main__':
    connection = get_mysql_connection()

    get_stars_above_50(connection)
    get_stars_by_query(connection)
    get_stars_filtered_sorted(connection)
    get_stars_by_range(connection)
    get_stars_by_combined_operators(connection)
    get_stars_by_aggregated_statistics(connection)
