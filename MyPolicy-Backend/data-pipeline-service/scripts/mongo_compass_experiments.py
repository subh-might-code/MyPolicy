#!/usr/bin/env python3
"""
MongoDB Compass Experiments - Standalone script (separate from data-pipeline-service)
Connects to your MongoDB Atlas cluster and runs some experiments.

Prerequisites: pip install pymongo

Run: python mongo_compass_experiments.py

Or set MONGODB_URI env var if you want to use a different connection string.
"""
import json
import os
import sys
from datetime import datetime
from bson import json_util

# Default: use your Atlas connection string
# Replace with your actual password if different
MONGODB_URI = os.getenv(
    "MONGODB_URI",
    "mongodb+srv://praticks2003_db_user:cPZqSJF3LPPsHSEv@cluster0.enwyvnr.mongodb.net/?appName=Cluster0"
)

def main():
    try:
        from pymongo import MongoClient
    except ImportError:
        print("Install pymongo first: pip install pymongo")
        sys.exit(1)

    print("=" * 50)
    print("MongoDB Atlas Experiments (standalone)")
    print("=" * 50)

    client = MongoClient(MONGODB_URI, serverSelectionTimeoutMS=5000)

    # Experiment 1: List databases
    print("\n[1] Listing databases...")
    dbs = client.list_database_names()
    print(f"    Found: {dbs}")

    # Experiment 2: Use or create experiment DB
    db = client["experiment_db"]
    print(f"\n[2] Using database: experiment_db")

    # Experiment 3: Create a collection and insert document
    col = db["test_collection"]
    doc = {
        "message": "Hello from Python experiment!",
        "timestamp": datetime.utcnow().isoformat(),
        "source": "mongo_compass_experiments.py",
    }
    result = col.insert_one(doc)
    print(f"\n[3] Inserted document, id: {result.inserted_id}")

    # Experiment 4: Query it back
    found = col.find_one({"_id": result.inserted_id})
    print(f"\n[4] Read back: {found}")

    # Experiment 5: Count documents
    count = col.count_documents({})
    print(f"\n[5] Total documents in test_collection: {count}")

    # Experiment 6: List collections in experiment_db
    colls = db.list_collection_names()
    print(f"\n[6] Collections in experiment_db: {colls}")

    # Display life_insurance collection from Backend_databases
    print("\n" + "=" * 70)
    print("life_insurance collection (Backend_databases)")
    print("=" * 70)
    backend_db = client["Backend_databases"]
    life_col = backend_db["life_insurance"]
    count = life_col.count_documents({})
    print(f"\nTotal documents: {count}\n")
    for i, doc in enumerate(life_col.find({}), 1):
        print(f"--- Document {i} ---")
        print(json.dumps(json_util.loads(json_util.dumps(doc)), indent=2, default=str))
        print()
    print("=" * 70)


if __name__ == "__main__":
    main()
