# Furthest Neighbours Search
Implementations of `k-furthest neighbours` algorithms in Java.

### The problem
Let `U` a collection of items, `Q` a collection of query items and `k` a positive integer. The k-furthest items
problem asks to find for every item in `Q` its k-furthest items, which belong in `U`.

### The algorithms
1. Brute Force (BruteForce.java): It is exact and of brute force.
2. [Guaranteed Drusilla Select](http://www.ratml.org/pub/pdf/2017exploiting.pdf) (GuaranteedDrusilla.java): It is approximate, but with a guaranteed solution quality
provided by the user.
3. [Query Dependent](https://www.itu.dk/people/pagh/papers/approx-furthest-neighbor-SISAP15.pdf) (QueryDependent.java): It is approximate and works only for k=1
4. Double Priority Queue 1-Dimensional (DoublePQ1D.java): It is exact and works only on 1-dimensional data. **It is still in development!**
5. Sorting 1-Dimensional (Sort1D.java): It is exact or guaranteed approximate and works only on 1-dimensional data. **It is still in development!**

### Disclaimer
This project has an experimental theme, I would not recommend using it in production.

### Java version
15+ (I'm pretty sure it can compile with a lower version too)
