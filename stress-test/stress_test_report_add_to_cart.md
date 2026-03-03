# Stress Test Report — Add Item to Cart Endpoint

| | |
|---|---|
| **Endpoint** | `POST /api/cart/add` |
| **Host** | `localhost:8080` |
| **Date** | 03 March 2026 |
| **Tool** | Apache JMeter |
| **Status** | PASS |
| **Authentication** | Bearer JWT (extracted from Login) |

---

## Test Configuration

| Parameter | Value |
|---|---|
| Virtual Users | 50 |
| Ramp-Up Period | 10 seconds |
| Loop Count | 10 iterations per thread |
| Total Requests | 500 |
| HTTP Method | POST |
| Content-Type | application/json |
| Authentication | Required — Bearer `${authToken}` |

**Token Flow:** Login thread group (1 thread, 1 loop) runs first, extracts JWT via JSON Extractor (`$.data.token`), saves to global props via JSR223 PostProcessor. Add Item to Cart reads token from props via JSR223 PreProcessor and attaches it as `Authorization: Bearer ${authToken}` header.

**Request Body:**
```json
{
  "productId": 1,
  "quantity": 1
}
```

---

## Results Summary

| Metric | Value |
|---|---|
| Total Requests | 500 |
| Error Rate | **0.00%** |
| Average Response Time | **6.1 ms** |
| Median (P50) | **5 ms** |
| P75 | **7 ms** |
| P90 | **9 ms** |
| P95 | **12 ms** |
| P99 | **16 ms** |
| Min Response Time | **3 ms** |
| Max Response Time | **56 ms** |
| Standard Deviation | **3.3 ms** |
| Throughput | **50.86 req/s** |
| Avg Response Payload | **882 bytes** |
| Test Duration | **9.8 seconds** |

---

## Assessment

The Add Item to Cart endpoint performs exceptionally well under load. Zero errors across all 500 authenticated POST requests and a P99 of just 16 ms means virtually every user gets a response in under 16 milliseconds regardless of concurrent load. The standard deviation of 3.3 ms confirms near-perfect consistency with no signs of contention or queuing at 50 concurrent users.

As a write operation (database INSERT), it is expected to be slightly slower than the read-only Get All Products endpoint — yet the difference is minimal (6.1 ms vs 1.7 ms average), indicating the write path is well optimized. The authentication layer adds negligible overhead as the JWT validation is handled efficiently before the cart logic executes.

---

## Comparison — All Endpoints

| Metric | Login (Post-Opt) | Get All Products | Add to Cart |
|---|---|---|---|
| Average | 117 ms | 1.7 ms | **6.1 ms** |
| P90 | 138 ms | 3 ms | **9 ms** |
| P99 | 264 ms | 5 ms | **16 ms** |
| Max | 337 ms | 23 ms | **56 ms** |
| Std Deviation | 29 ms | 1.5 ms | **3.3 ms** |
| Throughput | 44.28 req/s | 50.96 req/s | **50.86 req/s** |
| Error Rate | 0.00% | 0.00% | **0.00%** |
| Auth Required | Yes | No | Yes |
| Operation Type | Write | Read | Write |

---

## Verdict

✅ No optimization required. This endpoint is production-ready as tested.

---
*Document version: 1.0 | Date: 03 March 2026*
