import java.util.*;

/*
 =======================================================================
  TASK: Cinema Seat Booking  -  a HashMap with your own class as the key
 =======================================================================
*/

class Task_CinemaBooking {

    // A seat in the cinema hall, for example B5 = row B, seat number 5.
    // static so that main() can create Seat objects directly.
    static class Seat {
        final char row;     // 'A' to 'E'
        final int number;   // 1 to 6

        Seat(char row, int number) {
            this.row = row;
            this.number = number;
        }

        @Override
        public String toString() {
            return "" + row + number;
        }

        // TODO 1: two seats are the same seat when row AND number are the same.
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Seat other = (Seat) o;
            return row == other.row && number == other.number;
        }

        // TODO 2: equal seats must return the same hash code.
        // Uses the same fields as equals().
        @Override
        public int hashCode() {
            return Objects.hash(row, number);
        }
    }

    public static void main(String[] args) {

        // Key = Seat, Value = name of the customer who booked it
        HashMap<Seat, String> bookings = new HashMap<>();
        HashSet<Seat> broken = new HashSet<>();

        // Part A: book seats with put()
        bookings.put(new Seat('A', 1), "Ali");
        bookings.put(new Seat('A', 2), "Sara");
        bookings.put(new Seat('B', 5), "Omar");
        bookings.put(new Seat('C', 3), "Mona");
        System.out.println("Bookings:           " + bookings);

        // Part B: who booked C3?
        String c3Owner = bookings.get(new Seat('C', 3));
        System.out.println("Who booked C3:      " + c3Owner);

        // Part C: are A2 and E6 taken?
        System.out.println("Is A2 taken:        " + bookings.containsKey(new Seat('A', 2)));
        System.out.println("Is E6 taken:        " + bookings.containsKey(new Seat('E', 6)));

        // Part D: Youssef wants B5. putIfAbsent() does not replace an existing booking.
        String existing = bookings.putIfAbsent(new Seat('B', 5), "Youssef");
        if (existing == null) {
            System.out.println("Youssef -> B5:      booked");
        } else {
            System.out.println("Youssef -> B5:      refused, taken by " + existing);
        }

        // Part E: Ali moves from A1 to D2.
        bookings.remove(new Seat('A', 1));
        bookings.put(new Seat('D', 2), "Ali");
        System.out.println("After Ali moved:    " + bookings);

        // Part F: E1 and E2 are broken. Hana wants E1.
        broken.add(new Seat('E', 1));
        broken.add(new Seat('E', 2));

        Seat hanaSeat = new Seat('E', 1);
        if (!broken.contains(hanaSeat)) {
            bookings.put(hanaSeat, "Hana");
            System.out.println("Hana -> E1:         booked");
        } else {
            System.out.println("Hana -> E1:         refused, seat is broken");
        }

        // Part G: how many seats are booked?
        System.out.println("Seats booked:       " + bookings.size());

        // GIVEN - prints the seating chart
        System.out.println();
        printChart(bookings, broken);
    }

    // GIVEN - do not change.  Prints the hall:  X = booked   # = broken   . = free
    static void printChart(HashMap<Seat, String> bookings, HashSet<Seat> broken) {
        System.out.println("     1  2  3  4  5  6");
        for (char row = 'A'; row <= 'E'; row++) {
            System.out.print(row + "  ");
            for (int number = 1; number <= 6; number++) {
                Seat seat = new Seat(row, number);
                if (bookings.containsKey(seat)) {
                    System.out.print("  X");
                } else if (broken.contains(seat)) {
                    System.out.print("  #");
                } else {
                    System.out.print("  .");
                }
            }
            System.out.println();
        }
    }
}

/*
 QUESTIONS - write your answers here

 Q1. Why did we have to override hashCode() and equals()?
 A1. HashMap does not compare keys with ==. It first uses hashCode() to find
     the bucket, then equals() to find the exact key inside that bucket.
     Every time we write new Seat('B', 5) we get a NEW object, so by default
     (Object's version) it is different from the B5 stored in the map, because
     the default equals() compares memory addresses and the default hashCode()
     is based on the object's identity. By overriding both, we tell Java that
     two Seat objects with the same row and number are the same seat.
     Both are needed: hashCode() finds the right bucket, equals() confirms
     the match. The contract: if a.equals(b) then a.hashCode() == b.hashCode().

 Q2. Delete your hashCode() but keep equals(), and run again. What happens? Why?
 A2. The lookups fail. get(new Seat('C', 3)) returns null, containsKey(...)
     returns false, remove(...) removes nothing, and putIfAbsent() lets
     Youssef take B5. The chart also shows no X marks.
     Why: without our hashCode(), each new Seat gets a different identity-based
     hash code, so HashMap looks in the wrong bucket (and compares hashes first)
     and never even calls equals(). Our equals() is correct but never reached.

 Q3. Why are row and number final? What could go wrong if the number of a booked
     seat could be changed after it was put in the map?
 A3. hashCode() is computed from row and number. HashMap stores the entry in a
     bucket chosen by the hash at the moment of put(). If the number changes
     afterwards, the hash code changes, but the entry stays in the old bucket.
     Then get(), containsKey() and remove() look in the new bucket and cannot
     find it: the booking is still in the map (it appears in the printout and
     counts in size()) but is unreachable, which is a silent bug and a memory
     leak. Keys should be immutable, and final fields guarantee that.

 Q4. Change equals(Object o) to equals(Seat o). What happens?
     Then also remove @Override and run again. What happens now?
 A4. With @Override: compile error, "method does not override or implement a
     method from a supertype". equals(Seat) is not the same signature as
     Object.equals(Object), so it is not an override. @Override is what
     catches this mistake.
     Without @Override: it compiles, but equals(Seat) is only an OVERLOAD.
     HashMap calls equals(Object), which is still Object's identity-based
     version. So the program behaves as if we never wrote equals(): lookups
     fail like in Q2, and no error message tells us why.
     Lesson: always write @Override.

 BONUS. Replace the whole Seat class with: record Seat(char row, int number) {}
 A.  It works (Java 16+). A record automatically generates a constructor,
     accessors, equals(), hashCode() and toString(), and its fields are
     implicitly private final. equals() and hashCode() are based on all fields,
     so the HashMap logic is correct with no extra code.
     One visible difference: the generated toString() prints
     Seat[row=A, number=1] instead of A1, so the map output looks like
     {Seat[row=A, number=1]=Ali, ...}. To keep the A1 format, add
     your own toString() inside the record body.
*/