Below is a field-engineer-style crash course that takes you from *paper knowledge* to *ETL-ready* for both Bitcoin (
UTXO) and Ethereum-family (account) chains. I group the content exactly the way a Coinbase “Chain Stack” interview often
flows: theory → node access → streaming patterns → production data model → tool stack.

---

## 0 · One-screen cheat sheet (memorise)

| Axis                   | Bitcoin                                         | Ethereum (post-Merge)                                                                   |
|------------------------|-------------------------------------------------|-----------------------------------------------------------------------------------------|
| Ledger model           | **UTXO**                                        | **Account/contract** state trie                                                         |
| Canonicality           | 6 blocks ≈ \~1 hr                               | 2-3 epochs (≈ 64-96 slots, 12–19 min)                                                   |
| Node types             | pruned • full • **archive (rare)**              | light • full • **archive (common for analytics)**                                       |
| Real-time feed         | ZeroMQ (`zmqpubhashblock`, `zmqpubrawtx`)       | WebSocket `eth_subscribe` (`newHeads`, `logs`, `newPendingTx`)                          |
| Typical raw I/O        | `getblock`, `getrawtransaction`, `scantxoutset` | `eth_getBlockByNumber`, `eth_getLogs`, `debug_traceTransaction`                         |
| ETL gotchas            | Re-orgs, replace-by-fee (RBF) mempool churn     | Re-orgs, internal calls, chain splits on L2s                                            |
| Storage size (archive) | \~ 600 GB                                       | **Geth** ≈ 15 TB, **Erigon** ≈ 3 - 4 TB ([docs.polygon.technology][1], [reddit.com][2]) |

---

## 1 · Working mental model

1. **Block = header + body**
   *Header* has the hash, parent, root(s) (state & tx for ETH), difficulty/total work, timestamp.
   *Body* is transactions (plus receipts & ommers/uncles for ETH).

2. **Consensus ≠ data interface.**
   You rarely touch PoW/PoS internals in ETL, but you *must* plan for *fork choice*: store `<height, hash>` pairs, never
   height alone.

3. **Merkle proofs matter for verifiability** (they also show up in interview questions).
   Bitcoin uses a single Merkle tree of tx IDs; Ethereum has **Patricia-Merkle tries** for state, storage and receipts.

---

## 2 · Spinning up & talking to nodes

### Bitcoin Core

```bash
# Full node with raw-tx index & ZMQ enabled
bitcoind -txindex=1 -zmqpubrawblock=tcp://0.0.0.0:28332 \
         -zmqpubrawtx=tcp://0.0.0.0:28333
```

* **RPC** (`bitcoin-cli getblock <hash> 2`) – deterministic, but pull-based.
* **ZeroMQ** – fire-and-forget Pub/Sub for `rawblock`, `rawtx` streams. Use it for <1 sec latency
  feeds.  ([bitcoin.stackexchange.com][3], [stackoverflow.com][4])

Libraries: `btcsuite/btcd/rpcclient` (Go), `bitcoinrpc` (Python), `rust-bitcoincore-rpc`.

### Ethereum-family (Geth / Nethermind / Erigon / Besu)

```bash
geth --ws --ws.api eth,net,debug,txpool,erigon \
     --syncmode=full            # or --snapshot for archive
```

* **JSON-RPC/HTTP** – good for random look-ups, *not* for streaming.

* **WebSocket** – use `eth_subscribe`. Key channels:

  ```jsonc
  {
    "id": 1,
    "method": "eth_subscribe",
    "params": ["newHeads"]
  }
  ```

  Sends a header every block.  ([docs.chainstack.com][5], [docs.metamask.io][6])

* **Archive vs full** – only an *archive* node lets you query historical state (`eth_getBalance` at block N). Erigon
  compresses to \~3-4 TB and syncs faster. ([docs.polygon.technology][1], [reddit.com][2])

Go snippet:

```go
client, _ := ethclient.Dial("wss://mainnet.infura.io/ws/v3/<api-key>")
headers := make(chan *types.Header)
sub, _ := client.SubscribeNewHead(context.Background(), headers)
for h := range headers {
    fmt.Printf("Block #%d – %s\n", h.Number.Uint64(), h.Hash().Hex())
}
```

#### Third-party hosted RPC

| Provider | Notes |
|----------|-------|
| **Infura / Alchemy / QuickNode** | Battle-tested, pay-per-request; great for PoCs |
| **Chainstack** | Multi-cloud, archive options, good docs for `eth_subscribe` |
| **BlockPi / Blast / Ankr** | Cheaper tiers, variable reliability |

---

## 3 · Streaming & ETL patterns

### 3.1 Real-time ingest

````

```
  ┌─────────┐  websocket   ┌────────┐   protobuf   ┌──────────┐
  │  Node   │─────────────▶│ Parser │─────────────▶│ Kafka/KDS │
  └─────────┘              └────────┘              └──────────┘
                                                  ▲    ▲
                                                  │    │
                       Spark Structured Streaming │    │ Flink / Beam (late/dup handling)
```

```

1. **Deduplicate & canonicalize**: keep `<height, hash>` and ignore if parent mismatch.  
2. **Re-org buffer**: hold e.g. last N=25 blocks in memory; if a fork comes, retract (tombstone) old rows.  
3. **Watermarks**: event-time = block timestamp; allowedLateness ≈ 2 min BTC, 15 sec ETH-PoS.  

### 3.2 Historical backfill

* **Range scan**: walk blocks 0 → tip, checkpoint every 10 k.  
* **Firehose/Substreams** – pre-decoded flat files (one gRPC stream per chain) – 50-100× faster than RPC drenches.  :contentReference[oaicite:5]{index=5}  
* **Community dumps**: Google BigQuery public crypto datasets (ETH, BTC, BSC, Polygon).

---

## 4 · Canonical data model (minimum viable lake)

| Table | Key columns |
|-------|-------------|
| `blocks` | height, hash, parent_hash, ts, gas_used, miner |
| `transactions` | tx_hash, block_height, from, to, value, fee, status |
| `logs` (ETH) | tx_hash, idx, address, topics[4], data |
| `token_transfers` | tx_hash, log_idx, from, to, token_addr, amount |
| `trace_calls` (ETH trace) | tx_hash, depth, type, error, gas_cost |
| `utxo` (BTC) | tx_hash, vout, address, satoshis, spent_by |

Partition by `block_date=YYYY-MM-DD` or height buckets of 100k; store as **Parquet or Iceberg** for Spark.

---

## 5 · Tool & library ecosystem map

| Purpose | Bitcoin | Ethereum / EVM |
|---------|---------|----------------|
| Core node | `bitcoind` / `btcd` | `geth`, `nethermind`, **`erigon`** |
| Indexer | Electrum-Server, Esplora | The Graph, Substreams, Tenderly, BlockScout |
| ETL kit | `bitcoin-etl`, BlockSci | `ethereum-etl`, `blockchain-etl-airflow` |
| Data lake helpers | Coinmetrics, Amberdata | Dune, Nansen, Flipside |
| Testing nets | `regtest` | `anvil` (Foundry), `hardhat node` |

---

## 6 · Edge-cases you must handle

1. **Re-orgs & chain splits** – always idempotent-upsert by `(block_hash, tx_hash)`.  
2. **Contract self-destruct & create2** – address reuse in ETH.  
3. **Token decimals & proxies** – read ERC-20 metadata or rely on trust-lists.  
4. **Fee models** – Legacy gas vs EIP-1559 (base + priority) vs Bitcoin fee-rate; keep both for analytics.  
5. **L2 rollups** – often dump blobs back to L1; treat them as separate chains or sub-streams.

---

## 7 · What to prototype in Go before the interview

1. **ETH live block consumer** → push headers to Kafka, commit offsets.  
2. **BTC ZeroMQ listener** → write raw tx to a local LevelDB, then decode via `decoderawtransaction`.  
3. **Simple “de-reorger”** → accepts unordered blocks, emits canonical order with rollback.  
4. **Merkle-tree verifier** → given txID + block, prove inclusion (interview classic).  

Each ≤ 150 LOC, table-driven tests, `go test -race -bench .`.

---

### Closing advice for the interview

* Anchor every answer in *data-integrity* and *chain-specific quirks* (e.g., “I wait ≥ 2 epochs before declaring ETH data final to avoid re-orgs.”).  
* Name-drop the *tool ⇄ problem* mapping (Erigon for lightweight archives, Firehose for replay, `eth_subscribe` for near-real time).  
* Show awareness of **scale**: today’s archive nodes = TBs, 2027 ETH danksharding ≈ ↑ state, so decouple storage tier.  

Master the pieces above and you can discuss *both* algorithmic underpinnings *and* pragmatic pipeline design—exactly what Coinbase’s Data Foundations team looks for. Good luck!
::contentReference[oaicite:6]{index=6}
```

[1]: https://docs.polygon.technology/pos/how-to/erigon-archive-node/?utm_source=chatgpt.com "Run an Erigon archive node - Polygon Knowledge Layer"

[2]: https://www.reddit.com/r/ethstaker/comments/1916iti/synced_erigon_archive_node_currently_takes_28tb/?utm_source=chatgpt.com "Synced Erigon Archive node. Currently takes ~2.8TB + lighthouse ..."

[3]: https://bitcoin.stackexchange.com/questions/111492/understanding-zmq-notification-output?utm_source=chatgpt.com "Understanding zmq notification output - Bitcoin Stack Exchange"

[4]: https://stackoverflow.com/questions/59659681/how-to-disable-bitcoind-zeromq-notification-publish-block-or-transaction-in-memp?utm_source=chatgpt.com "How to disable Bitcoind ZeroMQ notification publish block or ..."

[5]: https://docs.chainstack.com/reference/ethereum-native-subscribe-newheads?utm_source=chatgpt.com "eth_subscribe (\"newHeads\") | Ethereum - Chainstack Docs"

[6]: https://docs.metamask.io/services/reference/ethereum/json-rpc-methods/subscription-methods/eth_subscribe/?utm_source=chatgpt.com "Ethereum eth_subscribe | MetaMask developer documentation"
