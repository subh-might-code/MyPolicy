#!/usr/bin/env python3
"""
Coverage Insights & Protection Gap Analysis - Advisory-Led UX

Transitions the system from a data repository to an "Advisory-Led UX" by:
- Product Presence Gap: Identifies missing insurance categories (Life, Health, Auto)
- Sum Insured Adequacy: Compares coverage vs industry benchmarks (e.g. 10x premium for Life)
- Temporal Gaps: Flags policies nearing expiry or already lapsed

Delivers: Unified Portfolio View, Coverage Insights, Human-Readable Advisory

Prerequisites: pip install pymongo
Run: python coverage_advisory.py [customer_id]   # e.g. 901120934
     python coverage_advisory.py --all           # all customers
     python coverage_advisory.py --demo          # insert demo policies and show full advisory
"""
import json
import os
import sys
from datetime import datetime
from pymongo import MongoClient

MONGODB_URI = os.getenv(
    "MONGODB_URI",
    "mongodb+srv://praticks2003_db_user:cPZqSJF3LPPsHSEv@cluster0.enwyvnr.mongodb.net/?appName=Cluster0",
)
DB_NAME = "Backend_databases"

# Advisory thresholds
REQUIRED_CATEGORIES = ["life_insurance", "health_insurance", "auto_insurance"]
LIFE_SUM_ASSURED_MULTIPLIER = 10  # Sum Assured should be >= premium * 10
HEALTH_COVERAGE_MIN = 300000  # Minimum recommended health coverage (INR)
AUTO_IDV_MIN = 100000  # Minimum recommended IDV for auto (INR)
DAYS_NEARING_EXPIRY = 90  # Flag if policy expires within 90 days


def parse_yyyymmdd(value):
    """Parse YYYYMMDD int to date, or return None."""
    if value is None:
        return None
    try:
        s = str(int(value))
        if len(s) != 8:
            return None
        return datetime(int(s[:4]), int(s[4:6]), int(s[6:8]))
    except (ValueError, TypeError):
        return None


def generate_coverage_advisory(db, customer_id: int) -> dict:
    """
    Generates a human-readable advisory for a customer based on their
    unified portfolio. Identifies Protection Gaps, Sum Assured adequacy,
    and Temporal gaps.
    """
    policies = list(db["unified_portfolio"].find({"customerId": customer_id}))
    categories = {p.get("source_collection") for p in policies if p.get("source_collection")}
    advisory_notes = []
    today = datetime.now().date()

    # 1. Product Presence Gap
    for cat in REQUIRED_CATEGORIES:
        if cat not in categories:
            label = cat.replace("_", " ").title()
            advisory_notes.append({
                "type": "PROTECTION_GAP",
                "severity": "high",
                "message": f"Protection Gap: You currently have no {label} coverage.",
            })

    # 2. Sum Insured Adequacy & 3. Temporal Gaps
    for p in policies:
        source = p.get("source_collection")
        premium = p.get("premium") or 0
        sum_assured = p.get("sum_assured") or 0
        policy_end_val = p.get("policy_end")
        policy_end_date = parse_yyyymmdd(policy_end_val)

        # Life Insurance: Sum Assured should be ~10x annual premium
        if source == "life_insurance" and premium:
            if sum_assured < (premium * LIFE_SUM_ASSURED_MULTIPLIER):
                advisory_notes.append({
                    "type": "SUM_ASSURED_ADEQUACY",
                    "severity": "medium",
                    "message": (
                        f"Advisory: Your Life Insurance coverage (₹{sum_assured:,}) may be low "
                        f"relative to your premium (₹{premium:,}). Consider coverage at least "
                        f"{LIFE_SUM_ASSURED_MULTIPLIER}x your annual premium."
                    ),
                    "policy_id": p.get("policy_id"),
                })

        # Health Insurance: Minimum coverage threshold
        if source == "health_insurance" and sum_assured and sum_assured < HEALTH_COVERAGE_MIN:
            advisory_notes.append({
                "type": "SUM_ASSURED_ADEQUACY",
                "severity": "medium",
                "message": (
                    f"Advisory: Your Health Insurance coverage (₹{sum_assured:,}) is below the "
                    f"recommended minimum of ₹{HEALTH_COVERAGE_MIN:,} for adequate protection."
                ),
                "policy_id": p.get("policy_id"),
            })

        # Auto Insurance: IDV adequacy
        if source == "auto_insurance" and sum_assured and sum_assured < AUTO_IDV_MIN:
            advisory_notes.append({
                "type": "SUM_ASSURED_ADEQUACY",
                "severity": "low",
                "message": (
                    f"Advisory: Your Auto Insurance IDV (₹{sum_assured:,}) may be lower than "
                    f"the recommended ₹{AUTO_IDV_MIN:,}."
                ),
                "policy_id": p.get("policy_id"),
            })

        # 3. Temporal Gaps
        if policy_end_date:
            end_date = policy_end_date.date()
            days_remaining = (end_date - today).days

            if days_remaining < 0:
                advisory_notes.append({
                    "type": "TEMPORAL_GAP",
                    "severity": "high",
                    "message": (
                        f"Lapsed: Policy {p.get('policy_id', 'N/A')} ({source.replace('_', ' ').title()}) "
                        f"expired on {end_date.strftime('%Y-%m-%d')}. Renew to maintain coverage."
                    ),
                    "policy_id": p.get("policy_id"),
                })
            elif days_remaining <= DAYS_NEARING_EXPIRY:
                advisory_notes.append({
                    "type": "TEMPORAL_GAP",
                    "severity": "medium",
                    "message": (
                        f"Expiring soon: Policy {p.get('policy_id', 'N/A')} ({source.replace('_', ' ').title()}) "
                        f"expires in {days_remaining} days ({end_date.strftime('%Y-%m-%d')}). "
                        "Consider renewing before lapse."
                    ),
                    "policy_id": p.get("policy_id"),
                })

    # Sanitize policies for output (exclude encrypted PII)
    unified_view = []
    for p in policies:
        safe = {k: v for k, v in p.items() if k not in ("encrypted_pan", "encrypted_mobile", "_id")}
        safe["_id"] = str(p.get("_id", ""))
        unified_view.append(safe)

    return {
        "customerId": customer_id,
        "advisory": advisory_notes,
        "summary": {
            "total_policies": len(policies),
            "categories_present": list(categories),
            "gaps_identified": len(advisory_notes),
        },
        "unified_view": unified_view,
    }


def insert_demo_records(db, customer_id: int):
    """
    Inserts sample stitched records for demonstration when no matches exist.
    Uses realistic data to showcase all advisory types (Product Gap, Sum Assured, Temporal).
    """
    from datetime import timedelta
    today = datetime.now().date()
    soon = (today + timedelta(days=45)).strftime("%Y%m%d")
    lapsed = (today - timedelta(days=30)).strftime("%Y%m%d")
    far = (today + timedelta(days=400)).strftime("%Y%m%d")

    demo_policies = [
        # Life: low sum assured (5000 < 10 * 50000)
        {"customerId": customer_id, "policy_id": "LIPOL-DEMO1", "insurer": "SBI Life", "premium": 50000,
         "sum_assured": 5000, "start_date": 20220101, "policy_end": int(far), "source_collection": "life_insurance"},
        # Health: below 300k
        {"customerId": customer_id, "policy_id": "HEPOL-DEMO1", "insurer": "HDFC Life", "premium": 15000,
         "sum_assured": 100000, "start_date": 20230101, "policy_end": int(far), "source_collection": "health_insurance"},
        # Auto: expiring soon
        {"customerId": customer_id, "policy_id": "AUPOL-DEMO1", "insurer": "Tata AIG", "premium": 8000,
         "sum_assured": 250000, "start_date": 20240101, "policy_end": int(soon), "source_collection": "auto_insurance"},
        # Life: lapsed
        {"customerId": customer_id, "policy_id": "LIPOL-DEMO2", "insurer": "ICICI", "premium": 12000,
         "sum_assured": 200000, "start_date": 20200101, "policy_end": int(lapsed), "source_collection": "life_insurance"},
    ]
    db["unified_portfolio"].insert_many(demo_policies)
    print(f"Inserted {len(demo_policies)} demo policies for customer {customer_id}")


def main():
    client = MongoClient(MONGODB_URI, serverSelectionTimeoutMS=5000)
    db = client[DB_NAME]

    if len(sys.argv) > 1 and sys.argv[1] == "--demo":
        # Insert demo records and run advisory for Amit Kulkarni
        customer_id = 901120934
        db["unified_portfolio"].delete_many({"customerId": customer_id})
        insert_demo_records(db, customer_id)
        result = generate_coverage_advisory(db, customer_id)
        print("=" * 60)
        print(f"Coverage Advisory (DEMO) - Customer {customer_id}")
        print("=" * 60)
        print(json.dumps(result, indent=2, default=str))
        return

    if len(sys.argv) > 1 and sys.argv[1] == "--all":
        # Generate advisory for all customers in customer_details
        customers = list(db["customer_details"].find({}, {"customerId": 1}))
        customer_ids = [c["customerId"] for c in customers if c.get("customerId") is not None]
        print("=" * 60)
        print("Coverage Advisory - All Customers")
        print("=" * 60)
        results = []
        for cid in customer_ids:
            result = generate_coverage_advisory(db, cid)
            results.append(result)
            print(f"\n--- Customer {cid} ---")
            print(f"Policies: {result['summary']['total_policies']} | Gaps: {result['summary']['gaps_identified']}")
            for note in result["advisory"][:3]:  # First 3 advisories
                msg = note["message"]
                print(f"  [{note['type']}] {msg[:80]}{'...' if len(msg) > 80 else ''}")
        # Save to file
        out_path = os.path.join(os.path.dirname(__file__), "advisory_output.json")
        with open(out_path, "w", encoding="utf-8") as f:
            json.dump(results, f, indent=2, default=str)
        print(f"\nFull output saved to: {out_path}")
    else:
        # Single customer (default: Amit Kulkarni 901120934)
        customer_id = int(sys.argv[1]) if len(sys.argv) > 1 else 901120934
        result = generate_coverage_advisory(db, customer_id)
        print("=" * 60)
        print(f"Coverage Advisory - Customer {customer_id}")
        print("=" * 60)
        print(json.dumps(result, indent=2, default=str))


if __name__ == "__main__":
    main()
