import pymongo
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
    
def get_mongo_collection():
    client = pymongo.MongoClient("mongodb://localhost:27017/")
    db = client["openClusters"]
    return db["cluster"]

def mysql_get_clusters(connection):
    cursor = connection.cursor()

    time_ip = time.time()

    cursor.execute("""
        SELECT 
            CASE 
                WHEN FeH < -0.5 THEN 'Low (< -0.5)'
                WHEN FeH >= -0.5 AND FeH < 0 THEN 'Medium (-0.5 to 0)'
                ELSE 'High (>= 0)'
            END AS feh_bin,
            AVG(age) AS average_age,
            MAX(DE_ICRS) AS max_de_icrs,
            COUNT(*) AS count
        FROM clusters
        WHERE FeH < 0 AND Diam_pc > 10
        GROUP BY
            CASE 
                WHEN FeH < -0.5 THEN 'Low (< -0.5)'
                WHEN FeH >= -0.5 AND FeH < 0 THEN 'Medium (-0.5 to 0)'
                ELSE 'High (>= 0)'
            END
        ORDER BY average_age DESC
""")
    results = cursor.fetchall()

    time_fp = time.time()

    print('Total time MySQL to get stars above 50 RA_ICRS = ', time_fp - time_ip)
    for row in results:
        print(row)
    cursor.close()


def mongo_get_clusters(collection):
    time_i = time.time()
    pipeline = [
        {"$match": {
            "$and": [
                {"features.FeH": {"$lt": 0}},
                {"features.Diam_pc": {"$gt": 10}}
            ]
        }},
        {"$addFields": { 
                "feh_bin": {
                    "$switch": {
                        "branches": [
                            {"case": {"$lt": ["$features.FeH", -0.5]}, "then": "Low (< -0.5)"},
                            {"case": {"$and": [{"$gte": ["$features.FeH", -0.5]}, {"$lt": ["$features.FeH", 0]}]}, "then": "Medium (-0.5 to 0)"},
                            {"case": {"$gte": ["$features.FeH", 0]}, "then": "High (>= 0)"}
                        ],
                        "default": "Unknown"
                    }
                }
            }
        },
        {"$group": {
            "_id": "$feh_bin",
            "average_age": {"$avg": "$features.age"},
            "max_de_icrs": {"$max": "$position.DE_ICRS"},
            "count": {"$sum": 1}
        }},
        {"$sort": {"average_age": -1}}
    ]
    results = collection.aggregate(pipeline)
    time_f = time.time()
    print('Total time pymongo to get stars by aggregated statistics = ', time_f - time_i)
    for doc in results:
        print(doc)


if __name__ == '__main__':
    collection = get_mongo_collection()
    connection = get_mysql_connection()

    mysql_get_clusters(connection)
    mongo_get_clusters(collection)