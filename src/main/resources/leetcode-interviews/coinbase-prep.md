Below is a focused “prep kit” that should hit ≈ 90 % of what typically shows up when a data-oriented backend team (
Golang + Spark + Temporal) gives LeetCode-style interviews. I’ve grouped problems by *concept*, mapped each to one or
two canonical LeetCode IDs, and noted why the topic matters for Coinbase’s Chain Stack work. Do them in rough order;
star-marked ones (★) are the minimum set if you’re short on time.

---

## 1. Data-stream & large-scale aggregation

| Concept                                                | Key Tricks                              | LeetCode IDs                                         |
|--------------------------------------------------------|-----------------------------------------|------------------------------------------------------|
| ★ Running median / percentile over an unbounded stream | Two heaps (min/max), running counts     | 295. Find Median from Data Stream                    |
| Heavy hitters / top-K in a stream                      | Min-heap of size K, or Count-Min + heap | 347. Top K Frequent Elements, 973. K Closest Points  |
| ★ Reservoir sampling                                   | Randomized O(1) mem                     | 382. Linked List Random Node, 398. Random Pick Index |
| Sliding-window aggregates (max, distinct, sum)         | Deque or hash + two-pointers            | 239, 340, 209                                        |
| Bloom / HyperLogLog sketch design (talk-through)       | Bit-array math, hashing                 | “Design a URL-visited checker” variant               |

*Why?*  Chain Stack pipelines firehose L1/L2 data; knowing heap + sketch patterns shows you can summarize tera-scale
feeds.

---

## 2. Graphs, DAGs & scheduling (Temporal-ish)

| Concept                                        | Key Tricks                                      | LeetCode IDs                          |
|------------------------------------------------|-------------------------------------------------|---------------------------------------|
| ★ Topological sort / DAG verification          | Kahn’s queue or DFS coloring                    | 207. Course Schedule, 210             |
| Critical path / longest path in DAG            | DP after topo sort                              | 1490. Clone DAG + weight mod          |
| Detect cycles in dependency graph with pruning | Union-Find or DFS stack                         | 684, 685                              |
| Task-retry with back-off (design)              | Exponential delay, idempotency, cron-like rRule | “Design Temporal workflow retry” mock |

*Why?*  Temporal stores workflows as DAGs; you’ll be asked how to guarantee ordering & retries.

---

## 3. Hashing, maps & constant-time lookups

| Concept                                        | Key Tricks                                     | LeetCode IDs                   |
|------------------------------------------------|------------------------------------------------|--------------------------------|
| ★ LRU / LFU cache                              | Doubly-linked list + dict, or min-freq buckets | 146 (LRU), 460 (LFU)           |
| Consistent hashing ring (theory + simple code) | Sort keys, virtual nodes                       | “Design TinyURL shard” variant |
| Two-sum / n-sum patterns                       | Hash map of complements                        | 1, 15, 18                      |

*Why?*  Sharding on block-height or chain-id plus hot-key eviction is common in chain warehousing.

---

## 4. Strings & parsing

| Concept                                     | Key Tricks        | LeetCode IDs                       |
|---------------------------------------------|-------------------|------------------------------------|
| ★ Valid parentheses & “edit stack” problems | Stack             | 20, 1249                           |
| Trie for prefix lookups / autocomplete      | Node map children | 208, 211                           |
| Streaming log parser (regex + FSM)          | DFA states        | 468. Validate IP, 65. Valid Number |

---

## 5. Arrays, sorting, two pointers

| Concept                                 | Key Tricks               | LeetCode IDs     |
|-----------------------------------------|--------------------------|------------------|
| ★ Merge K sorted lists (external merge) | Min-heap, pairwise merge | 23               |
| In-place rotate / cyclic replacement    | Mod-indexing             | 189              |
| Partition by pivot (Quickselect)        | Hoare/Lomuto             | 215. Kth Largest |

---

## 6. Heap, priority queue & interval handling

| Concept                              | Key Tricks       | LeetCode IDs |
|--------------------------------------|------------------|--------------|
| ★ Merge intervals / calendar booking | Sort + sweep     | 56, 253      |
| Median maintenance (see §1)          | Two-heap balance | 295          |
| Dijkstra on weighted graph           | Min-heap relax   | 743, 787     |

---

## 7. Concurrency & Go-specific patterns

You might get one “toy concurrency” exercise instead of a classic LeetCode question. Practice these in pure Go:

| Problem                                     | Pattern                                        |
|---------------------------------------------|------------------------------------------------|
| ★ “FooBar alternately prints FooBarFooBar…” | Two goroutines, buffered channels or sync.Cond |
| Alternating odd/even numbers print          | Mutex + condition or channels                  |
| H2O formation / semaphore                   | Counting semaphores via buffered channels      |
| Dining philosophers variant                 | Context, timed lock acquisition                |

Implement each once using **only** the standard library (`sync`, `context`, `time`).

---

## 8. Hard tier picks (for up-leveling)

| Topic                                                     | LeetCode ID | Why it’s worth one extra rep |
|-----------------------------------------------------------|-------------|------------------------------|
| Minimum cost to connect all points (Kruskal + Union-Find) | 1584        | Rare but shows MST knowledge |
| Maximum subarray sum in K-concat array (Kadane + math)    | 1191        | Tests linear scan math       |
| Serialize/deserialize N-ary tree                          | 428         | Data marshaling logic        |
| Finding the maximum rectangle in a histogram              | 84          | Stack trick                  |

---

### Practice cadence

| Day | Focus                             | Reps                                 |
|-----|-----------------------------------|--------------------------------------|
| 1   | Arrays / Strings warm-up          | 6 easy + 4 medium                    |
| 2   | Hash maps & heap                  | 6 medium                             |
| 3   | Graphs & DAG                      | 4 medium + 1 hard                    |
| 4   | Streaming & sketches              | 3 med + 2 design drills              |
| 5   | Concurrency in Go                 | Implement 4 patterns, add unit tests |
| 6   | Mock interview (self-record)      | One medium + one hard, 35 min each   |
| 7   | Review misses, repeat hardest set | –                                    |

---

### How to *show* Go expertise

* **Idiomatic code**: `err != nil` early returns, table-driven tests, small helper types.
* **Generics** for common DS (e.g., `type Heap[T constraints.Ordered]`).
* **Benchmark** critical methods with `go test -bench=.`, mention p99 latencies.
* **Race detector**: compile with `-race` on concurrency snippets.

---

## Final tips

1. **Hand-roll utilities once** (heap, Union-Find, Trie). Paste-ready muscle memory matters.
2. **Explain trade-offs out loud**: “I chose heap + hash in O(N log K) because the feed can spike to 100 M tx/s;
   alternatives are …”
3. **Bring domain color**: when the interviewer says “nodes,” think *transactions* or *ledger accounts*—then map graph
   theory back to that.
4. **Timebox & iterate**: if stuck at 10 min, propose brute-force, state complexity, then optimize.

Work this list end-to-end and you’ll have touched the canonical playbook that most Coinbase data-platform interviewers
pull from. Good luck—happy grinding!
