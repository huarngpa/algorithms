Below is a **realistic 4-level CodeSignal-style progression** you could see for a Coinbase pair-programming exercise on
*fraud-detection for crypto transactions*.
It mirrors the pattern CodeSignal uses (warm-ups → heavy algorithm) ([Reddit][1]) and the “initial problem + two
progressively harder extensions” format Coinbase has used in recent machine-coding rounds ([Medium][2]).
Each level comes with:

* a short problem spec in CodeSignal’s tone (function signature + I/O)
* the key idea & complexity
* a Go 11 solution sketch (idiomatic, interview-friendly)

---

## Level 1 — Basic Rule-Based Filter (warm-up)

**Problem**

```text
func flagLargeTx(transactions []Transaction, limit int64) []string
```

`Transaction` has `{ID string; User string; Amount int64}`.
Return the `ID`s whose `Amount ≥ limit`.

**Idea** – one pass filter.

*Time / Space*: **O(n)** / **O(f)** (flagged count).

```go
func flagLargeTx(txns []Transaction, limit int64) []string {
    flags := make([]string, 0, len(txns))
    for _, t := range txns {
        if t.Amount >= limit {
            flags = append(flags, t.ID)
        }
    }
    return flags
}
```

*What the interviewer looks for*: clean struct, clear loop, tiny test.

---

## Level 2 — Velocity Check in a Sliding Window

**Problem**

```text
func usersWithBurst(
    txns []Tx, k int, windowSec int64) []string
```

`Tx` now adds `Ts int64` (Unix seconds).
Flag any `User` who submits **`≥ k` transactions within any rolling `windowSec` seconds**.

**Idea**

* Sort by `Ts` asc (if not already).
* Per user, maintain a **deque of timestamps**; push current, pop old (`Ts < current-windowSec`).
* When `len(deque) == k` mark user.

This mirrors velocity-fraud heuristics used in credit-card work .

*Time*: **O(n log n)** if sort; **O(n)** streaming.
*Space*: **O(U · k)**.

```go
func usersWithBurst(txns []Tx, k int, w int64) []string {
    sort.Slice(txns, func(i, j int) bool { return txns[i].Ts < txns[j].Ts })
    dq := map[string][]int64{}
    flagged := map[string]struct{}{}

    for _, t := range txns {
        q := dq[t.User]
        // expire old
        cutoff := t.Ts - w + 1
        for len(q) > 0 && q[0] < cutoff {
            q = q[1:]
        }
        q = append(q, t.Ts)
        dq[t.User] = q
        if len(q) >= k {
            flagged[t.User] = struct{}{}
        }
    }
    ids := make([]string, 0, len(flagged))
    for u := range flagged {
        ids = append(ids, u)
    }
    return ids
}
```

---

## Level 3 — Two-Party “Ping-Pong” / Round-Trip Trades

**Problem**

```text
func suspiciousRoundTrips(txns []Tx3, maxGapSec int64) [][2]string
```

`Tx3` adds `From string; To string; Amount int64`.
Flag **pairs of accounts** `(A,B)` where:

1. `A → B` then `B → A` with *same `Amount`*
2. The second hop happens within `maxGapSec` of the first.

Return unique unordered pairs.

**Idea**

*Hash key*: `(min(A,B), max(A,B), Amount)` ➜ first-hop timestamp & direction.
Scan txns chronologically; when opposite hop arrives inside gap ⇒ suspicious.

*Time*: **O(n)** hash look-ups.
*Space*: **O(#openPairs)** (bounded by active window).

```go
type key struct{ a, b string; amt int64 }

func suspiciousRoundTrips(txns []Tx3, gap int64) [][2]string {
    first := map[key]struct {
        ts  int64
        dir int // 0 = a→b seen
    }{}
    marked := map[[2]string]struct{}{}

    for _, t := range txns {
        a, b := t.From, t.To
        k := key{min(a, b), max(a, b), t.Amount}
        entry, ok := first[k]
        if !ok {
            first[k] = struct{ ts int64; dir int }{t.Ts, boolToInt(a < b)}
            continue
        }
        // opposite direction?
        if entry.dir != boolToInt(a < b) && t.Ts-entry.ts <= gap {
            pair := [2]string{k.a, k.b}
            marked[pair] = struct{}{}
        }
        // update earliest to resist duplicates
        if t.Ts < entry.ts {
            first[k] = struct{ ts int64; dir int }{t.Ts, entry.dir}
        }
    }
    out := make([][2]string, 0, len(marked))
    for p := range marked {
        out = append(out, p)
    }
    return out
}
```

*Why it matters*: quick check for **money-laundering loops** or bid-wash trades with two wallets.

---

## Level 4 — Wash-Trading Cycle Detector (graph + streaming)

**Problem (full Q4 style)**

```text
func detectWashTrades(
    txns []Tx4,
    windowSec int64,
    maxCycle int,
    volThreshold int64,
    epsilon int64) []Cycle
```

`Tx4` adds `Ts, From, To, Amount`.
Within each sliding `windowSec`:

* Build a **directed graph** (`From → To`) with edge weight = sum(Amount).
* Find **SCCs whose size ≤ maxCycle** **AND**
  – total *volume* in component ≥ `volThreshold`
  – **net flow of each node |in-out| ≤ epsilon** (≈ zero)

Return the list of SCCs (each as ordered list of tx IDs).
This is the classic heuristic academics use for on-chain wash-trading detection ([arXiv][3]).

**Algorithm**

1. **Stream ingestion**

* Maintain a min-heap of tx by `Ts` to expire window.
* Incrementally update adjacency & per-node balances.

2. **Every new tx** (or on demand):

* Run **Tarjan / Kosaraju** on current sub-graph constrained by `maxCycle` (small).
* For each SCC meeting volume / balance criteria ⇒ flag cycle.

3. **Complexities**

| item          | worst-case                               |
|---------------|------------------------------------------|
| per-tx update | **O(log n)** (heap)                      |
| cycle scan    | **O(E)** inside window (sparse ≈ linear) |
| memory        | **O(Ewindow)** (only live window)        |

**Go sketch**

```go
// Tarjan strongly-connected components, early exit when size > maxCycle.
// For brevity the helper functions are elided; in an interview implement
// low-link recursion explicitly, track stack & index.

func detectWashTrades(
    txns []Tx4, win int64, mc int, volTh, eps int64) []Cycle {

    type edge struct{ to string; vol int64 }

    // state
    g := map[string][]edge{}
    bal := map[string]int64{}
    heap := txnHeap{} // min-heap by Ts
    res  := []Cycle{}

    for _, t := range txns {
        heapPush(&heap, t)
        insert(g, t.From, t.To, t.Amount)
        bal[t.From] -= t.Amount
        bal[t.To]   += t.Amount

        // expire old
        cutoff := t.Ts - win
        for heap.Len() > 0 && heap[0].Ts < cutoff {
            old := heapPop(&heap)
            remove(g, old.From, old.To, old.Amount)
            bal[old.From] += old.Amount
            bal[old.To]   -= old.Amount
        }

        // cheap heuristic: only run SCC when tx.Amount big
        if t.Amount >= volTh/10 {
            for _, comp := range tarjan(g, mc) {
                if totalVol(comp, g) >= volTh &&
                   isBalanced(comp, bal, eps) {
                    res = append(res, buildCycle(comp))
                }
            }
        }
    }
    return dedup(res)
}
```

*Conversation points*:

* reasoning about **SCC vs. cycles** (SCC cheaper when `mc` small)
* memory culling in streams (rolling window heap)
* trade-offs vs. ML anomaly scores (precision vs. recall).

---

### How to practice on CodeSignal

1. **Mock each level as a standalone “Task 1-4”**.
   – Use their `Run-Tests` button heavily; aim for 100 % unit tests early so you can refactor with confidence.

2. **Time-box** like the real platform:
   *Q1/Q2*: ≤ 5 min each, *Q3*: bulk of coding, *Q4*: algorithm sketch + core loop (acceptable to leave perf tweaks as
   comments).

3. **Narrate while typing** – Coinbase pair sessions are judged partly on collaboration; explain invariants and why a
   map/deque/heap is safe.

4. **Have snippets ready**: priority-queue, deque, Tarjan, unit-test harness.

---

### Extending to production-grade ML

CodeSignal won’t ask you to build a full ML model, but **talking through next steps** adds “senior” polish:

* Offline gradient-boosted or graph-embedding model on historical on-chain data (address graph, features =
  degree-centrality, time-series stats).
* Combine with **heuristic flags** above as model features ➜ boost recall.
* Handle concept-drift; retrain weekly; champion/challenger.
* A/B the fraud-score threshold to balance FP cost vs. FN risk.

These tie back to published research on graph-based crypto fraud detection and wash-trading heuristics ([arXiv][3]).

Good luck—master the mechanics, then focus on *communicating* each trade-off clearly.

[1]: https://www.reddit.com/r/cscareerquestions/comments/xhw4ly/just_took_my_first_codesignal_assessment/ "Just took my first CodeSignal Assessment : r/cscareerquestions"

[2]: https://medium.com/%40varshareddyarrabelli2003/coinbase-interview-experience-backend-3a523d97e15f "Coinbase Interview Experience — Software Engineer Backend (G2501) | by Varsha Reddy | Medium"

[3]: https://arxiv.org/html/2403.10361v1 "Unveiling Wash Trading in Popular NFT Markets"


Below is a **Java-flavoured rewrite** of the four CodeSignal-style tasks.
I keep the same progression—warm-up filter → sliding-window velocity → two-hop round-trip → wash-trading cycle
detector—but every snippet is now idiomatic Java 21 (no external libs, just `java.util`). Feel free to fold them into a
single `Solution` class or separate files as CodeSignal prefers.

---

## Level 1 — Large-Amount Rule

### Spec

```java
/**
 * @param txns   list of transactions (id, user, amount)
 * @param limit  flag if amount ≥ limit
 * @return ids of flagged transactions in original order
 */
static List<String> flagLargeTx(List<Transaction> txns, long limit)
```

### Idea & Complexity

Single pass, **O(n)** time, **O(f)** extra space (f = #flags).

### Code

```java
record Transaction(String id, String user, long amount) {
}

static List<String> flagLargeTx(List<Transaction> txns, long limit) {
    List<String> flagged = new ArrayList<>();
    for (Transaction t : txns) {
        if (t.amount() >= limit) flagged.add(t.id());
    }
    return flagged;
}
```

---

## Level 2 — Burst Velocity Check (Sliding Window)

### Spec

```java
/**
 * @param txns        list (id, user, amount, ts)
 * @param k           #tx threshold inside window
 * @param windowSecs  rolling window in seconds
 * @return users with ≥ k tx in any windowSecs interval
 */
static Set<String> usersWithBurst(List<Tx> txns, int k, long windowSecs)
```

### Idea

Per user keep a deque of timestamps; on each new tx pop expired (`ts < current - windowSecs + 1`), push current, test
size. If input isn’t time-sorted, sort first.

*Time*: **O(n log n)** with sort (CodeSignal gives you an unsorted array).
*Space*: **O(U · k)** (U = users).

### Code

```java
record Tx(String id, String user, long amount, long ts) {
}

static Set<String> usersWithBurst(List<Tx> txns, int k, long windowSecs) {
    txns.sort(Comparator.comparingLong(Tx::ts));      // if already sorted, skip
    Map<String, ArrayDeque<Long>> buckets = new HashMap<>();
    Set<String> flagged = new HashSet<>();

    for (Tx t : txns) {
        ArrayDeque<Long> dq = buckets.computeIfAbsent(t.user(), __ -> new ArrayDeque<>());
        long cutoff = t.ts() - windowSecs + 1;
        while (!dq.isEmpty() && dq.peekFirst() < cutoff) dq.pollFirst();
        dq.addLast(t.ts());
        if (dq.size() >= k) flagged.add(t.user());
    }
    return flagged;
}
```

---

## Level 3 — Two-Hop Round-Trip (Ping-Pong)

### Spec

```java
/**
 * Flag unordered pairs (A,B) where a transfer A→B followed by B→A
 * with identical amount occurs within maxGap.
 *
 * @return list of unique pairs in any order
 */
static List<Pair<String, String>> suspiciousRoundTrips(
        List<Tx3> txns, long maxGapSecs)
```

### Idea

Hash key `(min(A,B), max(A,B), amount)` ↦ first-hop (ts, dir).
On second hop, check opposite direction and Δt ≤ maxGapSecs.

*Time*: **O(n)**, *Space*: **O(active pairs)**.

### Code

```java
record Tx3(String id, String from, String to, long amount, long ts) {
}

record Pair<A, B>(A first, B second) {
}

static List<Pair<String, String>> suspiciousRoundTrips(
        List<Tx3> txns, long maxGapSecs) {

    record Key(String low, String high, long amt) {
    }
    record Seen(long ts, boolean dirLowToHigh) {
    }

    Map<Key, Seen> firstSeen = new HashMap<>();
    Set<Pair<String, String>> flagged = new HashSet<>();

    for (Tx3 t : txns) {
        String a = t.from(), b = t.to();
        boolean lowToHigh = a.compareTo(b) < 0;
        Key k = new Key(lowToHigh ? a : b, lowToHigh ? b : a, t.amount());
        Seen prev = firstSeen.get(k);

        if (prev == null) {
            firstSeen.put(k, new Seen(t.ts(), lowToHigh));
        } else if (prev.dirLowToHigh() != lowToHigh &&
                t.ts() - prev.ts() <= maxGapSecs) {
            flagged.add(new Pair<>(k.low, k.high));
        }
        // keep earliest ts only
        if (prev == null || t.ts() < prev.ts()) {
            firstSeen.put(k, new Seen(t.ts(), lowToHigh));
        }
    }
    return new ArrayList<>(flagged);
}
```

---

## Level 4 — Wash-Trading Cycles (Streaming SCC)

### Spec (headline)

```java
/**
 * Within a rolling window build a directed graph (wallet ➜ wallet, weight = Σ amount).
 * Return SCCs of size ≤ maxCycle whose total volume ≥ volThreshold AND
 * each node is almost balanced (|in-out| ≤ epsilon).
 */
static List<Set<String>> detectWashTrades(
        List<Tx4> txns,
        long windowSecs,
        int maxCycle,
        long volThreshold,
        long epsilon)
```

### High-Level Algorithm

1. **Window maintenance**
   *Min-heap* keyed by `ts` to expire edges older than `(latestTs - windowSecs)`.
   Incrementally maintain:

* `Map<String, Map<String, Long>> adj` – weighted adjacency
* `Map<String, Long> balance` – net in-minus-out per wallet.

2. **Tarjan (or Kosaraju) SCC**
   Run after each “interesting” insertion (e.g., big amount) but **early-exit** recursion if component grows beyond
   `maxCycle`.

3. **Filter**
   For each SCC `C` that survives size bound:

* volume = Σ edge weights inside `C`
* balanced = ∀ v∈C, `|balance[v]| ≤ epsilon`
  If both true and `volume ≥ volThreshold`, emit.

4. **Complexities**
   Window maintenance: **O(log n)** per tx (heap push/pop).
   SCC scan (sparse): **O(E\_w)** where `E_w` edges live in window.
   Memory: **O(E\_w)**.

### Code Skeleton

(Full implementation is long; show the core pieces you’d type under pressure. Comment the rest—you only need a working
subset to pass CodeSignal cases.)

```java
record Tx4(String id, String from, String to, long amount, long ts) {
}

static List<Set<String>> detectWashTrades(
        List<Tx4> txns,
        long windowSecs,
        int maxCycle,
        long volThreshold,
        long epsilon) {

    // --- live window state ---
    Map<String, Map<String, Long>> adj = new HashMap<>();
    Map<String, Long> balance = new HashMap<>();
    PriorityQueue<Tx4> heap = new PriorityQueue<>(Comparator.comparingLong(Tx4::ts));
    List<Set<String>> suspicious = new ArrayList<>();

    for (Tx4 tx : txns) {
        // 1) ingest
        heap.add(tx);
        adj.computeIfAbsent(tx.from(), __ -> new HashMap<>())
                .merge(tx.to(), tx.amount(), Long::sum);
        balance.merge(tx.from(), -tx.amount(), Long::sum);
        balance.merge(tx.to(), tx.amount(), Long::sum);

        // 2) expire old
        long cutoff = tx.ts() - windowSecs;
        while (!heap.isEmpty() && heap.peek().ts() < cutoff) {
            Tx4 old = heap.poll();
            Map<String, Long> m = adj.get(old.from());
            if (m != null) {
                m.computeIfPresent(old.to(), (__, v) -> (v == old.amount ? null : v - old.amount));
                if (m.isEmpty()) adj.remove(old.from());
            }
            balance.merge(old.from(), old.amount(), Long::sum);
            balance.merge(old.to(), -old.amount(), Long::sum);
        }

        // 3) heuristic: only run SCC when tx is "big"
        if (tx.amount() >= volThreshold / 10) {
            // Tarjan SCC
            List<Set<String>> comps = tarjan(adj, maxCycle);
            for (Set<String> comp : comps) {
                if (totalVolume(comp, adj) >= volThreshold &&
                        isBalanced(comp, balance, epsilon)) {
                    suspicious.add(comp);
                }
            }
        }
    }
    return suspicious;
}

/* ---------- helpers: tarjan, totalVolume, isBalanced ---------- */

/** Tarjan with size cap; returns all SCCs with size ≤ maxCycle. */
private static List<Set<String>> tarjan(
        Map<String, Map<String, Long>> graph, int maxCycle) {

    Map<String, Integer> idx = new HashMap<>();
    Map<String, Integer> low = new HashMap<>();
    Deque<String> stack = new ArrayDeque<>();
    List<Set<String>> sccs = new ArrayList<>();
    int[] time = {0};

    for (String v : graph.keySet()) {
        if (!idx.containsKey(v))
            dfsTarjan(v, graph, maxCycle, idx, low, stack, sccs, time);
    }
    return sccs;
}

private static void dfsTarjan(
        String v,
        Map<String, Map<String, Long>> g,
        int maxCycle,
        Map<String, Integer> idx,
        Map<String, Integer> low,
        Deque<String> stack,
        List<Set<String>> sccs,
        int[] time) {

    idx.put(v, time[0]);
    low.put(v, time[0]++);
    stack.push(v);

    for (String w : g.getOrDefault(v, Map.of()).keySet()) {
        if (!idx.containsKey(w)) {
            dfsTarjan(w, g, maxCycle, idx, low, stack, sccs, time);
            low.put(v, Math.min(low.get(v), low.get(w)));
        } else if (stack.contains(w)) {
            low.put(v, Math.min(low.get(v), idx.get(w)));
        }
    }

    // root?
    if (low.get(v).equals(idx.get(v))) {
        Set<String> comp = new HashSet<>();
        String w;
        do {
            w = stack.pop();
            comp.add(w);
        } while (!w.equals(v) && !stack.isEmpty());

        if (comp.size() <= maxCycle && comp.size() > 1) {
            sccs.add(comp);
        }
    }
}

private static long totalVolume(Set<String> comp,
                                Map<String, Map<String, Long>> adj) {
    long sum = 0L;
    for (String u : comp)
        for (Map.Entry<String, Long> e : adj.getOrDefault(u, Map.of()).entrySet())
            if (comp.contains(e.getKey())) sum += e.getValue();
    return sum;
}

private static boolean isBalanced(Set<String> comp,
                                  Map<String, Long> balance,
                                  long eps) {
    for (String v : comp)
        if (Math.abs(balance.getOrDefault(v, 0L)) > eps) return false;
    return true;
}
```

> **Interview guidance**: explain *why* you cap SCC size (early exit), how you decrement edge weights on expiry, and
> where you might micro-optimise (e.g., bitmap visited sets, cache totalVolume). It demonstrates senior-level thought even
> if you don’t finish every corner case under the timer.

---

### Practising Tips

1. **Paste each level into CodeSignal** using `public class Solution` wrappers and minimal I/O stubs (`Scanner` or
   hard-coded tests) to hit the green bars quickly.
2. **Narrate while coding** in a pair session: call out invariants—e.g., *“deque always time-ascending; we never let it
   grow past k unexpired elements”*.
3. Have snippets for:

* `ArrayDeque<Long>` sliding window
* `PriorityQueue` min-heap expiration
* Tarjan’s SCC template (above)

4. Once you finish the functional requirement, discuss **next steps**: feature-engineering, model-driven fraud scores,
   batch vs. streaming, observability metrics, etc.—that’s the “senior” polish Coinbase likes.

Good luck—happy coding!
