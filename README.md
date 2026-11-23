# Cinema Seat Booking: HashMap with a Custom Class as Key

A cinema hall has rows A to E with seats 1 to 6 in each row. Bookings are stored in a `HashMap<Seat, String>`:

- **key:** `Seat` (a custom class, for example `B5`)
- **value:** `String` (the name of the customer who booked it)

Because the key is a class and not a primitive or `String`, `Seat` must override `equals()` and `hashCode()` for the map to work correctly.

## Files

| File | Description |
|------|-------------|
| `Task_CinemaBooking.java` | The solution (Seat class, Parts A to G, answers to the questions) |

## How to Run

```bash
javac Task_CinemaBooking.java
java Task_CinemaBooking
```

Requires JDK 16+ only for the bonus (`record`). The main solution runs on any modern JDK.

## Output

### With `equals()` and `hashCode()` (correct)

```
Bookings:           {A1=Ali, C3=Mona, A2=Sara, B5=Omar}
Who booked C3:      Mona
Is A2 taken:        true
Is E6 taken:        false
Youssef -> B5:      refused, taken by Omar
After Ali moved:    {C3=Mona, A2=Sara, B5=Omar, D2=Ali}
Hana -> E1:         refused, seat is broken
Seats booked:       4

     1  2  3  4  5  6
A    .  X  .  .  .  .
B    .  .  .  .  X  .
C    .  .  X  .  .  .
D    .  X  .  .  .  .
E    #  #  .  .  .  .
```

The order inside `{ }` may differ on other machines, because `HashMap` has no guaranteed order.

### With `equals()` only, `hashCode()` removed (wrong)

```
Bookings:           {C3=Mona, A1=Ali, A2=Sara, B5=Omar}
Who booked C3:      null
Is A2 taken:        false
Is E6 taken:        false
Youssef -> B5:      booked
After Ali moved:    {C3=Mona, D2=Ali, A1=Ali, A2=Sara, B5=Omar, B5=Youssef}
Hana -> E1:         booked
Seats booked:       7

     1  2  3  4  5  6
A    .  .  .  .  .  .
B    .  .  .  .  .  .
C    .  .  .  .  .  .
D    .  .  .  .  .  .
E    .  .  .  .  .  .
```

Every lookup fails, `B5` ends up booked twice, the broken-seat check is bypassed, and the chart is empty.

## Solution Summary

```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Seat other = (Seat) o;
    return row == other.row && number == other.number;
}

@Override
public int hashCode() {
    return Objects.hash(row, number);
}
```

- `equals()` and `hashCode()` use the same fields (`row` and `number`).
- Every seat used in `main()` is a **new** `Seat` object, as the task rules require.
- `putIfAbsent()` returns `null` if the seat was free, otherwise the name already stored.

## Answers

### Q1. Why did we have to override `hashCode()` and `equals()`?

`HashMap` does not compare keys with `==`. It uses `hashCode()` to find the bucket, then `equals()` to find the exact key inside that bucket.

Every `new Seat('B', 5)` is a different object. By default, `equals()` compares memory addresses and `hashCode()` is based on object identity, so a new `B5` never matches the `B5` stored in the map. Overriding both tells Java that two `Seat` objects with the same row and number are the same seat.

Both methods are needed. `hashCode()` finds the right bucket and `equals()` confirms the match. The contract is: if `a.equals(b)`, then `a.hashCode() == b.hashCode()`.

### Q2. Delete `hashCode()` but keep `equals()`. What happens? Why?

The lookups fail (see the wrong output above):

- `get(new Seat('C', 3))` returns `null`.
- `containsKey()` returns `false`.
- `remove()` removes nothing.
- `putIfAbsent()` lets Youssef take `B5`, so `B5` is stored twice.
- The chart shows no `X` marks.

**Why:** without our `hashCode()`, each new `Seat` gets a different identity-based hash code. `HashMap` looks in the wrong bucket and never reaches `equals()`, even though our `equals()` is correct.

### Q3. Why are `row` and `number` `final`? What could go wrong if the number of a booked seat could change after it was put in the map?

`hashCode()` is computed from `row` and `number`. `HashMap` places the entry in a bucket chosen by the hash at the moment of `put()`.

If `number` changes afterwards, the hash changes but the entry stays in the old bucket. Then `get()`, `containsKey()` and `remove()` look in the new bucket and cannot find it. The booking is still in the map (it shows in the printout and in `size()`), but it is unreachable. This is a silent bug and a memory leak.

Keys should be immutable, and `final` fields guarantee that.

### Q4. Change `equals(Object o)` to `equals(Seat o)`. What happens? Then also remove `@Override`. What happens now?

- **With `@Override`:** compile error ("method does not override or implement a method from a supertype"). `equals(Seat)` has a different signature from `Object.equals(Object)`, so it is not an override. `@Override` exists to catch exactly this mistake.
- **Without `@Override`:** it compiles, but `equals(Seat)` is only an **overload**. `HashMap` calls `equals(Object)`, which is still the identity-based version from `Object`. The program behaves as if `equals()` was never written, and the lookups fail as in Q2, with no error message.

**Lesson:** always write `@Override`.

### Bonus. Replace the whole `Seat` class with `record Seat(char row, int number) {}`

It works (Java 16+). A record automatically generates the constructor, accessors, `equals()`, `hashCode()` and `toString()`, and its fields are implicitly `private final`. `equals()` and `hashCode()` are based on all fields, so the `HashMap` logic is correct with no extra code.

One visible difference: the generated `toString()` prints `Seat[row=A, number=1]` instead of `A1`, so the map prints as `{Seat[row=A, number=1]=Ali, ...}`. To keep the `A1` format, add your own `toString()` inside the record body.