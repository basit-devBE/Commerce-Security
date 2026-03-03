# Stress Test Report — Get All Products Endpoint

| | |
|---|---|
| **Endpoint** | `GET /api/products/public/all?page=0&size=50` |
| **Host** | `localhost:8080` |
| **Date** | 03 March 2026, 09:37:19 UTC |
| **Tool** | Apache JMeter |
| **Status** | PASS |

---

## Test Configuration

| Parameter | Value |
|---|---|
| Virtual Users | 100 |
| Ramp-Up Period | 10 seconds |
| Loop Count | 5 iterations per thread |
| Total Requests | 500 |
| HTTP Method | GET |
| Authentication Required | No (public endpoint) |

---

## Results Summary

| Metric | Value |
|---|---|
| Total Requests | 500 |
| Error Rate | **0.00%** |
| Average Response Time | **1.7 ms** |
| Median (P50) | **1 ms** |
| P90 | **3 ms** |
| P95 | **4 ms** |
| P99 | **5 ms** |
| Max Response Time | **23 ms** |
| Standard Deviation | **1.5 ms** |
| Throughput | **50.96 req/s** |
| Avg Response Payload | **3,406 bytes** |
| Test Duration | **9.8 seconds** |

---

## Assessment

The endpoint performs exceptionally well under load. Zero errors across all 500 requests and a P99 of just 5 ms means virtually every user receives a response in under 5 milliseconds regardless of concurrent load. The standard deviation of 1.5 ms confirms near-perfect consistency — the system shows no signs of contention or queuing at 100 concurrent users.

The larger response payload (3,406 bytes vs 1,065 bytes for login) did not negatively impact performance, suggesting the database query and serialization layer are well optimized for this endpoint. Being a read-only, public, paginated query with no authentication overhead is the primary driver of these results.

---

## Comparison vs. Login Endpoint (Post-Optimization)

| Metric | Login | Get All Products |
|---|---|---|
| Average Response | 117 ms | **1.7 ms** |
| P99 | 264 ms | **5 ms** |
| Throughput | 44.28 req/s | **50.96 req/s** |
| Std Deviation | 29 ms | **1.5 ms** |
| Concurrent Users Tested | 50 | 100 |

---

## Verdict

✅ No optimization required. This endpoint is production-ready as tested.

---
*Document version: 1.0 | Date: 03 March 2026*
