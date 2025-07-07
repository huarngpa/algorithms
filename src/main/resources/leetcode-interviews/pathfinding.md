Below is a compact **“cheat sheet + memory palace”** that lets you (1) decide *which* shortest-path algorithm fits a
problem, and (2) re-derive its core loop from first principles when the whiteboard is blank.

---

## 1  Decision checklist

| Question                                                  | Yes →                          | No →                   |
|-----------------------------------------------------------|--------------------------------|------------------------|
| **Only need *source ➜ all-others*?**                      | Dijkstra, Bellman–Ford         | Floyd–Warshall         |
| **Any negative-weight edges?**                            | Bellman–Ford (or BF + SPFA)    | Dijkstra               |
| **Need to detect negative cycles?**                       | Bellman–Ford                   | Dijkstra               |
| **Dense graph (*V²* edges) or need *all-pairs* quickly?** | Floyd–Warshall                 | Dijkstra/BF per source |
| **Edge weights all 1?**                                   | BFS (special case of Dijkstra) | —                      |

---

## 2  Core ideas & “how to rebuild it”

### 2.1  Dijkstra — *Greedy wavefront*

> **Mantra:** *“Always lock in the cheapest unreached vertex next.”*

1. **Data structure**: min-priority queue keyed by current best distance.
2. **Loop**

   ```
   while PQ not empty:
       (d,u) = PQ.extractMin()   # ‘settle’ u
       for (u → v, weight w):
           if d + w < dist[v]:    # RELAX
               dist[v] = d + w
               PQ.decreaseKey(v)
   ```
3. **Why it works**: All weights are non-negative ⇒ the first time a vertex leaves the queue we are sure no cheaper path
   can appear later.
4. **How to remember**: “*D*ijkstra = *D*ecreaseKey in a heap.”

> **Complexity:** `O((V + E) log V)` with binary heap; `O(V²)` with simple array (easy to code from scratch).

---

### 2.2  Bellman–Ford — *Edge relaxation on repeat*

> **Mantra:** *“Relax every edge V-1 times; changes on round V mean a negative cycle.”*

1. **Initialization**: `dist[src] = 0`, others ∞.
2. **Repeat V-1 times**

   ```
   for each edge (u → v, w):
       if dist[u] + w < dist[v]:
           dist[v] = dist[u] + w
   ```
3. **Cycle check**: One extra pass; if any distance still improves ⇒ negative cycle reachable from `src`.
4. **How to remember**: “*Bell* rings V-1 times; the V-th ring is the *alarm* for negative cycles.”

> **Complexity:** `O(VE)` — slower than Dijkstra but bullet-proof against negatives.

---

### 2.3  Floyd–Warshall — *Dynamic-programming over intermediate nodes*

> **Mantra:** *“Is a cheaper route allowed if I use only the first k vertices as layovers?”*

1. **Table**: `dist[i][j]` (initialized with edge weights or ∞; 0 on diagonal).
2. **Triple loop**

   ```
   for k in 1..V:
       for i in 1..V:
           for j in 1..V:
               dist[i][j] = min(dist[i][j],
                                dist[i][k] + dist[k][j])
   ```

   – `k` is the *hub* we now allow; `i` and `j` are endpoints.
3. **How to remember**: “FW = **F**or **W**ards 3-nested loops ‘k-i-j’.”

> **Complexity:** `O(V³)` time, `O(V²)` memory; elegant and tiny to write.

---

## 3  Mental mnemonics

| Algorithm          | One-liner                                   | Picture in your head                                                        |
|--------------------|---------------------------------------------|-----------------------------------------------------------------------------|
| **Dijkstra**       | Greedy frontier grows outward like ripples. | Pebble dropped in a pond, each ring settled once.                           |
| **Bellman–Ford**   | Keep *relaxing* until nothing changes.      | Squeezing a sponge repeatedly; water (= extra slack) drains away each pass. |
| **Floyd–Warshall** | Try every node as a layover.                | Airline routing table: add one airport row by row.                          |

---

## 4  Edge cases cheat list

* **Negative edge but no negative *cycle*** → Bellman–Ford succeeds, Dijkstra fails.
* **Graph with ≤ 10² vertices but dense** → Floyd–Warshall is often faster in practice.
* **Unit weights, huge sparse graph** → BFS `O(V+E)` beats fancy methods.
* **Need path reconstruction** → store a `parent` (Dijkstra, BF) or `next[i][j]` (FW) pointer during relaxation.

---

## 5  20-second “rebuild” drill

1. **Write “RELAX(u,v)”** on the board: if `d[u]+w < d[v]` then update.
2. **Ask:** *“Do I need a queue or full pass?”*

* If non-negative weights → need quickest “next min”: use PQ ⇒ Dijkstra.
* If negatives allowed → brute-force passes until stable ⇒ Bellman–Ford.

3. **Need all-pairs?** Wrap relaxation in outer loop over `k` ⇒ Floyd–Warshall.

Practise this flow a few times and the muscle memory sticks.

---

### Quick reference table

| Algo           | Handles negative edges? | Detects negative **cycles**? | Complexity   | Best for                                         |
|----------------|-------------------------|------------------------------|--------------|--------------------------------------------------|
| Dijkstra       | ❌                       | ❌                            | `O(E log V)` | Large sparse, non-negative weights               |
| Bellman–Ford   | ✅                       | ✅                            | `O(VE)`      | Sparse with possible negatives, need cycle check |
| Floyd–Warshall | ✅                       | ✅                            | `O(V³)`      | Dense or *all-pairs* up to \~500 nodes           |

---

**Practice tip:** Re-implement each from scratch in < 10 minutes using the mnemonics above; then test on a tiny graph
with a negative edge to cement when each succeeds or fails.

Good luck—once the “relax-repeat-or-queue” pattern is second nature, these algorithms become easy recall in any
interview.
