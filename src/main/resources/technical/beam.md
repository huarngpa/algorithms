### Apache Beam vs. Apache Flink — where each fits in a modern streaming stack

| Dimension                           | **Apache Beam**                                                                                                                        | **Apache Flink (runtime)**                                                                                                                                  |
|-------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------|
| *What it is*                        | **SDK + portable execution model.** Think “write once, run on any runner.”                                                             | **Streaming engine** (with its own APIs). Executes jobs, handles state, checkpoints, back-pressure.                                                         |
| *Typical role*                      | **Pipeline authoring & portability.** You write a DAG in Python/Java/Go ➜ choose a runner (Flink, Spark, Dataflow, Samza, etc.).       | **High-performance runtime** you deploy directly (on-prem, K8s, Flink-on-K8s, or managed).                                                                  |
| *Batch + Streaming*                 | True unified semantics (same code on bounded or un-bounded PCollections) ([beam.apache.org][1], [confluent.io][2])                     | Unified since 1.12, but most users treat Flink primarily as a streaming engine.                                                                             |
| *Latency profile*                   | Depends on runner (when Beam runs **on** Flink you inherit Flink’s latencies).                                                         | Sub-second; millis with Event-Time, Watermarks, Exactly-Once.                                                                                               |
| *Language surface*                  | Java, Python, Go; portable—all compile to Beam-Proto graph.                                                                            | Java / Scala / Python (Table/SQL)––exposes the full native API surface.                                                                                     |
| *Advanced state*                    | Supported only if runner supports it (Flink runner = yes; Spark runner = no timers)                                                    | Rich **Keyed State**, RocksDB back-end, Async I/O, FLIP-state API ([ververica.com][3])                                                                      |
| *Portability benefit*               | Multi-cloud hedge; move the same pipeline from Dataflow→Flink→Spark without rewrite.                                                   | Full access to bleeding-edge features (e.g., Flink 2.0 KRaft, unified checkpoint compaction) ([flink.apache.org][4], [ververica.com][3])                    |
| *Where Coinbase-style teams use it* | Quick ML / ETL pipelines that must run both on GCP Dataflow and on internal MSK + Flink clusters; experimentation notebooks in Python. | Latency-critical enrichment (DEX order-book joins, risk windowing) where you need flink-only features (Async I/O, dynamic scaling, new reactive execution). |

---

#### 1. Conceptual layers in a streaming architecture

```
 ┌────────────┐   Beam SDK  (optional)
 │   Author   │   beam.Pipeline(…)  →  Beam DAG (protobuf)
 └────┬───────┘
      ▼
 ┌────────────┐   Beam Runner for Flink
 │ BeamRunner │   Translates DAG → Flink job graph
 └────┬───────┘
      ▼
 ┌────────────┐   Flink JobManager / TaskManagers
 │   Flink    │   Executes, checkpoints, back-pressure control
 └────────────┘
```

*If you skip Beam*, you write the Flink job graph directly with the DataStream / Table API.

---

#### 2. When to reach for **Beam**

* **Portability / vendor hedge** – write once, run on Dataflow during research, move to self-hosted Flink for
  prod ([beam.apache.org][5])
* **Multi-language team** – analytics in Python, prod in Java, shared Beam DSL.
* **“One DAG for batch+stream”** – nightly backfill and real-time feed share code.

*Trade-off* → you wait for the Beam runner to expose the newest Flink goodies; some FLIPs ship months earlier in native
Flink.

---

#### 3. When to reach for **native Flink**

* **Sub-100 ms SLA** – you need low-latency async enrichment, custom state TTL, exactly-once sinks.
* **Complex keyed state** (session windows, CEP, custom timers).
* **Operational control** – reactive scaling, fine-grained checkpoints, state backend tuning, KRaft masterless mode in
  Flink 2.0 ([flink.apache.org][4]).
* **SQL / Table API** – Flink’s latest optimizer, changelog-stream joins, and unified batch planner land here first.

---

#### 4. What senior engineers consider

| Design question     | Beam answer                                                            | Native Flink answer                                                                        |
|---------------------|------------------------------------------------------------------------|--------------------------------------------------------------------------------------------|
| **Upgrade cadence** | Match Beam-runner release; wait until runner supports new Flink major. | You control the Flink version (1.20.1 bug-fix today, 2.0 tomorrow) ([flink.apache.org][6]) |
| **Debugging**       | Two hops (Beam graph + runner logs).                                   | One hop (Flink UI, metrics, savepoints).                                                   |
| **Platform skills** | Data engineers / ML scientists can stay in Beam.                       | Requires deeper understanding of state backend, Watermarks, checkpoints.                   |
| **Feature gaps**    | No Beam timers in Spark runner; no SQL DDL.                            | Full feature surface, but no cross-runner portability.                                     |

---

#### 5. How they coexist inside a Coinbase-like platform

* **Bronze / Silver ETL** → Beam (Python) on GCP Dataflow for rapid iteration.
* Exported to **Iceberg on S3**.
* **Gold real-time enrichment** → Native Flink (Java) on EKS / MSK, leveraging Async I/O to hit on-chain price oracles.
* Both publish to the same Kafka topics; downstream micro-services subscribe via gRPC (exactly-once semantics).

---

### Quick mnemonic

> **Beam = *Write-Once API layer***,
> **Flink = *Real-time execution engine***.

Keep that sentence ready; most interviewers will nod immediately.

[1]: https://beam.apache.org/?utm_source=chatgpt.com "Apache Beam®"

[2]: https://www.confluent.io/learn/apache-beam/?utm_source=chatgpt.com "Apache Beam: Introduction to Batch and Stream Data Processing"

[3]: https://www.ververica.com/blog/announcing-the-release-of-apache-flink-1.20?utm_source=chatgpt.com "Announcing the Release of Apache Flink 1.20 - Ververica"

[4]: https://flink.apache.org/2025/03/24/apache-flink-2.0.0-a-new-era-of-real-time-data-processing/?utm_source=chatgpt.com "Apache Flink 2.0.0: A new Era of Real-Time Data Processing"

[5]: https://beam.apache.org/blog/beam-summit-2024-overview/?utm_source=chatgpt.com "Apache Beam Summit 2024: Unlocking the power of ML for data ..."

[6]: https://flink.apache.org/2025/02/12/apache-flink-1.20.1-release-announcement/?utm_source=chatgpt.com "Apache Flink 1.20.1 Release Announcement"
