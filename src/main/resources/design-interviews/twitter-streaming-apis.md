You're asking an **incredibly real-world and senior-level question**. Let's get practical.

> **You want to expose a streaming API** that provides **real-time blockchain or enriched data** to **internal teams and
external clients** — potentially **100,000+ concurrent consumers**.

This is exactly the kind of challenge you’d solve as a senior at Coinbase or any modern fintech company.

---

## 🧠 The Core Problem

You're trying to **fan out high-volume real-time data** (like blockchain transactions, order book changes, or enrichment
events) to:

1. **Internal services** (analytics, fraud detection, trading bots)
2. **External consumers** (via APIs — maybe paying customers, traders, partners)

This is **not trivial**: it must be **scalable**, **secure**, **low-latency**, and **fault-tolerant**.

---

## 🔧 Core Tech Choices

| Tech                        | Best For                                     | Notes                                                                                   |
|-----------------------------|----------------------------------------------|-----------------------------------------------------------------------------------------|
| **Kafka/MSK**               | Internal pipelines                           | Durable pub/sub. Great for Flink, Spark, etc. Not exposed to external clients directly. |
| **gRPC**                    | Fast, typed APIs between services            | Great for internal APIs, low-latency, supports streaming, contract-first.               |
| **WebSockets**              | External real-time clients                   | Browser-friendly, mobile-friendly. More infra work.                                     |
| **GraphQL (subscriptions)** | Event-driven external data (e.g. new blocks) | Developer-friendly but less battle-tested at scale.                                     |
| **SNS/SQS**                 | Fan-out to other AWS services                | Not designed for user-facing APIs. Internal only.                                       |
| **NATS / MQTT**             | Lightweight pub/sub                          | Often used in IoT or internal control planes.                                           |

---

## ✅ Typical Industry Architecture for Public Streaming APIs

### 💡 Pattern: **Kafka or Flink → Gateway → WebSocket/gRPC API**

```text
[Flink Job or Kafka Topic]
        ↓
[Push Gateway Service]
        ↓
[gRPC or WebSocket API Layer]
        ↓                     ↓
[Internal consumers]     [External clients]
```

* **Kafka/Flink**: source of truth (real-time chain updates, enriched events).
* **Push Gateway**: consumes from Kafka, maintains session state and connection pools.
* **API Layer**: offers streaming APIs (gRPC or WebSocket). Auth/authz lives here.
* **Rate limiting, user tiers, partitions of data** (e.g. US customers only) applied here.

---

## 🎥 Let’s Talk Streaming APIs: gRPC vs WebSocket vs GraphQL Subscriptions

| Feature               | gRPC                                 | WebSocket           | GraphQL Subscriptions                |
|-----------------------|--------------------------------------|---------------------|--------------------------------------|
| **Latency**           | 🟢 Excellent (HTTP/2)                | 🟢 Excellent        | 🟠 Depends on backend                |
| **Scalability**       | 🟢 High                              | 🟡 Needs infra care | 🔴 Tricky to scale                   |
| **Type Safety**       | 🟢 Strong with Protobuf              | 🔴 Manual           | 🟢 Strong with SDL                   |
| **Browser Support**   | 🔴 Poor (no native gRPC in browsers) | 🟢 Native           | 🟢 Native                            |
| **Client Simplicity** | 🟢 SDK-based                         | 🟢 Socket APIs      | 🟢 Apollo & ecosystem                |
| **Internal Use**      | ✅ Best                               | 🚫 Overhead         | 🚫 Uncommon                          |
| **External API**      | 🚫 Needs wrapper                     | ✅ Very common       | 🟢 Often used with Hasura or AppSync |
| **Load Balancing**    | 🟢 Native with Envoy, etc.           | 🟠 Sticky sessions  | 🟠 Depends on engine                 |
| **Backpressure**      | 🟢 Built-in                          | 🟠 You implement    | 🔴 Very tricky                       |

---

## ✅ Recommendation by Use Case

| Use Case                                                        | Recommendation                             |
|-----------------------------------------------------------------|--------------------------------------------|
| Internal services: data science, fraud detection                | **gRPC** + Kafka/Flink                     |
| Internal real-time microservices                                | **gRPC streaming**                         |
| External real-time client (e.g., user wants live txs or prices) | **WebSocket** or **GraphQL Subscriptions** |
| Public APIs (coinbase.com/docs style)                           | REST + WebSockets or REST + polling        |

---

## 💬 How Coinbase Actually Does It

Coinbase has public APIs like:

* `wss://ws-feed.exchange.coinbase.com` (WebSocket)
* `https://api.exchange.coinbase.com` (REST)

Internally, they:

* Use **Kafka/MSK** for event streams
* Use **gRPC for internal service APIs**
* Stream enriched data (like transactions, identity events) to **Flink → Kinesis or Kafka → real-time analytics**
* WebSocket services for **real-time UIs, alerts, and trade feeds**

Read
more: [https://docs.cloud.coinbase.com/exchange/docs/websocket-overview](https://docs.cloud.coinbase.com/exchange/docs/websocket-overview)

---

## 🧠 Senior-Level Considerations

### 1. **Backpressure & Flow Control**

* gRPC: built-in.
* WebSocket: custom flow control (ack messages or windowing).
* GraphQL subs: must wrap with backpressure protocol (e.g., `graphql-ws`).

### 2. **Security**

* Internal: IRSA + mTLS + gRPC ACLs.
* External: OAuth2, API keys, IP whitelisting.
* Optionally: JWTs with scopes like `realtime:tx.read`.

### 3. **Rate Limiting / Fair Usage**

* Protect against clients consuming too fast or subscribing to everything.
* Tiered access (basic/free tier = 5 tx/sec, premium = 100 tx/sec).
* Drop events for clients falling too far behind.

### 4. **Multitenancy / Sharding**

* Can’t stream *all* data to *everyone*.
* Filter by:

  * Region
  * Wallet address
  * Chain
  * Access level (e.g., only show “your own” events)

---

## 🏗️ Example: Design a Real-Time Streaming API for `coinbase.com/stream`

1. **Producer:**

* Blockchain node → Kafka topic (`chain-events`)
* Flink job enriches events → `chain-events.enriched`

2. **Gateway Service (Go or Rust)**

* Consumes Kafka or uses Flink side outputs
* Pushes to connected clients
* Maintains per-client filters (chain, wallet, user tier)

3. **API Layer**

* Expose:

  * `POST /stream/subscribe` (REST to start session)
  * `GET /stream/ws` (WebSocket upgrade)
  * `streamService.Subscribe()` (gRPC streaming method)

4. **Security**

* External clients: OAuth2 + JWTs
* Internal: gRPC mTLS + IRSA + IAM

5. **Infra**

* Use NGINX/Envoy/WebSocket proxy
* Horizontal autoscaling (per connection count)
* Use Redis or memory for session state

---

## 🔚 Final Recommendation

For your use case:

| Tier                                   | Choice                                      |
|----------------------------------------|---------------------------------------------|
| Internal microservices                 | **Kafka + gRPC streaming**                  |
| External real-time APIs (devs/traders) | **WebSockets** (GraphQL subs optional)      |
| Durable ingestion & enrichment         | **Kafka → Flink or Spark**                  |
| Push-to-client layer                   | **Custom gateway that fans out from Kafka** |

---

Would you like a diagram + code scaffolding of this architecture (gRPC + Kafka internal + WebSocket external)?

Perfect — let's go **deep** and **hands-on** into how you would build the **gateway service** for a real-time streaming
API like what Coinbase might use to stream blockchain events.

We'll treat this like a **system architecture design interview** response — except fully engineered, with the **why**, *
*how**, and **hard bits** explained like a senior engineer would think about.

---

## 🎯 GOAL

You want a **streaming gateway service** that:

* Accepts **WebSocket** or **gRPC** streaming connections from clients (internal or external)
* Forwards them **real-time events** coming from Kafka or Flink
* Supports **authentication, filtering, and fan-out to thousands (or 100K+) of clients**

---

## 💡 Responsibilities of the Gateway

1. **Maintain long-lived client connections**

* WebSocket or gRPC bidirectional stream

2. **Pull and fan out data from upstream (Kafka/Flink)**

* With **per-client filtering**

3. **Enforce auth + rate limits**
4. **Handle backpressure + client drops**
5. **Track session state in memory (or Redis)**

---

## 🔧 Concrete Stack

Let’s assume:

* **Kafka** is the event backbone (`block-events` topic, JSON or Protobuf messages)
* **Gateway is written in Go** using:

  * `gorilla/websocket` for WS
  * `segmentio/kafka-go` or `confluentinc/confluent-kafka-go`
  * `jwt-go` for auth
* **Redis** for ephemeral session data (client filters, connected clients)

---

## 📦 Message Flow

```plaintext
[Blockchain Ingest (Flink/Spark)] 
      → Kafka: block-events topic (partitioned, compacted)

           ↓
     [Gateway Service]
    - WebSocket connection pool
    - Auth & filtering
    - Kafka consumer (group per topic/shard)
           ↓
   [Internal clients (gRPC)] / [External users (WebSocket)]
```

---

## 🧱 1. Connection Handling (WebSocket)

```go
func handleWS(w http.ResponseWriter, r *http.Request) {
    token := r.Header.Get("Authorization")
    claims, err := validateJWT(token) // parse scopes, client_id, filters

    conn, err := upgrader.Upgrade(w, r, nil) // WebSocket upgrade
    if err != nil { ... }

    client := &Client{
        Conn: conn,
        Filters: claims.Filters,
        RateLimiter: NewTokenBucket(...),
    }

    go client.listenForDisconnect()
    go client.writePump()
}
```

* Each client gets a struct with metadata: filters, rate, socket
* Put client in a concurrent map: `map[client_id]*Client`

---

## 🛠️ 2. Kafka Consumer Pool

You might run one consumer per topic partition **per gateway instance**, or **shard across microservices**.

```go
r := kafka.NewReader(kafka.ReaderConfig{
    Brokers: []string{"broker1:9092"},
    GroupID: "gateway-stream-group",
    Topic:   "block-events",
})

for {
    msg, err := r.ReadMessage(ctx)
    if err != nil { continue }

    event := decodeEvent(msg.Value)

    for _, client := range clients {
        if client.Matches(event) {
            client.Send(event)
        }
    }
}
```

* Clients register interest via filters (e.g. "chain == 'eth'", "address == 0x123")
* You index or loop over clients to fan out

👉 In a scaled version, **fan-out happens via a pub/sub layer** (NATS, Redis pub/sub, or Kafka itself)

---

## 🧰 3. Filtering Logic

```go
func (c *Client) Matches(event Event) bool {
    return event.Chain == c.Filters.Chain && event.Address == c.Filters.Address
}
```

For scale:

* Pre-index clients by filter dimensions
* e.g. `map[string][]*Client` where key is `chain:address`

You might use **Bloom filters** or **Redis set intersections** to speed this up at high scale.

---

## 🚦 4. Backpressure Handling

WebSocket and gRPC are **push-based**, so:

* You must **queue outgoing messages** per client
* You must **drop** or **pause** if client is slow

```go
func (c *Client) writePump() {
    for msg := range c.Outbound {
        err := c.Conn.WriteJSON(msg)
        if err != nil {
            log.Printf("Client %v disconnected", c.ID)
            c.Close()
            return
        }
    }
}
```

If the buffer fills:

* Drop messages
* Disconnect
* Pause delivery with flow control (complex)

---

## 🔐 5. Auth & Authorization

```go
claims, err := ParseJWT(token)
if !claims.HasScope("realtime:stream") {
    http.Error(w, "Unauthorized", 403)
    return
}
```

JWT claims determine:

* Access scope
* Which filters are allowed (e.g., only your own wallet)
* Rate limit tier

---

## 📉 6. Rate Limiting

Token bucket per client:

```go
if !client.RateLimiter.Allow() {
    log.Warn("Rate limit exceeded")
    continue
}
```

Can be memory-local or shared Redis buckets using Lua scripts.

---

## 📊 7. Observability & Ops

Metrics to expose:

* Connections open / per client ID
* Event delivery time / per user
* Kafka lag (how far behind gateway is)
* Drops due to backpressure
* Auth failures, disconnects

Use **Prometheus** + **Grafana** for dashboards.
Use **structured logging** with client IDs.

---

## ⚡️ Scaling the Gateway Service

| Strategy                | How                                                                                     |
|-------------------------|-----------------------------------------------------------------------------------------|
| Horizontal scale        | Each instance consumes Kafka partitions                                                 |
| Load balancing          | Use sticky sessions via NGINX or Envoy (WebSocket aware)                                |
| Multi-tenant separation | Deploy by shard, chain, or customer                                                     |
| External push           | Use gRPC push-to-server or WebSocket                                                    |
| Internal delivery       | Also support gRPC APIs with same logic (`stream Subscribe(Req) returns (stream Event)`) |

---

## 🧱 Optional: Gateway Service as a Sidecar (gRPC Streaming Model)

Coinbase-style:

* Internal services do **not** connect to Kafka
* They connect to the **gateway via gRPC streaming** and subscribe
* Gateway maintains a single Kafka connection
* Pushes decoded structured events into the `Subscribe()` stream

---

## ✅ Summary: What Makes This Gateway Work at Scale

| Concern           | Solution                                       |
|-------------------|------------------------------------------------|
| Client mgmt       | Connection map with filters & metadata         |
| Kafka integration | One consumer group, async fan-out              |
| Message matching  | Per-client filtering, pre-indexing             |
| Auth & scope      | JWTs with scopes (e.g., chain-read, tx-read)   |
| Rate limiting     | Per-connection token bucket                    |
| Observability     | Kafka lag, queue drops, user metrics           |
| Horizontal scale  | Stateless per-connection, NATS/pubsub optional |

---

Would you like me to scaffold this architecture in Go (a simple WebSocket gateway that connects to Kafka, filters, and
streams to clients), or diagram this pipeline to solidify the mental model?

Yes — you’re **right to be confused** because you’re now thinking like a **systems engineer** wrestling with the core
tradeoffs of building a high-scale, low-latency, real-time delivery system. Let me walk you through this step by step,
with **diagrams in words**, **real architecture flows**, and finally answer your core question about **ordering,
partitioning**, and **why it’s hard**.

---

## 🔁 High-Level Flow Overview

Let’s walk through this **end-to-end data flow** in simple steps:

### 🔢 1. Upstream Event Source

* Blockchain node emits events (blocks, transactions)
* They are pushed into Kafka, to a topic like `chain-events`

```text
[Chain Node or Flink Job] → Kafka topic: chain-events
```

### 🔌 2. Gateway Service Starts Up

* Connects to Kafka as a **consumer**
* Subscribes to one or more **partitions** of the topic
* Opens a WebSocket listener for clients

```text
[Kafka Consumer] ← Gateway → WebSocket clients (external or internal)
```

### 📡 3. Clients Connect

* Clients (frontend apps, bots, services) establish WebSocket connections to the Gateway.
* They authenticate (e.g. JWT).
* They declare filters: “I want to see all txns on Ethereum involving address 0x123…”

```text
Client A: wants ETH txns for 0x123  
Client B: wants BTC blocks
```

### 🔁 4. Kafka Emits a New Message

* Kafka gives the Gateway a message from the `chain-events` topic:

  ```json
  {
    "chain": "ETH",
    "type": "tx",
    "from": "0xabc",
    "to": "0x123",
    "value": 1.5
  }
  ```

### 📤 5. Gateway Matches Message to Clients

* Gateway loops through all connected clients.
* For each one, it checks if the message **matches their filter**.
* If so, it pushes it onto that client's **outbound message queue**.

```text
For each msg in Kafka:
    For each WebSocket client:
        If client.filter.matches(msg):
            client.send(msg)
```

### 📬 6. Messages Are Delivered to WebSocket Clients

* Each client has a `writePump()` goroutine sending messages from their queue.
* Messages are JSON or binary (e.g. Protobuf), sent over the socket.

---

## 🤔 Now Your Concern: What About Ordering?

You said:

> "Wait, if I’m reading from Kafka and looping over clients — does that mean I **lose ordering** guarantees per client?"

Let’s unpack that.

---

## 🔎 Kafka Ordering Guarantees: The Truth

| Kafka Ordering                    | Notes                                    |
|-----------------------------------|------------------------------------------|
| **Within a partition**            | Messages are **strictly ordered**        |
| **Across partitions**             | No ordering guarantees                   |
| **For a single Kafka consumer**   | Messages arrive in order (per partition) |
| **For a single WebSocket client** | You must preserve order **yourself**     |

So, in **your gateway**, you get messages in order **from Kafka**.
But — once you’re pushing them into **multiple queues for each WebSocket client**, **you control the ordering**.

---

## ✅ How to Preserve Ordering for a Client

If a client is subscribed to messages **from a single partition**, and you:

* Push each Kafka message onto **client’s queue in the order received**
* And the **client’s write goroutine** delivers them FIFO

➡️ **You preserve order for that client**

But if…

* You have multiple **Kafka partitions**
* Or **multiple gateway instances**
* Or the client subscribes to **multiple sources (e.g., multiple chains)**

➡️ **Ordering becomes best-effort or scoped per-substream**

---

## 🧱 Example: What You Might Do in Production

### A. Partitioning Strategy

* If you want **per-wallet ordering**, you might partition by `wallet address hash`
* That ensures all messages for `0x123` go to the same partition

### B. Kafka Consumer Group

* Each Gateway instance reads a **subset of partitions**
* Guarantees ordered stream per partition

### C. Client Filter Routing

* You assign clients to gateway pods **based on their filter**

  * Example: all `ETH` clients go to pod 1, `BTC` to pod 2

### D. Internal Queues

* Each client gets a **FIFO buffer** (channel or ring buffer)
* Messages are **pushed in-order** and **written in-order**

---

## 🛠 How to Break Ordering (What to Watch Out For)

* If a message matches multiple filters and is **processed in parallel**, you could re-order
* If your system does **multiple reads from Kafka partitions concurrently**, you might see partial reordering
* If a client reconnects and **missed messages**, catching up might create gaps

---

## 🧠 Real-World Engineering Tradeoffs

| Requirement                 | Design Implication                                                |
|-----------------------------|-------------------------------------------------------------------|
| **Strict ordering**         | Use single Kafka partition, scoped filters                        |
| **High throughput**         | Use many partitions, weaken global ordering                       |
| **Low latency**             | Avoid deep client queues, drop slow clients                       |
| **Fan-out to 100K clients** | Shard across gateway pods, push-based delivery, stateful services |
| **Resilience**              | Reconnect with offset checkpointing or deduplication IDs          |

---

## ✅ Summary

* You **can** preserve order per client **if**:

  * Messages come from one partition
  * Gateway instance handles that partition and client
  * Messages are queued and sent in order
* For **large scale**, you sacrifice global ordering for **parallelism**
* Use **filter-aware partitioning**, **scoped ordering**, and **idempotent events** to reduce pain

---

Would you like me to write code scaffolding for:

* A gateway with ordered WebSocket fan-out, or
* A Kafka consumer that routes filtered messages with ordering guarantees?

Or do you want a diagram to visualize this all better?



---
Designing APIs for streaming Twitter data, particularly in the context of Coinbase’s Chainstack—a blockchain data
platform focused on scalability, low-latency, and real-time data access—requires a system that can handle
high-throughput, real-time data streams while ensuring reliability and flexibility for diverse use cases. Since
Chainstack’s streaming APIs are designed to keep downstream systems in sync with blockchain state and handle events like
chain reorganizations, we’ll adapt those principles to a Twitter-like streaming scenario. The goal is to create APIs
that allow users to stream real-time tweet data (akin to blockchain events) efficiently, with considerations for
Coinbase’s emphasis on security, scalability, and practical system
design.[](https://www.coinbase.com/fr-fr/blog/part-1-chainstorage-the-enterprise-blockchain-data-availability-layer)[](https://www.designgurus.io/answers/detail/what-are-the-top-system-design-interview-questions-for-coinbase-interview)

Below, we’ll work through the design of streaming APIs for Twitter data, focusing on the architecture, API
specifications, data flow, and handling edge cases, while aligning with Chainstack’s design philosophy (e.g.,
compute-storage separation, low-latency access, and plug-in extensibility). I’ll assume a system design interview
context, where clarity, trade-offs, and scalability are key, and I’ll incorporate Coinbase’s interview expectations of
practical microservice architecture and network
topology.[](https://www.coinbase.com/fr-fr/blog/part-1-chainstorage-the-enterprise-blockchain-data-availability-layer)[](https://interviewing.io/coinbase-interview-questions)

---

### Step 1: Requirements and Scope

Let’s define the functional and non-functional requirements for the Twitter streaming APIs, tailored to a Coinbase
Chainstack-like environment.

#### Functional Requirements

- **Real-Time Streaming**: Users can subscribe to a stream of tweets (e.g., all tweets, tweets by specific users, or
  tweets matching keywords/hashtags).
- **Filtering**: Support filtering by user IDs, hashtags, or keywords to reduce data volume.
- **Event Notifications**: Notify subscribers of new tweets or updates (e.g., retweets, deletions) in near real-time.
- **Historical Access**: Allow limited access to recent tweets for context (e.g., last 10 minutes of data upon
  subscription).
- **Extensibility**: Support multiple Twitter-like data types (tweets, replies, retweets) and future extensions (e.g.,
  direct messages).

#### Non-Functional Requirements

- **Scalability**: Handle millions of active users and thousands of tweets per second (Twitter averages ~6,000
  tweets/second).[](https://medium.com/%40narengowda/system-design-for-twitter-e737284afc95)
- **Low Latency**: Deliver tweets to subscribers within milliseconds of posting.
- **Reliability**: Ensure no data loss during high traffic or failures, with graceful degradation.
- **Security**: Authenticate and authorize API consumers; prevent abuse (e.g., rate limiting).
- **Consistency**: Handle eventual consistency, as tweet visibility may be delayed slightly without significant user
  impact.[](https://medium.com/%40narengowda/system-design-for-twitter-e737284afc95)

#### Assumptions

- Users interact via WebSocket or HTTP/2 for streaming (aligned with Chainstack’s streaming
  APIs).[](https://www.coinbase.com/fr-fr/blog/part-1-chainstorage-the-enterprise-blockchain-data-availability-layer)
- The system processes tweet metadata (e.g., user ID, text, timestamp, hashtags) rather than media files.
- Inspired by Chainstack, we’ll use a generic data model with plug-in layers for tweet-specific processing.

---

### Step 2: High-Level Architecture

We’ll design the system as a microservices architecture, inspired by Chainstack’s layered approach (ChainStorage,
Chainsformer, ChaIndex) and Coinbase’s focus on practical microservice topology. The architecture separates concerns
like data ingestion, processing, storage, and delivery to ensure scalability and
maintainability.[](https://www.coinbase.com/fr-fr/blog/part-1-chainstorage-the-enterprise-blockchain-data-availability-layer)[](https://interviewing.io/coinbase-interview-questions)

#### Components

1. **Ingestion Layer** (Tweet Ingestion Service):

- Receives new tweets, retweets, and updates from Twitter’s internal systems (or user clients).
- Validates and enriches tweet data (e.g., extracts hashtags, timestamps).
- Publishes raw tweets to a message queue for processing.

2. **Processing Layer** (Tweet Processing Service):

- Consumes tweets from the queue.
- Applies filtering logic (e.g., user IDs, hashtags) and prepares data for streaming.
- Handles event categorization (e.g., new tweet, retweet, deletion).
- Inspired by Chainstack’s Chainsformer, this layer uses big data tools like Apache Spark for stream
  processing.[](https://www.coinbase.com/blog/part-3-from-block-to-apis-building-indexers-on-chainstack)

3. **Storage Layer** (Tweet Storage Service):

- Stores tweet data for persistence and historical queries.
- Uses a combination of:
  - **Redis** for fast, in-memory access to recent tweets (e.g., last 10
    minutes).[](https://medium.com/%40narengowda/system-design-for-twitter-e737284afc95)
  - **Database** (e.g., Cassandra or PostgreSQL) for durable storage of all tweets.
- Similar to ChainStorage’s “read replica” model, this layer optimizes for low-latency
  reads.[](https://www.coinbase.com/fr-fr/blog/part-1-chainstorage-the-enterprise-blockchain-data-availability-layer)

4. **Streaming Layer** (Streaming API Service):

- Manages WebSocket/HTTP/2 connections with clients.
- Subscribes to processed tweet streams and pushes relevant data to clients based on their filters.
- Handles connection lifecycle (e.g., reconnects, heartbeats).

5. **Orchestration Layer** (API Gateway & Orchestration Service):

- Exposes public APIs for subscription management and authentication.
- Routes requests to the Streaming API Service and enforces rate limiting/security.
- Inspired by Chainstack’s orchestration engine for
  indexing.[](https://www.coinbase.com/blog/part-3-from-block-to-apis-building-indexers-on-chainstack)

6. **Monitoring & Scaling**:

- Metrics collection (e.g., latency, throughput) using Prometheus/Grafana.
- Autoscaling for ingestion and processing services based on load.

#### Data Flow

1. A user posts a tweet, which hits the Ingestion Service via an internal API.
2. The Ingestion Service validates the tweet and pushes it to a Kafka topic.
3. The Processing Service consumes the Kafka topic, applies filters, and publishes processed tweets to another Kafka
   topic (or Redis pub/sub for low-latency).
4. The Streaming API Service subscribes to the processed topic and pushes tweets to connected clients via WebSocket.
5. The Storage Service asynchronously stores tweets in Redis (for recent data) and a database (for persistence).
6. Clients subscribe to the Streaming API with filters (e.g., “tweets with #crypto”) and receive real-time updates.

---

### Step 3: API Design

We’ll design RESTful and streaming APIs for subscribing to and receiving Twitter data, ensuring alignment with
Chainstack’s block-level and streaming API philosophy (simple, generic,
extensible).[](https://www.coinbase.com/fr-fr/blog/part-1-chainstorage-the-enterprise-blockchain-data-availability-layer)

#### 1. REST API for Subscription Management

Used to authenticate, configure filters, and initiate streaming.

**Endpoint**: `POST /v1/stream/subscribe`

- **Description**: Creates a streaming session with specified filters.
- **Request Body**:
  ```json
  {
    "api_key": "string", // Authentication
    "filters": {
      "user_ids": ["user123", "user456"], // Optional: Follow specific users
      "hashtags": ["#crypto", "#bitcoin"], // Optional: Match hashtags
      "keywords": ["coinbase"], // Optional: Match keywords in tweet text
      "max_tweets_per_second": 100 // Optional: Throttle delivery rate
    }
  }
  ```
- **Response**:
  ```json
  {
    "session_id": "abc123",
    "websocket_url": "wss://api.twitter-stream.com/v1/stream/abc123"
  }
  ```
- **Notes**:
  - Returns a WebSocket URL for the client to connect to.
  - Rate limiting enforced via `api_key` (e.g., 10 subscriptions per minute).

**Endpoint**: `DELETE /v1/stream/subscribe/{session_id}`

- **Description**: Terminates a streaming session.
- **Response**: `204 No Content`

#### 2. WebSocket Streaming API

Used to deliver real-time tweet data to subscribed clients.

**Connection**: `wss://api.twitter-stream.com/v1/stream/{session_id}`

- **Protocol**: WebSocket with JSON payloads.
- **Message Format** (Server to Client):
  ```json
  {
    "event_type": "new_tweet", // Enum: new_tweet, retweet, delete
    "tweet": {
      "id": "tweet789",
      "user_id": "user123",
      "text": "Loving #crypto at Coinbase!",
      "timestamp": "2025-07-03T22:09:00Z",
      "hashtags": ["#crypto", "#coinbase"],
      "retweet_id": null // Null for original tweets
    }
  }
  ```
- **Client to Server**:
  - Heartbeat messages to maintain connection: `{"type": "heartbeat"}`.
  - Optional filter updates: `{"type": "update_filters", "filters": {...}}`.

- **Notes**:
  - Clients reconnect automatically using `session_id` if disconnected.
  - Server pushes only tweets matching the client’s filters.
  - Backpressure handled by throttling delivery if client buffer fills.

#### 3. Historical Data API (Optional)

For clients needing recent tweets upon subscription.

**Endpoint**: `GET /v1/tweets/recent`

- **Description**: Fetches tweets from the last 10 minutes, filtered by criteria.
- **Query Parameters**:
  - `user_ids`: Comma-separated user IDs.
  - `hashtags`: Comma-separated hashtags.
  - `keywords`: Comma-separated keywords.
  - `limit`: Max tweets to return (default: 100).
- **Response**:
  ```json
  {
    "tweets": [
      {
        "id": "tweet789",
        "user_id": "user123",
        "text": "Loving #crypto at Coinbase!",
        "timestamp": "2025-07-03T22:09:00Z",
        "hashtags": ["#crypto", "#coinbase"]
      }
    ]
  }
  ```

---

### Step 4: Detailed Design

Let’s dive into the key components and their implementation, focusing on scalability and reliability, as Coinbase
interviewers emphasize practical trade-offs.[](https://interviewing.io/coinbase-interview-questions)

#### Ingestion Layer

- **Tech**: Go-based microservice for high-performance I/O.
- **Function**: Accepts tweets via HTTP POST, validates (e.g., text length ≤ 280 characters), and enriches (e.g., parses
  hashtags).
- **Output**: Publishes to Kafka topic `raw_tweets` with schema:
  ```json
  {
    "id": "tweet789",
    "user_id": "user123",
    "text": "string",
    "timestamp": "ISO8601",
    "hashtags": ["string"],
    "retweet_id": "string|null"
  }
  ```
- **Scaling**: Horizontal scaling with load balancer; partition Kafka topic by `user_id` for parallelism.
- **Trade-Offs**:
  - **Pros**: Kafka ensures durability and decouples ingestion from processing.
  - **Cons**: Adds slight latency (~10ms) due to queuing.

#### Processing Layer

- **Tech**: Apache Spark Streaming for real-time processing, inspired by Chainstack’s use of
  Spark.[](https://www.coinbase.com/blog/part-3-from-block-to-apis-building-indexers-on-chainstack)
- **Function**:
  - Consumes `raw_tweets` topic.
  - Applies filters (e.g., match hashtags, keywords) using Spark’s DataFrame API.
  - Publishes filtered tweets to Redis pub/sub channels (e.g., `tweets:hashtag:crypto`).
  - Handles event types (e.g., new tweet, retweet, deletion) with plug-in logic, similar to Chainstack’s
    blockchain-specific
    layers.[](https://www.coinbase.com/fr-fr/blog/part-1-chainstorage-the-enterprise-blockchain-data-availability-layer)
- **Scaling**: Spark cluster with dynamic allocation; Redis sharding for pub/sub.
- **Trade-Offs**:
  - **Pros**: Spark handles high-throughput streams; Redis pub/sub is low-latency.
  - **Cons**: Spark’s overhead may increase latency for simple filters; Redis requires careful memory management.

#### Storage Layer

- **Tech**:
  - **Redis**: Stores recent tweets (10-minute sliding window) for fast access.
  - **Cassandra**: Stores all tweets with schema optimized for time-series queries (e.g., by `timestamp` and `user_id`).
- **Function**:
  - Redis: Key `tweet:<id>` for individual tweets; sorted set `recent_tweets` for time-based queries.
  - Cassandra: Table `tweets (timestamp, user_id, tweet_id, text, hashtags)` with clustering by `timestamp`.
- **Scaling**: Redis cluster for sharding; Cassandra’s distributed nature handles large datasets.
- **Trade-Offs**:
  - **Pros**: Redis ensures low-latency for recent data; Cassandra scales for historical data.
  - **Cons**: Redis memory costs; eventual consistency in Cassandra may delay historical queries.

#### Streaming Layer

- **Tech**: Node.js with `ws` library for WebSocket; Nginx as reverse proxy.
- **Function**:
  - Maintains WebSocket connections (millions of concurrent clients).
  - Subscribes to Redis pub/sub channels based on client filters.
  - Pushes matching tweets to clients.
  - Handles reconnects and heartbeats.
- **Scaling**: Horizontal scaling with Kubernetes; session affinity via `session_id`.
- **Trade-Offs**:
  - **Pros**: WebSocket is ideal for real-time; Node.js handles concurrent connections well.
  - **Cons**: WebSocket state management is complex; requires robust reconnect logic.

#### Orchestration Layer

- **Tech**: Kong API Gateway for routing and rate limiting; OAuth 2.0 for authentication.
- **Function**:
  - Authenticates clients via `api_key`.
  - Enforces rate limits (e.g., 100 connections/hour per key).
  - Routes subscription requests to Streaming API Service.
- **Scaling**: Stateless gateway scales horizontally.
- **Trade-Offs**:
  - **Pros**: Centralized security and rate limiting simplify microservices.
  - **Cons**: Gateway becomes a single point of failure if not highly available.

---

### Step 5: Handling Edge Cases

Coinbase interviewers value robustness and edge-case handling. Let’s address key
scenarios:[](https://www.coinbase.com/blog/how-coinbase-interviews-for-engineering-roles)

1. **High Tweet Volume**:

- **Problem**: A celebrity tweets, causing a spike (e.g., millions of subscribers).
- **Solution**: Precompute filtered streams for popular hashtags/users in Processing Layer; use Redis pub/sub to fan out
  to subscribers efficiently.[](https://medium.com/%40narengowda/system-design-for-twitter-e737284afc95)
- **Chainstack Parallel**: Similar to handling blockchain reorgs by precomputing state
  changes.[](https://www.coinbase.com/fr-fr/blog/part-1-chainstorage-the-enterprise-blockchain-data-availability-layer)

2. **Client Disconnects**:

- **Problem**: Network issues cause clients to drop WebSocket connections.
- **Solution**: Store `session_id` and filters in Redis; allow clients to reconnect and resume using `session_id`.
  Buffer recent tweets (e.g., 30 seconds) in Redis for replay.

3. **Data Consistency**:

- **Problem**: Eventual consistency may cause delayed tweet visibility.
- **Solution**: Accept eventual consistency for non-critical updates (tweets are not financial transactions); prioritize
  low latency over strict consistency.[](https://medium.com/%40narengowda/system-design-for-twitter-e737284afc95)

4. **Rate Limiting Abuse**:

- **Problem**: Malicious clients open excessive connections.
- **Solution**: Enforce per-key rate limits in API Gateway; monitor connection metrics for anomalies.

5. **Failure Recovery**:

- **Problem**: Processing Layer crashes, missing tweets.
- **Solution**: Use Spark’s checkpointing to resume from last processed offset; Kafka’s durability ensures no data
  loss.[](https://www.coinbase.com/blog/part-3-from-block-to-apis-building-indexers-on-chainstack)

---

### Step 6: Trade-Offs and Optimizations

- **Latency vs. Throughput**: WebSocket and Redis prioritize low latency, but Kafka/Spark may introduce 10-50ms delays.
  For ultra-low latency, consider bypassing Spark for simple filters and pushing directly to Redis.
- **Cost vs. Scalability**: Redis is memory-intensive; optimize by expiring old tweets aggressively. Cassandra scales
  better for long-term storage but has higher write latency.
- **Extensibility**: Plug-in architecture (like Chainstack’s blockchain layers) allows adding new filter types (e.g.,
  geolocation) without rewriting core
  logic.[](https://www.coinbase.com/fr-fr/blog/part-1-chainstorage-the-enterprise-blockchain-data-availability-layer)
- **Security**: OAuth 2.0 and rate limiting protect APIs, but consider encrypting WebSocket payloads for sensitive use
  cases.

---

### Step 7: Interview Considerations

For a Coinbase system design interview, expect the interviewer to probe your design’s scalability, security, and
trade-offs. Here’s how to present this
design:[](https://www.designgurus.io/answers/detail/what-are-the-top-system-design-interview-questions-for-coinbase-interview)

- **Whiteboard/Verbal Flow**:
  1. Start with requirements and assumptions (5 min).
  2. Sketch high-level architecture with components and data flow (10 min).
  3. Detail API specs and key microservices (10 min).
  4. Discuss scaling, edge cases, and trade-offs (10 min).
  5. Leave time for questions (5 min).

- **Coinbase-Specific Tips
  **:[](https://www.coinbase.com/blog/how-coinbase-interviews-for-engineering-roles)[](https://interviewing.io/coinbase-interview-questions)
  - Emphasize microservice boundaries and network topology (e.g., Kafka for decoupling, Redis for speed).
  - Highlight security (e.g., OAuth, rate limiting) given Coinbase’s focus on secure financial systems.
  - Be ready to justify tech choices (e.g., “I chose Spark for its streaming capabilities, but for simpler filters, a
    custom Go service could reduce latency”).
  - If asked to simplify, propose a single-service MVP with HTTP polling instead of WebSocket for small-scale use cases.

- **Chainstack Alignment**:
  - Mirror Chainstack’s compute-storage separation by isolating ingestion, processing, and streaming.
  - Emphasize extensibility, as Chainstack supports multiple blockchains via
    plug-ins.[](https://www.coinbase.com/fr-fr/blog/part-1-chainstorage-the-enterprise-blockchain-data-availability-layer)
  - Discuss low-latency streaming, as Chainstack’s APIs rival relational database
    indexers.[](https://www.coinbase.com/fr-fr/blog/part-1-chainstorage-the-enterprise-blockchain-data-availability-layer)

---

### Step 8: Sample Interview Dialogue

**Interviewer**: “How would you design APIs to stream Twitter data for millions of users?”
**You**: “Let’s design a system for real-time Twitter streaming. First, I’d clarify requirements: users need to
subscribe to tweets by user IDs or hashtags, with low-latency delivery and filtering. Non-functionally, we need to
handle ~6,000 tweets/second and millions of connections, with strong security.

I propose a microservices architecture: an Ingestion Service to receive tweets, a Processing Service for filtering, a
Storage Service for persistence, and a Streaming Service for WebSocket delivery. Tweets flow through Kafka for
decoupling, processed by Spark for filtering, and pushed via Redis pub/sub to clients. The API includes a REST endpoint
to create subscriptions and a WebSocket for streaming.

For scalability, we’ll partition Kafka by user ID and scale the Streaming Service horizontally. Security is handled via
OAuth and rate limiting in an API Gateway. Edge cases like celebrity tweet spikes are mitigated by precomputing filtered
streams.”

**Interviewer**: “What if a client disconnects?”
**You**: “We store the client’s `session_id` and filters in Redis. On reconnect, the client uses `session_id` to resume.
We buffer 30 seconds of recent tweets in Redis to replay, ensuring no data loss during brief disconnects.”

**Interviewer**: “How do you handle high tweet volumes?”
**You**: “For spikes, we precompute streams for popular hashtags in the Processing Layer and use Redis pub/sub to fan
out to subscribers. This reduces latency compared to per-client filtering. We also autoscale Spark workers and monitor
throughput with Prometheus.”

---

### Step 9: Optional Enhancements

If time allows or the interviewer pushes for advanced features:

- **Machine Learning**: Add a service to score tweets for relevance (e.g., using LLMs), inspired by Coinbase’s ML use
  cases.[](https://www.datainterview.com/blog/coinbase-machine-learning-engineer-interview)
- **Geo-Distributed Delivery**: Use CDN edge servers to reduce latency for global
  clients.[](https://www.designgurus.io/answers/detail/what-are-the-top-system-design-interview-questions-for-coinbase-interview)
- **Analytics**: Track streaming metrics (e.g., tweets delivered per second) in a separate analytics pipeline.

---

This design balances Coinbase’s expectations of practical, scalable, and secure systems with Chainstack’s focus on
real-time, extensible data streaming. Let me know if you’d like to dive deeper into any component, simulate more
interview questions, or explore a different angle (e.g., coding a sample API handler)!