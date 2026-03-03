# Stress Test Report — Login Endpoint
## Pre-Optimization Performance Baseline

| | |
|---|---|
| **Endpoint** | `POST /api/users/public/login` |
| **Host** | `localhost:8080` |
| **Date** | 03 March 2026, 08:42:54 UTC |
| **Tool** | Apache JMeter |
| **Report Status** | DRAFT — Pre-Optimization Baseline |
| **Prepared by** | Development Team |

> **Purpose:** This document captures the current performance state of the login endpoint *before* any latency or optimization work begins. All metrics serve as the official baseline for future benchmarking and regression testing.

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Test Configuration](#2-test-configuration)
3. [Test Results](#3-test-results)
4. [Reliability & Stability Analysis](#4-reliability--stability-analysis)
5. [Performance Degradation Analysis](#5-performance-degradation-analysis)
6. [Network & Connection Analysis](#6-network--connection-analysis)
7. [Observations & Root Cause Hypothesis](#7-observations--root-cause-hypothesis)
8. [Known Limitations of This Test](#8-known-limitations-of-this-test)
9. [Optimization Targets & Recommendations](#9-optimization-targets--recommendations)
10. [Acceptance Criteria for Post-Optimization Testing](#10-acceptance-criteria-for-post-optimization-testing)
11. [Appendix — Raw Metric Reference](#11-appendix--raw-metric-reference)

---

## 1. Executive Summary

This report presents the results of a stress test conducted on the Login API endpoint. The test simulated **50 concurrent users** each making **10 sequential login requests**, for a total of **500 HTTP requests** over approximately **45.6 seconds**.

### Key Findings at a Glance

| Metric | Result | Assessment |
|---|---|---|
| Total Requests | 500 | Complete |
| Error Rate | **0.00%** | ✅ PASS |
| Average Response Time | **3,451 ms** | ⚠️ Needs improvement |
| Median Response Time | **3,774 ms** | ⚠️ Needs improvement |
| P90 Response Time | **5,216 ms** | ⚠️ Needs improvement |
| Min Response Time | **425 ms** | ✅ Healthy at low concurrency |
| Max Response Time | **6,595 ms** | ⚠️ Unacceptable for UX |
| Throughput | **10.96 req/s** | Baseline recorded |
| Peak Concurrent Users | **50** | Target achieved |

The system demonstrated **perfect reliability** — zero errors, zero failed requests, and consistent HTTP 200 responses across all 500 calls. This confirms the endpoint is functionally stable under the tested load.

However, **response times are significantly high** and grow in near-linear proportion with the number of concurrent threads. Under low concurrency (1–9 threads), average response time is a reasonable 764 ms. At peak concurrency (40–49 threads), average response time climbs to 4,162 ms — a **5.5× degradation**. This behavior is the primary issue to be addressed in the upcoming optimization phase.

---

## 2. Test Configuration

### 2.1 JMeter Thread Group Settings

| Parameter | Value |
|---|---|
| Thread Group Name | Login |
| Number of Threads (Virtual Users) | 50 |
| Ramp-Up Period | 10 seconds |
| Loop Count | 10 iterations per thread |
| Same User on Each Iteration | Yes |
| Total Planned Requests | 500 (50 × 10) |
| Total Executed Requests | 500 |

The 10-second ramp-up means one new virtual user was introduced approximately every 0.2 seconds. This gradual ramp-up simulates a realistic burst scenario and also provides visibility into how the system behaves at different concurrency levels during the same test run.

### 2.2 Target Endpoint

| Parameter | Value |
|---|---|
| URL | `http://localhost:8080/api/users/public/login` |
| HTTP Method | POST |
| Protocol | HTTP/1.1 |
| Host | localhost |
| Port | 8080 |
| Environment | Local development machine |
| Headers | Managed via HTTP Header Manager |

### 2.3 Request & Response Payload Sizes

| Parameter | Value |
|---|---|
| Average Request Size (Sent) | 257 bytes |
| Average Response Size (Received) | 1,065 bytes |
| Response Content Type | text |

The response payload is consistent at exactly 1,065 bytes across all 500 requests, which confirms the server returned the same data structure for every call (no truncated or partial responses).

### 2.4 Listener / Results Collection

Results were captured using JMeter's **Aggregate Report** listener and saved to `HTTP_Request.csv`. The following fields were recorded per request: `timeStamp`, `elapsed`, `label`, `responseCode`, `responseMessage`, `threadName`, `dataType`, `success`, `failureMessage`, `bytes`, `sentBytes`, `grpThreads`, `allThreads`, `URL`, `Latency`, `IdleTime`, `Connect`.

---

## 3. Test Results

### 3.1 Overall Performance Metrics

| Metric | Value | Unit |
|---|---|---|
| Total Samples | 500 | requests |
| Successful Requests | 500 | requests |
| Failed Requests | 0 | requests |
| Error Rate | 0.00% | % |
| Test Duration | 45.6 | seconds |
| Throughput | 10.96 | requests/sec |
| Average Response Time | 3,451 | ms |
| Median Response Time (P50) | 3,774 | ms |
| Standard Deviation | 1,516 | ms |
| Minimum Response Time | 425 | ms |
| Maximum Response Time | 6,595 | ms |
| Average Latency (TTFB) | 3,449 | ms |
| Average Connection Time | 0.052 | ms |
| Average Response Payload | 1,065 | bytes |
| Average Request Payload | 257 | bytes |

The standard deviation of **1,516 ms** is notably high — nearly 44% of the average value. This indicates wide variability in response times, which is directly explained by the varying concurrency levels throughout the test. A low standard deviation would indicate consistent performance; the high value here is a clear signal of load-induced degradation.

### 3.2 Response Time Percentile Distribution

Percentiles show the maximum response time experienced by a given proportion of users. They are more meaningful than averages for understanding user experience because they expose the tail end of the distribution.

| Percentile | Response Time | Interpretation |
|---|---|---|
| P50 (Median) | 3,774 ms | Half of all users waited more than 3.77 seconds |
| P75 | 4,726 ms | 1 in 4 users waited more than 4.73 seconds |
| P90 | 5,216 ms | 1 in 10 users waited more than 5.22 seconds |
| P95 | 5,467 ms | 1 in 20 users waited more than 5.47 seconds |
| P99 | 5,874 ms | 1 in 100 users waited more than 5.87 seconds |

The gap between the **minimum (425 ms)** and the **median (3,774 ms)** is striking — nearly a 9× difference. This is not noise; it is a structural pattern showing that request timing is heavily influenced by how many other requests are in-flight simultaneously.

The relatively small gap between P90 (5,216 ms) and P99 (5,874 ms) suggests that once the system is under full load, the response time distribution is fairly tight in the upper tail — meaning saturation is consistent rather than sporadic.

### 3.3 HTTP Response Code Distribution

| Response Code | Count | Percentage |
|---|---|---|
| 200 OK | 500 | 100.00% |
| 4xx Client Error | 0 | 0.00% |
| 5xx Server Error | 0 | 0.00% |
| Timeout / No Response | 0 | 0.00% |

Every single request received a valid `200 OK` response. The endpoint did not crash, did not return errors, and did not time out at any point during the test. This is a strong stability signal.

---

## 4. Reliability & Stability Analysis

### 4.1 Error Rate

**Error rate: 0.00%** across all 500 requests.

This is the most important reliability metric. At 50 concurrent users with 10 loops each, the login endpoint handled the full load without a single failure. This confirms:

- No connection refusals or resets
- No HTTP 4xx or 5xx responses
- No timeouts
- No partial or malformed responses
- No thread starvation causing dropped requests

### 4.2 Response Consistency

The response payload size was **identical across all 500 requests at 1,065 bytes**. This indicates the server returned the same response structure every time — no error fallbacks, no truncated responses, no empty bodies. The endpoint behaved deterministically under load.

### 4.3 Connection Stability

The average TCP connection time was **0.052 ms**, effectively near-zero. This confirms that the network layer (loopback interface on localhost) introduced no overhead, and all observed latency originates entirely from application-layer processing — not from networking. This is important context when interpreting the response time figures.

---

## 5. Performance Degradation Analysis

### 5.1 Response Time by Concurrency Level

This is the most insightful section of the report. By grouping requests according to how many threads were active at the time of the request, a clear degradation curve emerges.

| Active Threads | Avg Response (ms) | Min (ms) | Max (ms) | Request Count |
|---|---|---|---|---|
| 1 – 9 | 764 | 425 | 1,868 | 25 |
| 10 – 19 | 1,400 | 556 | 3,264 | 42 |
| 20 – 29 | 2,248 | 954 | 4,000 | 46 |
| 30 – 39 | 3,483 | 1,172 | 5,802 | 89 |
| 40 – 49 | 4,162 | 642 | 6,595 | 293 |
| 50 (peak) | 2,891 | 815 | 3,756 | 5 |

### 5.2 Degradation Rate

| Concurrency Range | Avg Response Time | Change from Baseline |
|---|---|---|
| 1–9 threads (baseline) | 764 ms | — |
| 10–19 threads | 1,400 ms | +83% |
| 20–29 threads | 2,248 ms | +194% |
| 30–39 threads | 3,483 ms | +356% |
| 40–49 threads (peak steady state) | 4,162 ms | +445% |

The progression is close to linear, which is characteristic of a **queuing bottleneck**. When a shared resource (such as a database connection pool, a thread pool, or an authentication service) has a fixed capacity and more requests arrive than can be served simultaneously, excess requests queue up. Each request's wait time is proportional to how many others are ahead of it in the queue.

### 5.3 The Majority of Requests Hit Peak Load

293 out of 500 requests (58.6%) were executed when 40–49 threads were active. This is expected given that the ramp-up completes partway through the test and the remaining iterations run at full concurrency. This means the "average" response time of 3,451 ms is heavily weighted toward peak-load behavior and is a realistic representation of what users would experience during a login burst.

### 5.4 Notable Outliers

The minimum response time of **425 ms** was recorded early in the test when only 1–3 threads were active. This demonstrates the endpoint is inherently capable of fast responses when not under contention. The optimization goal is to bring responses closer to this baseline figure even under full load.

The maximum of **6,595 ms** occurred at peak concurrency. At 6.6 seconds for a login operation, this would be considered a poor user experience in any production context.

---

## 6. Network & Connection Analysis

| Metric | Value | Notes |
|---|---|---|
| Average Connection Time | 0.052 ms | Negligible — localhost loopback |
| Average Latency (TTFB) | 3,449 ms | Almost identical to elapsed time |
| Avg Elapsed Time | 3,451 ms | Total round-trip time |
| Idle Time | 0 ms | No idle time recorded |

The near-zero difference between **Latency (TTFB)** and **Elapsed time** (3,449 ms vs 3,451 ms) confirms that:

1. The server begins sending the response almost immediately after finishing processing — there is no slow transmission phase.
2. The bottleneck is entirely in **server-side processing time**, not in data transfer.
3. Network infrastructure is **not a factor** in any of the observed latency.

This is important for the optimization phase: engineers should focus exclusively on server-side processing (database queries, authentication logic, token generation, middleware) rather than any network-related improvements.

---

## 7. Observations & Root Cause Hypothesis

### 7.1 Primary Observation: Queuing-Type Degradation

The near-linear relationship between concurrent threads and response time is the hallmark of a **resource contention / queuing problem**, not a computational one. If the bottleneck were CPU-bound work (e.g., bcrypt hashing), we would expect to see more erratic, non-linear spikes. Instead, the smooth progression suggests requests are waiting in an orderly queue for access to a fixed-capacity resource.

### 7.2 Most Likely Root Causes

**Database connection pool saturation** — The most common cause of this pattern in login endpoints. If the connection pool size is set to a low number (e.g., 5–10 connections) and 50 threads simultaneously need a database connection to validate credentials, the majority will queue waiting for a free connection. Response time then scales linearly with pool exhaustion.

**Authentication/hashing overhead accumulation** — Password hashing algorithms like bcrypt, argon2, or scrypt are intentionally CPU-expensive. Under concurrent load, if these operations all compete for CPU time, response times can stack up. This would compound a connection pool issue.

**Thread pool limits in the application server** — If the embedded application server (e.g., Tomcat, Netty) has a limited worker thread pool, incoming requests may be queued at the HTTP level before any application logic runs.

**JWT signing bottleneck** — If the endpoint generates a JWT token on login and uses a synchronous signing operation backed by a single key manager, this could serialize under concurrent load.

### 7.3 What the Data Rules Out

- **Network issues:** Connection time is 0.052 ms — completely negligible.
- **Application crashes or instability:** Zero errors, fully consistent responses.
- **Memory exhaustion:** No timeouts or errors that would indicate GC pauses or OOM conditions.
- **Intermittent faults:** The response pattern is smooth and predictable, not random.

---

## 8. Known Limitations of This Test

The following limitations should be considered when interpreting these results and planning future tests.

**Local environment only.** All tests were run on `localhost:8080`. Production infrastructure (remote servers, load balancers, real network latency, different hardware specs) will produce different results. These numbers are valid as a relative baseline but should not be used to predict production behavior directly.

**Single endpoint tested.** Only the login endpoint was tested. No other endpoints were active during this test, so there was no competing load from other application features. In production, the login endpoint would compete with all other traffic.

**No think time between iterations.** JMeter loops execute requests back-to-back without any pause. Real users would pause between actions. This makes the test more aggressive than typical real-world usage.

**Single credential set.** All threads logged in using the same user credentials. This may have introduced database query caching effects that would not occur with a diverse user base. It may also have triggered session or token management behavior specific to repeated logins by the same user.

**No warm-up period.** The test started immediately after JMeter launch. The application server and JVM were likely not in a fully warmed-up state (JIT compilation, connection pool initialization) for the first few seconds, which may have inflated early response times slightly.

**No soak/endurance testing.** This test ran for ~45 seconds. Longer-running tests (soak tests of 10–60 minutes) would reveal memory leaks, connection pool exhaustion over time, and other time-dependent behaviors.

---

## 9. Optimization Targets & Recommendations

The following items are recommended for investigation and implementation before the next round of testing. Each addresses one or more root causes identified in Section 7.

### 9.1 Database Connection Pool Tuning (High Priority)

Review and increase the database connection pool size. For 50 concurrent users, a minimum pool size of 20–50 connections is a reasonable starting point. Tools/config to check: HikariCP `maximumPoolSize`, Spring Boot `spring.datasource.hikari.*` properties, or the equivalent for your stack.

### 9.2 Async or Non-Blocking Login Processing (High Priority)

If the application server uses a blocking I/O model (e.g., traditional Tomcat with blocking servlets), consider migrating the login flow to a non-blocking/reactive model. Under blocking I/O, each concurrent request consumes a thread for its entire duration; under non-blocking I/O, threads are freed during I/O waits and can serve other requests.

### 9.3 Password Hashing Cost Factor Review (Medium Priority)

Review the bcrypt (or equivalent) work factor. A work factor that is appropriate for a single-threaded context may be too expensive when 50 requests are hashing passwords simultaneously. Consider whether the current cost factor can be reduced while remaining within security policy, or whether hashing can be offloaded to a dedicated thread pool.

### 9.4 Response Caching for Token Generation (Medium Priority)

If the endpoint regenerates a full JWT on every request (including re-fetching user roles/permissions from the database), consider caching user metadata with a short TTL to reduce database round-trips per login.

### 9.5 Thread Pool Configuration (Medium Priority)

Review the embedded server thread pool configuration. Ensure the maximum thread count is not artificially limiting concurrency. For 50 concurrent users, a minimum of 50 worker threads should be available.

### 9.6 Database Query Optimization (Medium Priority)

Profile the SQL queries executed during login (credential lookup, role fetch, session creation). Ensure appropriate indexes exist on `username`/`email` columns and that queries are not performing full table scans.

---

## 10. Acceptance Criteria for Post-Optimization Testing

The following thresholds define success for the next round of stress testing after optimization work is complete. These targets are based on industry-standard web application performance expectations.

| Metric | Current Baseline | Target (Post-Optimization) |
|---|---|---|
| Error Rate | 0.00% | ≤ 0.00% |
| Average Response Time | 3,451 ms | ≤ 1,000 ms |
| P90 Response Time | 5,216 ms | ≤ 1,500 ms |
| P95 Response Time | 5,467 ms | ≤ 2,000 ms |
| P99 Response Time | 5,874 ms | ≤ 3,000 ms |
| Max Response Time | 6,595 ms | ≤ 3,500 ms |
| Throughput | 10.96 req/s | ≥ 30 req/s |
| Standard Deviation | 1,516 ms | ≤ 400 ms |

The target average response time of **≤ 1,000 ms** under 50 concurrent users represents a roughly **3.5× improvement** over the current baseline. The standard deviation target of ≤ 400 ms aims to ensure consistency across concurrency levels, not just a lower average.

---

## 11. Appendix — Raw Metric Reference

### A. Full Percentile Table

| Percentile | Response Time (ms) |
|---|---|
| P10 | ~680 |
| P25 | ~1,700 |
| P50 | 3,774 |
| P75 | 4,726 |
| P90 | 5,216 |
| P95 | 5,467 |
| P99 | 5,874 |
| P100 (Max) | 6,595 |

### B. Test Execution Timeline

| Phase | Timestamp (UTC) | Threads Active |
|---|---|---|
| Test start | 08:42:54.680 | 1 |
| Ramp-up midpoint (~5s) | 08:42:59 | ~25 |
| Full concurrency reached | 08:43:04 | 50 |
| Test end | 08:43:40 | 1 |

### C. Data File Reference

| Field | Description |
|---|---|
| `timeStamp` | Unix epoch milliseconds when request was sent |
| `elapsed` | Total round-trip time in milliseconds |
| `label` | JMeter sampler label ("HTTP Request") |
| `responseCode` | HTTP status code (all 200) |
| `success` | Boolean pass/fail per JMeter assertion |
| `bytes` | Response bytes received |
| `sentBytes` | Request bytes sent |
| `grpThreads` | Active threads in this thread group |
| `allThreads` | Total active threads across all groups |
| `Latency` | Time to first byte (TTFB) in ms |
| `IdleTime` | Time spent idle (all 0) |
| `Connect` | TCP connection establishment time in ms |

### D. JMeter Version Notes

Results were collected using Apache JMeter with the Aggregate Report listener. The CSV file was generated with "Save Table Header" enabled. All timestamps are in Unix epoch milliseconds (UTC).

---

*End of Report*

---
*Document version: 1.0 — Pre-Optimization Baseline | Status: DRAFT | Date: 03 March 2026*
