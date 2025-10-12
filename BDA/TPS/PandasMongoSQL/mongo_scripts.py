import time
import pymongo

def get_mongo_connection():
    client = pymongo.MongoClient("mongodb://localhost:27017/")
    db = client["openClusters"]
    return db["cluster"]
    

def get_stars_above_50(collection):
    time_i = time.time()
    myquery_nested = {'position.RA_ICRS': {"$gt": 50}}
    results = collection.find(myquery_nested)
    time_f = time.time()
    print('Total time pymongo to get stars above 50 RA_ICRS = ', time_f - time_i)

def get_stars_by_query(collection):
    time_i = time.time()
    myquery_nested = {
        'name': {"$regex": "^A"}, 
        'position.RA_ICRS': {"$lt": 180},
        "$or": [
            {'position.DE_ICRS': {"$gt": 60}},
            {'position.DE_ICRS': {"$lt": -60}}
        ]
    }
    results = collection.find(myquery_nested)
    time_f = time.time()
    print('Total time pymongo to get stars by query = ', time_f - time_i)

def get_stars_filtered_sorted(collection):
    time_i = time.time()
    query = {"$and": [{'features.age': {"$gt": 4}}, {'features.FeH': {"$lt": 0}}]}

    results = collection.find(query).sort('position.RA_ICRS', pymongo.ASCENDING)
    time_f = time.time()
    print('Total time pymongo to get stars filtered and sorted = ', time_f - time_i)

def get_stars_by_range(collection):
    time_i = time.time()
    myquery_nested = {"$and": [
        {'features.Diam_pc': {"$gte": 5, "$lte": 20}},
        {'features.age': {"$lt": 9}},
    ]}
    results = collection.find(myquery_nested).limit(10)
    time_f = time.time()
    print('Total time pymongo to get stars by range = ', time_f - time_i)

def get_stars_by_combined_operators(collection):
    time_i = time.time()
    myquery_nested = {
        "$or": [
            {"$and": [
                {'position.RA_ICRS': {"$gte": 100, "$lte": 200}},
                {'position.DE_ICRS': {"$gt": 0}}
            ]},
            {"$and": [
                {'position.RA_ICRS': {"$lt": 50}},
                {'position.DE_ICRS': {"$lt": -30}}
            ]}
        ]
    }
    results = collection.find(myquery_nested)
    time_f = time.time()
    print('Total time pymongo to get stars by combined operators = ', time_f - time_i)

def get_stars_by_aggregated_statistics(collection):
    time_i = time.time()
    pipeline = [
        {
            "$match": {
                "features.FeH": {"$exists": True, "$ne": None},
                "features.Diam_pc": {"$exists": True, "$ne": None}
            }
        },
        {
            "$addFields": {
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
        {
            "$group": {
                "_id": "$feh_bin",
                "avg_diam_pc": {"$avg": "$features.Diam_pc"},
                "max_diam_pc": {"$max": "$features.Diam_pc"},
            }
        },
    ]
    results = list(collection.aggregate(pipeline))

    time_f = time.time()
    print('Total time pymongo to get Diam_pc stats by FeH bins = ', time_f - time_i)



if __name__ == '__main__':
    collection = get_mongo_connection()
    get_stars_above_50(collection)
    get_stars_by_query(collection)
    get_stars_filtered_sorted(collection)
    get_stars_by_range(collection)
    get_stars_by_combined_operators(collection)
    get_stars_by_aggregated_statistics(collection)