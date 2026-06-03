# Java Streams Interview Cheat Sheet (One Page)

Use this as a copy/paste template sheet for common interview patterns.

## Setup (reuse in snippets)

```java
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

record Person(String name, String dept, int age, long salary, List<String> skills) {}

List<Person> people = List.of(
    new Person("Ana", "Eng", 30, 120_000, List.of("java", "sql")),
    new Person("Ben", "Eng", 26, 100_000, List.of("java", "aws")),
    new Person("Cam", "Ops", 35, 110_000, List.of("linux", "aws")),
    new Person("Di", "Eng", 30, 120_000, List.of("java", "kafka"))
);
```

---

## 1) MAP Patterns

### A) Transform list (object -> field)
```java
List<String> names = people.stream()
    .map(Person::name)
    .toList();
```

### B) map + filter + sort
```java
List<String> engNames = people.stream()
    .filter(p -> p.dept().equals("Eng"))
    .map(Person::name)
    .sorted()
    .toList();
```

### C) Flatten nested lists (flatMap)
```java
List<String> allSkills = people.stream()
    .flatMap(p -> p.skills().stream())
    .distinct()
    .sorted()
    .toList();
```

### D) Build a map (key -> value)
```java
Map<String, Long> salaryByName = people.stream()
    .collect(Collectors.toMap(
        Person::name,
        Person::salary
    ));
```

### E) toMap with duplicate-key merge (IMPORTANT)
```java
Map<Integer, String> nameByAgeKeepFirst = people.stream()
    .collect(Collectors.toMap(
        Person::age,
        Person::name,
        (first, second) -> first // merge rule when keys collide
    ));
```

---

## 2) GROUP Patterns

### A) groupBy key
```java
Map<String, List<Person>> byDept = people.stream()
    .collect(Collectors.groupingBy(Person::dept));
```

### B) groupBy + count
```java
Map<String, Long> countByDept = people.stream()
    .collect(Collectors.groupingBy(
        Person::dept,
        Collectors.counting()
    ));
```

### C) groupBy + mapped field list
```java
Map<String, List<String>> namesByDept = people.stream()
    .collect(Collectors.groupingBy(
        Person::dept,
        Collectors.mapping(Person::name, Collectors.toList())
    ));
```

### D) groupBy + reduce to single best item per group
```java
Map<String, Optional<Person>> highestPaidByDept = people.stream()
    .collect(Collectors.groupingBy(
        Person::dept,
        Collectors.maxBy(Comparator.comparingLong(Person::salary))
    ));
```

---

## 3) AVERAGE Patterns

### A) Average of one field
```java
double avgSalary = people.stream()
    .collect(Collectors.averagingLong(Person::salary));
```

### B) Average per group
```java
Map<String, Double> avgSalaryByDept = people.stream()
    .collect(Collectors.groupingBy(
        Person::dept,
        Collectors.averagingLong(Person::salary)
    ));
```

### C) Average lap time per lapCount (your case)
```java
record LapRecord(String lapId, long time, String driverId, int lapCount) {}

Map<Integer, Double> avgByLapCount = laps.stream()
    .collect(Collectors.groupingBy(
        LapRecord::lapCount,
        Collectors.averagingLong(LapRecord::time)
    ));
```

### D) Integer average (if interviewer wants long result)
```java
Map<Integer, Long> avgAsLong = laps.stream()
    .collect(Collectors.groupingBy(
        LapRecord::lapCount,
        Collectors.collectingAndThen(
            Collectors.summarizingLong(LapRecord::time),
            stats -> stats.getCount() == 0 ? 0L : stats.getSum() / stats.getCount()
        )
    ));
```

---

## 4) TOP-K Patterns

### A) Top K largest numbers
```java
int k = 3;
List<Integer> topK = nums.stream()
    .sorted(Comparator.reverseOrder())
    .limit(k)
    .toList();
```

### B) Top K objects by field
```java
int k = 2;
List<Person> topPaid = people.stream()
    .sorted(Comparator.comparingLong(Person::salary).reversed())
    .limit(k)
    .toList();
```

### C) Top K per group
```java
int k = 2;
Map<String, List<Person>> topKByDept = people.stream()
    .collect(Collectors.groupingBy(
        Person::dept,
        Collectors.collectingAndThen(Collectors.toList(), list ->
            list.stream()
                .sorted(Comparator.comparingLong(Person::salary).reversed())
                .limit(k)
                .toList()
        )
    ));
```

### D) Top K frequent elements
```java
int k = 2;
List<String> topKFrequentSkills = people.stream()
    .flatMap(p -> p.skills().stream())
    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
    .entrySet().stream()
    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
    .limit(k)
    .map(Map.Entry::getKey)
    .toList();
```

---

## Interview Notes (High Value)

- Prefer `groupingBy + downstream collector` instead of collecting to `List` then re-streaming.
- For duplicates in `toMap`, always provide a merge function.
- Use `comparingInt/comparingLong` for primitive fields.
- Mention complexity:
  - Group/count/average: `O(n)`
  - Top-k via full sort: `O(n log n)`
  - If asked for better, mention min-heap: `O(n log k)`
- If output order matters, use `LinkedHashMap` or `TreeMap` in collectors.

