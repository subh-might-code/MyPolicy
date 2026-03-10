#!/usr/bin/env python3
"""
Policy-to-Customer Stitching - Unified Identity Layer

What this achieves for MyPolicy:
- Unified Identity: Bridges data silos by stitching heterogeneous policy records
  to a central customer identity.
- Rule-Based Mapping: Uses PII combinations (PAN, Mobile+DOB) per project objectives.
- Security Compliance: Encrypts PII at rest in MongoDB (key success metric).
- Clean Data for UI: unified_portfolio collection ready for Unified Portfolio View.

Prerequisites: pip install pymongo cryptography
Run: python policy_stitching.py

Ensure standardized_output.json exists (run metadata_standardization.py first).
"""
import json
import os
import pathlib
from pymongo import MongoClient
from cryptography.fernet import Fernet

# ---------------------------------------------------------------------------
# 1. Database Setup (Atlas)
# ---------------------------------------------------------------------------
MONGODB_URI = os.getenv(
    "MONGODB_URI",
    "mongodb+srv://praticks2003_db_user:cPZqSJF3LPPsHSEv@cluster0.enwyvnr.mongodb.net/?appName=Cluster0",
)
DB_NAME = "Backend_databases"

# ---------------------------------------------------------------------------
# 2. Encryption Setup (Mandatory PII requirement)
# Key persisted so same records decrypt consistently across runs.
# In production, use a secrets manager / env var.
# ---------------------------------------------------------------------------
SCRIPT_DIR = pathlib.Path(__file__).parent
KEY_FILE = SCRIPT_DIR / ".encryption_key"
STANDARDIZED_FILE = SCRIPT_DIR / "standardized_output.json"


def get_or_create_cipher():
    """Load existing key or generate and persist new one."""
    if KEY_FILE.exists():
        key = KEY_FILE.read_bytes()
    else:
        key = Fernet.generate_key()
        KEY_FILE.write_bytes(key)
    return Fernet(key)


def encrypt_data(cipher_suite, data):
    """Encrypt PII for storage at rest."""
    if data is not None and data != "":
        return cipher_suite.encrypt(str(data).encode()).decode()
    return None


def find_customer_id(customers_col, policy: dict):
    """
    Rule-based matching: PAN (strongest), then Mobile+DOB.
    Returns (customerId, match_type) or (None, "NO_MATCH").
    """
    # Rule 1: Match by PAN (Strongest)
    pan = policy.get("pan")
    if pan:
        match = customers_col.find_one({"refCustItNum": pan})
        if match:
            return match.get("customerId"), "PAN_MATCH"

    # Rule 2: Match by Mobile + DOB (Secondary)
    mobile = policy.get("mobile")
    dob = policy.get("dob")
    if mobile is not None and dob is not None:
        match = customers_col.find_one({
            "refPhoneMobile": mobile,
            "datBirthCust": dob,
        })
        if match:
            return match.get("customerId"), "MOBILE_DOB_MATCH"

    return None, "NO_MATCH"


def main():
    # 1. Run metadata standardization if output missing
    if not STANDARDIZED_FILE.exists():
        print("Run metadata_standardization.py first to generate standardized_output.json")
        return

    client = MongoClient(MONGODB_URI, serverSelectionTimeoutMS=5000)
    db = client[DB_NAME]
    customers_col = db["customer_details"]
    unified_portfolio_col = db["unified_portfolio"]

    # 2. Load standardized data
    with open(STANDARDIZED_FILE, "r", encoding="utf-8") as f:
        standardized_data = json.load(f)

    data = standardized_data.get("data", {})
    policy_collections = ["life_insurance", "auto_insurance", "health_insurance"]

    # 3. Flatten all policies (exclude customer_details - those are the match targets)
    all_policies = []
    for coll_name in policy_collections:
        for rec in data.get(coll_name, []):
            rec["_source_collection"] = coll_name  # keep for reference
            all_policies.append(rec)

    print("=" * 60)
    print("Policy-to-Customer Stitching")
    print("=" * 60)
    print(f"Total policies to process: {len(all_policies)}")

    cipher_suite = get_or_create_cipher()

    # 4. Clear existing unified_portfolio for idempotent re-runs (optional)
    # Comment out if you want to accumulate.
    unified_portfolio_col.delete_many({})

    stitched_results = []
    match_counts = {"PAN_MATCH": 0, "MOBILE_DOB_MATCH": 0, "NO_MATCH": 0}

    for policy in all_policies:
        cust_id, match_type = find_customer_id(customers_col, policy)
        match_counts[match_type] = match_counts.get(match_type, 0) + 1

        if cust_id:
            unified_record = {
                "customerId": cust_id,
                "policy_id": policy.get("policy_id"),
                "insurer": policy.get("insurer"),
                "premium": policy.get("premium"),
                "sum_assured": policy.get("sum_assured"),
                "start_date": policy.get("start_date"),
                "policy_end": policy.get("policy_end"),
                "source_collection": policy.get("_source_collection", policy.get("source_collection")),
                "match_method": match_type,
                "encrypted_pan": encrypt_data(cipher_suite, policy.get("pan")),
                "encrypted_mobile": encrypt_data(cipher_suite, policy.get("mobile")),
            }
            stitched_results.append(unified_record)
            unified_portfolio_col.insert_one(unified_record)

    print(f"\nMatch results:")
    print(f"  PAN_MATCH:       {match_counts['PAN_MATCH']}")
    print(f"  MOBILE_DOB_MATCH: {match_counts['MOBILE_DOB_MATCH']}")
    print(f"  NO_MATCH:        {match_counts['NO_MATCH']}")

    print(f"\nSuccessfully stitched {len(stitched_results)} policies to customer identities.")
    print(f"Unified portfolio saved to: {DB_NAME}.unified_portfolio")

    if stitched_results:
        print("\nSample unified record:")
        sample = stitched_results[0].copy()
        # Don't print full encrypted values in sample
        if "encrypted_pan" in sample:
            sample["encrypted_pan"] = "<encrypted>"
        if "encrypted_mobile" in sample:
            sample["encrypted_mobile"] = "<encrypted>"
        print(json.dumps(sample, indent=2, default=str))

    print("=" * 60)


if __name__ == "__main__":
    main()
