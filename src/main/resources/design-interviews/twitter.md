Below is a **fully-self-contained, GitHub-ready Markdown study guide** for the classic *“Design Twitter”* question.
I updated every “see X” cross-reference so you no longer need to jump to other docs, and I added a quick-reference *
*cheat-sheet table** of conversions and rule-of-thumb numbers you should memorize.

---

# Designing “Twitter” - System Design Study Guide

> Difficulty  **Medium**   • Target interview: Coinbase, FAANG, FinTech
> Goal Explain how to post, store, serve, and scale 100 M tweets/day with 28 B timeline reads/day.

---

## 1. What *is* Twitter?

Twitter (now *X*) is a micro-blogging network where registered users publish short messages (≤280 chars) called **tweets
**. Visitors can:

* **Post** tweets (optionally with photos / videos).
* **Follow** other accounts to receive their tweets in a personal **timeline**.
* **Like** (favorite) tweets.
* Consume via Web, mobile apps, SMS, public APIs.

---

## 2. Requirements & Goals

| Type                        | Requirement                                                                                                                                            |
|-----------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Functional**              | *Post* tweet, *follow/unfollow* users, *like* tweets, build **timeline** (top N recent tweets from followees), support embedded media (photo / video). |
| **Non-functional**          | 99.95 % availability • p95 timeline latency ≤ 200 ms • write latency ≤ 50 ms. Consistency may be slightly relaxed (eventual OK).                       |
| **Extended - nice to have** | Search tweets • Replies • Retweets • Trends • “Who to follow” suggestions • Notifications • Moments.                                                   |

---

## 3. Scale & Capacity Estimates

**Users & traffic**

| Metric                   | Assumption                                                                                             | Result |
|--------------------------|--------------------------------------------------------------------------------------------------------|--------|
| Total users              | 1 B                                                                                                    |        |
| Daily active users (DAU) | 200 M                                                                                                  |        |
| New tweets/day           | 100 M ⇒ **≈1.2 k writes/s** (×3 ⇒ 3 k–4 k peak)                                                        |        |
| Likes/day                | 5 likes/DAU ⇒ 1 B                                                                                      |        |
| Timeline views/day       | Each DAU reads timeline twice + 5 profile visits × 20 tweets   → 28 B ⇒ **≈325 k reads/s** (≈1 M peak) |        |

**Storage**

* Tweets: 140 chars ≈ 280 B + 30 B meta ⇒ 310 B.
  100 M × 310 B ≈ **30 GB/day** text (no media).
* Media: 1 photo/5 tweets @ 200 kB, 1 video/10 tweets @ 2 MB ⇒ ≈ **24 TB/day**.

5 years of tweets: 30 GB × 365 × 5 ≈ 55 TB (text-only). Media → multi-petabyte (handled by S3/Lake).

**Bandwidth**

*Ingress*: 24 TB/day ≈ 290 MB/s
*Egress*: 35 GB/s peak (CDN for media, edge cache for hot tweets).

---

## 4. Public API Example (REST)

```http
POST /v1/tweets
Headers: Authorization: Bearer <JWT>

{
  "text": "gm world 🌞",
  "media_ids": ["abc123", "def456"],
  "reply_to": "987654321",
  "location": {"lat": 37.77, "lon": -122.4}
}
```

*Response*: `201 Created` → `{ "tweet_url": "https://x.com/123456" }`

---

## 5. High-Level Architecture

```
          ┌─────────────────┐
          │   Load Balancer │
          └────────┬────────┘
                 HTTPS
                (REST)
          ┌────────▼────────┐
          │  Tweet API Tier │  ← horizontal Go/Java servers
          └────────┬────────┘
     Kafka produce │ (tweets_raw)
          ┌────────▼────────┐
          │   Kafka Cluster │  ← write 1.2k/s, read fan-out
          └────────┬────────┘
        Flink / Spark Enrichment
          ┌────────▼────────┐
          │   Timeline Fan- ░░ streaming job
          │   out Service   │  (push to followers, update cache)
          └────────┬────────┘
           Redis / Memcached (recent timelines, hot tweets)
          ┌────────▼────────┐
          │  Object store   │ media (S3+CloudFront)
          └─────────────────┘
          Iceberg tables in S3 for long-term analytics
```

---

## 6. Database Schema (choice & rationale)

| Table                                                                | Reasoning / Storage                                                                   |
|----------------------------------------------------------------------|---------------------------------------------------------------------------------------|
| **Users** (`user_id PK`, profile fields)                             | Rare writes, frequent reads ⇒ RDBMS (Aurora / Spanner) or strongly-consistent Doc DB. |
| **Tweets** (`tweet_id PK`, `user_id`, `created_at`, `text`, `media`) | Massive append-only ⇒ wide-column store (Cassandra) **sharded by `tweet_id`**.        |
| **Follows** (`user_id`, `followee_id`)                               | Small rows, need existence checks ⇒ DynamoDB partitioned by `user_id`.                |
| **Likes** (`user_id`, `tweet_id`)                                    | High write, idempotent ⇒ DynamoDB with TTL for unlikes.                               |

*SQL vs NoSQL*: Tweets demand horizontal scale + high write; follows & likes need low-latency lookups. Use polyglot:
Cassandra / ScyllaDB for tweets, DynamoDB for edges.

---

## 7. Sharding Strategy

**TweetID = (Time-ordered ULID64)**
`[48-bit epoch seconds][16-bit sequence]`

1. **Primary key** naturally sorts by time → efficient “latest tweets” scan.
2. **Shard** by `(tweet_id % N_shards)`; new tweets distribute evenly.
3. Hot celebrity problem mitigated by sequence bits.

**Alternative**: shard by `user_id`; simpler reads per user but hot-key risk.

---

## 8. Caching

| Layer    | What is cached                                     | TTL             | Replacement |
|----------|----------------------------------------------------|-----------------|-------------|
| L1 Redis | “timeline\:user\_id” → list\<tweet\_id> (latest N) | 30 s            | LRU         |
| L2 Redis | individual tweet objects                           | 10 min          | LRU         |
| CDN      | media (images/video)                               | 1 h (immutable) | N/A         |

Cache miss → query Cassandra (tweets) + Dynamo (follows) → recalc timeline.

---

## 9. Timeline Generation (self-contained)

### Push vs Pull

| Strategy | How                                                                                        | Pros                        | Cons                                     |
|----------|--------------------------------------------------------------------------------------------|-----------------------------|------------------------------------------|
| **Push** | On write, insert tweet-id into *each follower’s* timeline list (Redis list or Flink state) | O(1) read (just a list pop) | Write amplification (avg 200 fan-outs)   |
| **Pull** | On read, fetch K followees, merge their last M tweets by recency                           | Cheap writes                | Read heavy; scatter-gather across shards |

**Hybrid**: Push for heavy users (celeb followees), pull for long-tail.

---

## 10. Fault Tolerance & Replication

* **Kafka**: `replication.factor=3`, `min.insync.replicas=2`.
* **Cassandra**: RF = 3 across AZs, QUORUM reads (timeline OK with slight staleness).
* **Redis**: Cluster mode, multi-AZ, async replicas.
* **Media**: S3 versioning + cross-region replication.

---

## 11. Load Balancing

* Client → Edge: ALB + WAF → API pods (round-robin, health-probe).
* API → Kafka: produce w/ batching (`linger.ms=5`, `batch.size=64 kB`).
* Timeline WS: sticky hash on user\_id → gateway pod; Envoy handles WebSocket upgrades.

---

## 12. Monitoring & Alerts

| Metric                      | Alert Threshold |
|-----------------------------|-----------------|
| p95 tweet POST latency      | > 100 ms        |
| p95 timeline latency        | > 200 ms        |
| Kafka consumer lag          | > 10 s          |
| Redis hit ratio             | < 80 %          |
| Cassandra read timeout rate | > 1 %           |

Log lineage for GDPR deletions & internal audits.

---

## 13. Extensions (self-filled)

### Retweet

Store `retweet_id` + pointer to original `tweet_id`. Timeline service expands inline; no text duplication.

### Trending Hashtags

*Windowed Flink* job over Kafka counting hashtags per 5-min sliding window; top N stored in Redis.

### Search

Create **Lucene / OpenSearch** cluster; job streams tweets → inverted index for full-text + hashtag filters.

### Who-to-Follow

Offline Spark ML job using follow-graph embeddings; features stored in Redis; API returns top suggestions.

---

## 14. Memorize-Me Cheat-Sheet

| Quantity         | Value                              |
|------------------|------------------------------------|
| 1 KB             | 1024 bytes                         |
| 1 MB             | 1024 KB ≈ 1 M bytes                |
| 1 GB             | 1024 MB ≈ 1 B bytes                |
| 1 TB             | 1024 GB ≈ 1 T bytes                |
| Secs in day      | 86 400                             |
| Tweets/day 100 M | ≈1.2 k tweets/s (avg)              |
| 28 B reads/day   | ≈325 k reads/s (avg)               |
| Like ratio       | 5 likes/DAU → 1 B/day              |
| Media ≈24 TB/day | 278 MB/s ingress                   |
| p95 lat targets  | 50 ms write · 200 ms read          |
| Kafka safe RF    | 3 (2 in-sync)                      |
| Redis RAM        | 1 byte key overhead ≈ 16–48 B item |

---

**Practice**: Re-draw the high-level boxes in 60 s, then verbally walk through write-path, read-path, failure scenario,
and one extended feature. That’s the polished answer interviewers expect.
