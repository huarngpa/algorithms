### Java Regex Cheat-Sheet

*(fast-scan primer for LeetCode / CodeSignal & whiteboard interviews)*

---

#### 1 · Core Vocabulary

| Purpose               | Pattern                                       | Notes                                                                                                        |
|-----------------------|-----------------------------------------------|--------------------------------------------------------------------------------------------------------------|
| **Anchors**           | `^`, `$`, `\A`, `\Z`, `\b`, `\B`              | `^/$` obey `MULTILINE`; `\A/\Z` always start/end of entire input; `\b` word-boundary (between `\w` and `\W`) |
| **Literals**          | `\Q…\E`                                       | Quote an entire chunk (no escaping needed inside)                                                            |
| **Character Classes** | `[abc]`, `[^abc]`, `[a-z&&[^aeiou]]`          | Java supports **intersection** (`&&`)                                                                        |
| **Pre-defined**       | `\d`, `\w`, `\s`, `.`                         | `\w` ≈ *letters, digits, “\_”* unless you add `UNICODE_CHARACTER_CLASS`                                      |
| **POSIX**             | `\p{Lower}`, `\p{Alpha}`, `\p{ASCII}`         | All POSIX & full Unicode (`\p{Sc}` for Currency)                                                             |
| **Quantifiers**       | `*`, `+`, `?`, `{m,n}`                        | append `?` = **reluctant**; `+` = **possessive** (`a*+`)                                                     |
| **Groups**            | `(...)`, `(?:...)`, `(?<name>...)`, `(?>...)` | non-capturing, named, **atomic** (cuts backtracking)                                                         |
| **Back-refs**         | `\1`, `\k<name>`                              | only valid inside same match attempt                                                                         |
| **Look-around**       | `(?=X)`, `(?!X)`, `(?<=X)`, `(?<!X)`          | Java ≥9 supports **variable-length** look-behind                                                             |

---

#### 2 · Pattern Flags

```java
Pattern p = Pattern.compile(regex,
        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE);
```

Inline versions: `(?i)(?m)(?s)(?u)(?x)` etc. Example: `(?i)^abc$` ignores case.

| Flag                            | Effect                                                |
|---------------------------------|-------------------------------------------------------|
| `CASE_INSENSITIVE` (`i`)        | ASCII by default, add `UNICODE_CASE` for full Unicode |
| `MULTILINE` (`m`)               | `^` / `$` work per line                               |
| `DOTALL` (`s`)                  | `.` matches *everything* including `\n`               |
| `UNICODE_CHARACTER_CLASS` (`u`) | `\d`, `\w`, `\s` become Unicode-aware                 |
| `COMMENTS` (`x`)                | Ignores un-escaped whitespace & allows `#` comments   |

---

#### 3 · API Mini-Map

```java
Pattern p = Pattern.compile(regex);         // expensive – cache it
Matcher m = p.matcher(text);

m.

matches();          // whole input must match
m.

find();             // next subsequence
m.

group();            // last match
m.

group(1);           // nth capture
m.

replaceAll(repl);   // supports $1 … $n backrefs
Pattern.

split(text);  // like String.split but faster on big input

// Since Java 8
Stream<String> tokens = p.splitAsStream(text);
Predicate<String> validator = p.asMatchPredicate();
```

---

#### 4 · Performance Hacks

* **Pre-compile** your `Pattern`; in LeetCode simply declare `static final Pattern P = …`.
* Prefer **possessive quantifiers** (`*+`, `++`, `?+`, `{m,n}+`) or **atomic groups** `(?>...)` to slam the door on
  pathological backtracking.
* For “one & only one pass” parsing, combine `Matcher.find()` in a loop with `appendReplacement/appendTail`.
* If all you need is *containment*, `String.contains` followed by a narrow regex often beats a giant pattern.

---

#### 5 · Grab-Bag Snippets

```java
// 1 Validate IPv4 (y67 LeetCode problem)
private static final Pattern IPV4 =
        Pattern.compile("(?:(?:25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)\\.){3}"
                + "(?:25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)");

// 2 Split camelCase -> words
List<String> words = Arrays.stream(
                "parseHTTPResponse".split("(?<=[a-z])(?=[A-Z])"))
        .toList();   // [parse, HTTP, Response]

// 3 Collapse consecutive duplicates (log dedup)
String collapsed = "aaaabbbcca"
        .replaceAll("(.)\\1+", "$1");          // -> "abca"

// 4 Extract balances "USDT  123.45" into (asset,value)
Matcher m = Pattern.compile("(\\p{Alpha}{2,10})\\s+([+\\-]?\\d+(?:\\.\\d+)?)")
        .matcher(line);
while(m.

find()){
String asset = m.group(1);
BigDecimal val = new BigDecimal(m.group(2));
}

// 5 Find round-trip trades A→B then B→A (reuse in fraud task)
Pattern ROUND = Pattern.compile("(\\w+)->(\\w+).*?\\2->\\1", Pattern.DOTALL);
boolean sus = ROUND.matcher(chunk).find();
```

---

#### 6 · Interview-Type Challenges & Pattern Crib

| Task                        | Sketch                                                       |                                          |
|-----------------------------|--------------------------------------------------------------|------------------------------------------|
| **Email** (simple)          | `^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$`          |                                          |
| **Numeric Literal**         | \`^\[+-]?(0                                                  | \[1-9]\d\*)(\\.\d+)?(\[eE]\[+-]?\d+)?$\` |
| **Log Timestamp ISO-8601**  | `\\d{4}-\\d{2}-\\d{2}[T ]\\d{2}:\\d{2}:\\d{2}(?:\\.\\d+)?Z?` |                                          |
| **HTML Tag Strip**          | `(?s)<[^>]+>` (then `replaceAll` with `""`)                  |                                          |
| **Tokenize by non-letters** | `split("\\P{Alpha}+")`                                       |                                          |

---

#### 7 · Look-Around Cheat

| Goal                                                        | Pattern                                      |
|-------------------------------------------------------------|----------------------------------------------|
| Digits **not** at word-edge                                 | `(?<!\\d)\\d+(?!\\d)`                        |
| Capture “foo” followed by “bar” **without** consuming “bar” | `foo(?=bar)`                                 |
| Find “cat” not preceded by “house”                          | `(?<!house)cat`                              |
| Dollar amounts, reject `$0`                                 | `\\$(?=\\d*[1-9]\\d*)(\\d{1,3}(?:,\\d{3})*)` |

---

#### 8 · When **Not** to Use Regex (talk like a senior)

* Parsing **nested / recursive** structures – use a parser or stack.
* You need **streaming / multi-gig logs** – split & manual scan often 10× faster.
* Anything that becomes a write-only “megaregex”; clarity beats clever.
  Mentioning these trade-offs scores review points at Coinbase.

---

##### Quick Mental Checklist Before You Hit “Run Tests”

1. **Anchors correct?** (`^` vs `\A`, `$` vs `\Z` when multiline).
2. **Escaped backslashes** inside Java string (double them!).
3. Use **reluctant `*?`** when trimming minimal interior.
4. Compile once; share pattern across calls.
5. Add `UNICODE_CHARACTER_CLASS` if `\w`/`\d` must be Unicode.

Keep this page handy, rehearse five minutes, and you’ll crush those regex subtasks en-route to the bigger algorithm.
Good luck 🚀
