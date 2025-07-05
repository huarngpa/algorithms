Below is a step-by-step, intuition-first tour of reservoir sampling, moving from the simplest form to the more
sophisticated “skip-ahead” variants that data-engineers reach for when streams are huge and time is precious.

---

## 1. The core problem

> **Goal:** From an *unbounded* or unknown-length stream of items, keep **k** samples so that **every element that has
appeared so far is equally likely (probability = k / n) to be in the reservoir**, where **n** is the number of elements
> seen to date.

The obvious “store everything, sample later” solution costs **O(n)** memory—impossible when the stream is larger than
RAM or even infinite. Reservoir sampling achieves the same statistical guarantee with **O(k)** memory.

---

## 2. Algorithm R (a.k.a. “the textbook” or “simple reservoir”)

### 2.1  k = 1 (single sample)

1. Read the first element, put it in the reservoir.
2. For the *i-th* element (1-based indexing) where **i ≥ 2**:

* Replace the current reservoir element with probability **1 / i**.
* Otherwise keep the old one.

#### Why it works (intuition)

* Every element keeps getting “a ticket” for the single reservoir slot that costs exactly the same: a 1 / i chance when
  it arrives **and it survives every later round**.
* Inductive proof sketch:

  * Base case i = 1: probability is 1.
  * Induction step: Assume each earlier element’s probability is 1 / (i − 1).

    * It is *kept* at step i with probability (1 − 1 / i).
    * So new probability = (1 / (i − 1)) × (1 − 1 / i) = 1 / i.

### 2.2  General k

```python
reservoir = []                          # size k
for i, item in enumerate(stream, 1):    # 1-based counter
    if i <= k:
        reservoir.append(item)          # fill first k
    else:
        j = random.randint(1, i)        # inclusive
        if j <= k:
            reservoir[j-1] = item       # replace
```

*Time*: **O(n)** random draws and comparisons.
*Space*: **O(k)**.

---

## 3. Why we need something faster

When n ≫ k (think billions of sensor readings per hour) Algorithm R still touches every element. That means:

* **CPU**: a branch and often a random-number generation per record.
* **Random-number generator (RNG) throughput**: calling `rand()` billions of times can dwarf I/O.

Skip-ahead algorithms reduce the *number of candidates* the reservoir has to inspect.

---

## 4. Vitter’s family of algorithms (skip counting)

| Variant | Expected items touched      | RNG calls per item | Memory | Notes                               |
|---------|-----------------------------|--------------------|--------|-------------------------------------|
| **R**   | n                           | 1                  | k      | Simple, no skips                    |
| **X**   | n − k                       | \~1                | k      | Cheap first fill, same asymptotics  |
| **L**   | n − k                       | 1                  | k      | Streams large, uses 32-bit math     |
| **Z**   | n × (k / (n + 1)) ≈ n·(k/n) | α < 1              | k      | Best for *huge* n; logarithmic skip |

(*Touch = “produce a random number and compare once”.*)

### 4.1  High-level idea

Instead of inspecting every record, compute **how many items we can safely skip** before the next replacement is even
possible.

* The gap size is drawn from a **geometric** (or exponential for large i) distribution derived from the replacement
  probability.
* We fast-forward i by that many elements, read the next candidate, and perform exactly one replacement test.

### 4.2  Sketch of Algorithm L (k ≥ 1)

1. **Fill the reservoir** with the first k elements. Set **i = k**.
2. While the stream continues:

1. Generate a random `u ∈ (0,1)`.
2. Compute the **skip length**: `s = ⌊log(u) / log(1 − k / i)⌋`.
3. Advance the stream by `s` items (discard). Set `i += s`.
4. Read the next item (now i + 1). Pick a random reservoir slot to replace.

The algebra ensures that, after skipping, the conditional probability of replacement stays correct. Complexity drops to
**O(n · k / (n + 1))** replacements; when k ≪ n, this is close to *O(k log(n/k))*.

*(Algorithm Z refines the math further and uses a clever “guard table” to collapse floating-point ops into integer
arithmetic, marginally faster on 64-bit CPUs.)*

---

## 5. Unbounded streaming realities

* **No end-of-stream required**: All reservoir algorithms maintain uniformity *at every prefix*; you can stop at any
  time and still have a fair sample.
* **Checkpointing**: For long-running jobs (Flink/Spark Structured Streaming) persist the reservoir and the running item
  count `i` so you can resume after failure.
* **Concurrency**: If the stream is partitioned, take a reservoir from each partition, then merge them with another
  round of reservoir sampling (“distributed reservoir”).
* **Weighted sampling**: When some items deserve higher probability, switch to *weighted reservoir* (
  Efraimidis–Spirakis): assign each item key `w` a priority `r^{1/w}` where `r ∈ (0,1)`. Keep the k highest priorities.

---

## 6. Interview-ready summary table 🌟

| Fact to remember                | Value                                                                      |
|---------------------------------|----------------------------------------------------------------------------|
| Memory needed                   | **O(k)** for all variants                                                  |
| Simple Algorithm R replace-prob | 1 / i                                                                      |
| Proof trick                     | Induction on **i**                                                         |
| Skip-ahead key distribution     | Geometric (or exponential)                                                 |
| When to prefer Algorithm L/Z    | k ≪ n and RNG cost is material                                             |
| Weighted version cue            | Efraimidis–Spirakis priority = `r^{1/w}`                                   |
| Merge of two reservoirs         | Run Algorithm R treating each item of reservoir B as “next stream element” |

---

### Quick‐check mental model

Think of each incoming element buying **one lottery ticket** whose chance of victory is *exactly equal* to the
incumbent’s. Every algorithm—simple or skip-ahead—just plays the lottery more or less aggressively:

* **R**: Draw a ticket for *every* entrant.
* **L/Z**: Bulk-skip obvious losers, then draw.

Either way the outcome is identical: uniform random selection with tiny, fixed memory.

---

Feel free to ask for:

* runnable code samples in Go or Python (including simulations that verify uniformity),
* adaptations for weighted or time-decayed sampling,
* distributed merge patterns, or
* deeper dives into Vitter’s derivations.

Happy sampling!
