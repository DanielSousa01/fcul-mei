import pandas as pd
import pymongo
import json
import numpy as np
from sqlalchemy import create_engine, text

def generate_json():
    df = pd.read_csv('dias_catalogue.csv')

    df['position'] = df[['RA_ICRS', 'DE_ICRS', 'Plx', 'dist_PLX']].apply(
        lambda s: s.to_dict(), axis=1
    )

    df['features'] = df[['r50', 'Vr', 'age', 'FeH', 'Diam_pc']].apply(
        lambda s: s.to_dict(), axis=1
    )

    df[['name', 'position', 'features']].to_json("dias_catalogue_filtered.json",
                                                 orient="records", date_format="epoch",
                                                 double_precision=10, force_ascii=True, date_unit="ms",
                                                 default_handler=None, indent=2)


def create_mongodb():
    client = pymongo.MongoClient("mongodb://localhost:27017/")
    db = client["openClusters"]
    collection = db["cluster"]

    with open("dias_catalogue_filtered.json", "r") as f:
        stars_data = json.load(f)

    collection.drop()

    result = collection.insert_many(stars_data)

    print("Inserted documents:", len(result.inserted_ids))
    print("Total in collection:", collection.count_documents({}))


def create_mysql_database():
    df = pd.read_csv("dias_catalogue.csv")
    df = df.replace('', None)
    df.replace([np.inf, -np.inf], np.nan, inplace=True)

    engine = create_engine("mysql+mysqlconnector://root:password@localhost:3306/")

    with engine.connect() as conn:
        conn.execute(text("CREATE DATABASE IF NOT EXISTS openclusters"))
        conn.commit()

    engine = create_engine("mysql+mysqlconnector://root:password@localhost:3306/openclusters")

    df.to_sql(name='clusters', con=engine, if_exists='replace', index=False)


def read_from_csv():
    df = pd.read_csv('dias_catalogue.csv')
    df['name'] = df['name'].str.strip()
    df = df[['name', 'RA_ICRS', 'DE_ICRS', 'Plx', 'dist_PLX', 'Vr', 'age', 'FeH', 'Diam_pc', 'r50']]

    print(df.head())


if __name__ == '__main__':
    generate_json()
    create_mongodb()
    create_mysql_database()
    read_from_csv()
