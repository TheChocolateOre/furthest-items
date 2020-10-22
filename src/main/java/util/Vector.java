package util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

/**
 * Represents a mathematical vector that starts from the origin of a Euclidean
 * space and ends at the point specified by its coordinate values.
 */
public class Vector {

    /**
     * The coordinates of the end point of this Vector.
     */
    private @NotNull double[] coordinates;

    /**
     * Indicates if the given Vectors have all the same size.
     * @param vectors An array of Vector's' to determine if they all have the
     * same size.
     * @return True if all the given Vector's' have the same size, otherwise
     * false.
     * @throws IllegalArgumentException If the length of the vectors array is
     * {@literal <} 2.
     */
    public static boolean sameSize(@NotNull Vector... vectors) {
        if (vectors.length < 2) {
            throw new IllegalArgumentException("At least 2 Vector's' must be " +
                    "given.");
        }//end if

        final int SIZE = vectors[0].size();
        for (int i = 1; i < vectors.length; ++i) {
            if (vectors[i].size() != SIZE) {
                return false;
            }//end if
        }//end for

        return true;
    }

    /**
     * Calculates the sum of the 2 given Vector's' and stores it in a new
     * created Vector that is returned.
     * @param addend1 The 1st Vector of the addition.
     * @param addend2 The 2nd Vector of the addition.
     * @return A new created Vector with the result of the addition of the 2
     * given addend Vector's'.
     * @throws IllegalArgumentException If the given Vector's' don't have the
     * same size.
     */
    public static @NotNull Vector add(@NotNull Vector addend1, @NotNull Vector
            addend2) {
        Vector result = new Vector(addend1.size());
        Vector.add(addend1, addend2, result);
        return result;
    }

    /**
     * Calculates the sum between the given Vector and addend value and stores
     * the result in a new Vector.
     * @param vector A Vector to act as the 1st addend.
     * @param addend A value to act as the 2nd addend.
     * @return A new created Vector with the result of the addition of the given
     * Vector and addend value.
     */
    public static @NotNull Vector add(@NotNull Vector vector, double addend) {
        Vector result = new Vector(vector.size());
        result.setAll(i -> vector.get(i) + addend);
        return result;
    }

    /**
     * Calculates the difference of the 2 given Vector's' and stores it in a new
     * created Vector that is returned.
     * @param minuend The minuend Vector of the subtraction.
     * @param subtrahend The subtrahend Vector of the subtraction.
     * @return A new created Vector with the result of the subtraction of the 2
     * given Vector's'.
     * @throws IllegalArgumentException If the given Vector's' don't have the
     * same size.
     */
    public static @NotNull Vector subtract(@NotNull Vector minuend, @NotNull
            Vector subtrahend) {
        Vector result = new Vector(minuend.size());
        Vector.subtract(minuend, subtrahend, result);
        return result;
    }

    /**
     * Calculates the difference between the given Vector and subtrahend value
     * and stores the result in a new Vector.
     * @param vector A Vector to act as the minuend.
     * @param subtrahend A value to act as the subtrahend.
     * @return A new created Vector with the result of the difference between
     * the given Vector and subtrahend value.
     */
    public static @NotNull Vector subtract(@NotNull Vector vector, double
            subtrahend) {
        return Vector.add(vector, -subtrahend);
    }

    /**
     * Calculates the product of the 2 Vector's' and stores it in a new created
     * Vector that is returned, with values result(i) = factor1(i) * factor2(i).
     * @param factor1 The 1st Vector of the multiplication.
     * @param factor2 The 2nd Vector of the multiplication.
     * @return A new created Vector with the result of the multiplication of the
     * 2 given Vector's', with values result(i) = factor1(i) * factor2(i).
     * @throws IllegalArgumentException If the given Vector's' don't have the
     * same size.
     */
    public static @NotNull Vector multiplyFlat(@NotNull Vector factor1, @NotNull
            Vector factor2) {
        Vector result = new Vector(factor1.size());
        Vector.multiplyFlat(factor1, factor2, result);
        return result;
    }

    /**
     * Calculates the product of the given Vector and factor value and stores
     * the result in a new Vector.
     * @param vector A Vector to act as the 1st factor.
     * @param factor A value to act as the 2nd factor.
     * @return A new created Vector with the result of the multiplication of the
     * given Vector and factor value.
     */
    public static @NotNull Vector multiply(@NotNull Vector vector, double
            factor) {
        Vector result = new Vector(vector.size());
        result.setAll(i -> vector.get(i) * factor);
        return result;
    }

    /**
     * Calculates the quotient of the 2 given Vector's' and stores it in a new
     * created Vector that is returned, with values result(i) = dividend(i) /
     * divisor(i).
     * @param dividend The dividend Vector of the division.
     * @param divisor The divisor Vector of the division.
     * @return A new created Vector with the result of the division of 2 given
     * Vector's', with values result(i) = dividend(i) / divisor(i).
     * @throws IllegalArgumentException If the given Vector's' don't have the
     * same size.
     */
    public static @NotNull Vector divideFlat(@NotNull Vector dividend, @NotNull
            Vector divisor) {
        Vector result = new Vector(dividend.size());
        Vector.divideFlat(dividend, divisor, result);
        return result;
    }

    /**
     * Calculates the quotient of the given Vector and divisor value and stores
     * the result in a new Vector.
     * @param vector A Vector to act as the dividend.
     * @param divisor A value to act as the divisor.
     * @return A new created Vector with the result of the division between the
     * given Vector and divisor value.
     */
    public static @NotNull Vector divide(@NotNull Vector vector, double
            divisor) {
        return Vector.multiply(vector, 1.0 / divisor);
    }

    /**
     * Calculates the dot product of 2 given Vector's'.
     * @param v1 The 1st Vector of the dot product.
     * @param v2 The 2nd Vector of the dot product.
     * @return The dot product of the 2 given Vector's'.
     * @throws IllegalArgumentException If the 2 given Vector's' do not have the
     * same size.
     */
    public static double dotProduct(@NotNull Vector v1, @NotNull Vector v2) {
        if (!Vector.sameSize(v1, v2)) {
            throw new IllegalArgumentException("The given Vector's' must " +
                    "have the same size.");
        }//end if

        double dotProduct = 0.0;
        for (int i = 0; i < v1.size(); ++i) {
            dotProduct += v1.get(i) * v2.get(i);
        }//end for

        return dotProduct;
    }

    /**
     * Calculates the the angle between the 2 given Vector's', in radians.
     * @param v1 The 1st Vector.
     * @param v2 The 2nd Vector.
     * @return The angle between the 2 given Vector's', in radians.
     */
    public static double angle(@NotNull Vector v1, @NotNull Vector v2) {
        return Math.acos(Vector.cosTheta(v1, v2));
    }

    /**
     * Calculates the cosine of the angle between the 2 given Vector's'.
     * @param v1 The 1st Vector.
     * @param v2 The 2nd Vector.
     * @return The cosine of the angle between the 2 given Vector's'.
     */
    public static double cosTheta(@NotNull Vector v1, @NotNull Vector v2) {
        return Vector.dotProduct(v1, v2) / (v1.magnitude() * v2.magnitude());
    }

    /**
     * Calculates the squared Euclidean distance between 2 given Vector's',
     * i.e. the square distance between their end points.
     * This method is used for performance reasons, when the true value
     * of a distance does not need to be calculated, but instead it's square is
     * sufficient to make decisions.
     * @param v1 The 1st Vector.
     * @param v2 The 2nd Vector.
     * @return The squared Euclidean distance between the 2 given Vector's'.
     * @throws IllegalArgumentException If the 2 given Vector's' do not have the
     * same size.
     */
    public static double sqrDistance(@NotNull Vector v1, @NotNull Vector v2) {
        if (!Vector.sameSize(v1, v2)) {
            throw new IllegalArgumentException("The given Vector's' must " +
                    "have the same size.");
        }//end if

        double sqrDistance = 0.0;
        for (int i = 0; i < v1.size(); ++i) {
            double d = v1.get(i) - v2.get(i);
            sqrDistance += d * d;
        }//end for

        return sqrDistance;
    }

    /**
     * Calculates the Euclidean distance between 2 given Vector's', i.e. the
     * distance between their end points.
     * @param v1 The 1st Vector.
     * @param v2 The 2nd Vector.
     * @return The Euclidean distance between the 2 given Vector's'.
     * @throws IllegalArgumentException If the 2 given Vector's' do not have the
     * same size.
     */
    public static double distance(@NotNull Vector v1, @NotNull Vector v2) {
        return Math.sqrt(Vector.sqrDistance(v1, v2));
    }

    /**
     * Calculates the center Vector given a Collection of Vector's'.
     * @param vectors A Collection of Vector's' to calculate their center.
     * @return A new Vector that represents the center of the given Collection
     * of Vector's'.
     * @throws IllegalArgumentException If Collection vectors is empty.
     */
    public static @NotNull Vector center(@NotNull Collection<Vector> vectors) {
        if (vectors.isEmpty()) {
            throw new IllegalArgumentException("Argument Collection vectors " +
                    "can't be empty.");
        }//end if

        Vector center = new Vector(vectors.stream()
                                          .findFirst()
                                          .get()
                                          .size());

        for (Vector v : vectors) {
            center.add(v);
        }//end for

        return center.divide(vectors.size());
    }

    /**
     * Calculates the center Vector given an Iterator of Vector's'.
     * @param iterator An Iterator of Vector's' to calculate their center.
     * @return A new Vector that represents the center of the given Collection
     * of Vector's'.
     * @throws IllegalArgumentException If iterator has no elements.
     */
    public static @NotNull Vector center(@NotNull Iterator<Vector> iterator) {
        if (!iterator.hasNext()) {
            throw new IllegalArgumentException("Argument iterator has no " +
                    "elements.");
        }//end if

        Vector center = new Vector(iterator.next());
        long size = 1L;
        while (iterator.hasNext()) {
            center.add(iterator.next());
            ++size;
        }//end while

        return center.divide(size);
    }

    /**
     * Calculates the sum of the 2 addend Vector's' and stores it in a given
     * Vector.
     * @param addend1 The 1st Vector of the addition.
     * @param addend2 The 2nd Vector of the addition.
     * @param result A Vector to store the result of the addition.
     * @throws IllegalArgumentException If the given Vector's' don't have the
     * same size.
     */
    @Deprecated
    private static void add(@NotNull Vector addend1, @NotNull Vector addend2,
            @NotNull Vector result) {
        if (!Vector.sameSize(addend1, addend2, result)) {
            throw new IllegalArgumentException("The given Vector's' must " +
                    "have the same size.");
        }//end if

        result.setAll(i -> addend1.get(i) + addend2.get(i));
    }

    /**
     * Calculates the difference of the minuend and subtrahend Vector's' and
     * stores it in a given Vector.
     * @param minuend The minuend Vector of the subtraction.
     * @param subtrahend The subtrahend Vector of the subtraction.
     * @param result A Vector to store the result of the subtraction.
     * @throws IllegalArgumentException If the given Vector's' don't have the
     * same size.
     */
    @Deprecated
    private static void subtract(@NotNull Vector minuend, @NotNull Vector
            subtrahend, @NotNull Vector result) {
        if (!Vector.sameSize(minuend, subtrahend, result)) {
            throw new IllegalArgumentException("The given Vector's' must " +
                    "have the same size.");
        }//end if

        result.setAll(i -> minuend.get(i) - subtrahend.get(i));
    }

    /**
     * Calculates the product of the 2 factor Vector's' and stores it in a given
     * Vector, with values result(i) = factor1(i) * factor2(i).
     * @param factor1 The 1st Vector of the multiplication.
     * @param factor2 The 2nd Vector of the multiplication.
     * @param result A Vector to store the result of the multiplication, with
     * values result(i) = factor1(i) * factor2(i).
     * @throws IllegalArgumentException If the given Vector's' don't have the
     * same size.
     */
    @Deprecated
    private static void multiplyFlat(@NotNull Vector factor1, @NotNull Vector
            factor2, @NotNull Vector result) {
        if (!Vector.sameSize(factor1, factor2, result)) {
            throw new IllegalArgumentException("The given Vector's' must " +
                    "have the same size.");
        }//end if

        result.setAll(i -> factor1.get(i) * factor2.get(i));
    }

    /**
     * Calculates the quotient of the dividend and divisor Vector's' and stores
     * it in a given Vector, with values result(i) = dividend(i) / divisor(i).
     * @param dividend The dividend Vector of the division.
     * @param divisor The divisor Vector of the division.
     * @param result A Vector to store the result of the division, with values
     * result(i) = dividend(i) / divisor(i).
     * @throws IllegalArgumentException If the given Vector's' don't have the
     * same size.
     */
    @Deprecated
    private static void divideFlat(@NotNull Vector dividend, @NotNull Vector
            divisor, @NotNull Vector result) {
        if (!Vector.sameSize(dividend, divisor, result)) {
            throw new IllegalArgumentException("The given Vector's' must " +
                    "have the same size.");
        }//end if

        result.setAll(i -> dividend.get(i) / divisor.get(i));
    }

    /**
     * Creates a Vector, given the number of dimensions of the Euclidean space
     * it lies in. The created Vector will end at the origin, i.e. have a length
     * of 0.
     * @param size The number of dimensions of the Euclidean space this Vector
     * lies in.
     * @throws IllegalArgumentException If size is {@literal <} 1.
     */
    public Vector(int size) {
        if (size < 1) {
            throw new IllegalArgumentException("Argument size can't be < 1.");
        }//end if

        this.coordinates = new double[size];
    }

    /**
     * Creates a Vector, given the number of dimensions of the Euclidean space
     * it lies in and a function to initialize it.
     * @param size The number of dimensions of the Euclidean space this Vector
     * lies in.
     * @param function A function to initialize this Vector.
     * @throws IllegalArgumentException If size {@literal <} 1.
     */
    public Vector(int size, ToDoubleFunction<Integer> function) {
        this(size);
        for (int i = 0; i < this.coordinates.length; ++i) {
            this.coordinates[i] = function.applyAsDouble(i);
        }//end for
    }

    /**
     * Creates a copy Vector of the given Vector.
     * @param other A Vector to copy.
     */
    public Vector(@NotNull Vector other) {
        this.coordinates = Arrays.copyOf(other.coordinates,
                other.coordinates.length);
    }

    /**
     * Creates a Vector, given the coordinates of its end point.
     * @param coordinates An array with the coordinates of the end point of this
     * Vector.
     * @throws IllegalArgumentException If the length of the coordinates array
     * is {@literal <} 1.
     */
    public Vector(@NotNull double... coordinates) {
        if (coordinates.length < 1) {
            throw new IllegalArgumentException("Argument size can't be < 1.");
        }//end if

        this.coordinates = coordinates;
    }

    /**
     * Gets the value of the i-coordinate of the end point of this Vector.
     * @param i The i-coordinate of the end point of this Vector, starting from
     * 0.
     * @return The value of the i-coordinate of the end point of this Vector.
     * @throws IndexOutOfBoundsException If i is {@literal <} 0 or i is
     * {@literal >=} to the number of dimensions of the Euclidean space this
     * Vector lies in.
     */
    public double get(int i) {
        Objects.checkIndex(i, this.size());
        return this.coordinates[i];
    }

    /**
     * Sets the value of the i-coordinate of the end point of this Vector.
     * @param i The i-coordinate of the end point of this Vector, starting from
     * 0.
     * @param value The new value of the i-coordinate of the end point of this
     * Vector.
     * @throws IndexOutOfBoundsException If i is {@literal <} 0 or i is
     * {@literal >=} to the number of dimensions of the Euclidean space this
     * Vector lies in.
     */
    public void set(int i, double value) {
        Objects.checkIndex(i, this.coordinates.length);
        this.coordinates[i] = value;
    }

    /**
     * Sets all the values of the coordinates of the end point of this Vector,
     * based on a given function.
     * @param function Function that accepts the index of a coordinate of this
     * Vector and returns a value to be set at that coordinate.
     */
    public void setAll(@NotNull ToDoubleFunction<Integer> function) {
        for (int i = 0; i < this.size(); ++i) {
            this.set(i, function.applyAsDouble(i));
        }//end for
    }

    /**
     * Gets the number of dimensions of the Euclidean space this Vector lies in.
     * @return The number of dimensions of the Euclidean space this Vector lies
     * in.
     */
    public int size() {
        return this.coordinates.length;
    }

    /**
     * Creates a List that represents this Vector.
     * @return A List that represents this Vector.
     */
    public @NotNull
    List<Double> toList() {
        return Arrays.stream(this.coordinates)
                     .boxed()
                     .collect(Collectors.toList());
    }

    /**
     * Calculates the sum of this Vector with the given addend Vector and stores
     * it in this Vector.
     * @param addend An addend Vector to perform the addition with this Vector.
     * @return A reference to this Vector with the result of the addition of it
     * with the given addend Vector, for use in chain calls.
     * @throws IllegalArgumentException If the given Vector's' don't have the
     * same size.
     */
    @Contract(pure = false)
    public @NotNull Vector add(@NotNull Vector addend) {
        Vector.add(this, addend, this);
        return this;
    }

    /**
     * Adds the given addend value to all the coordinate values of this Vector.
     * @param addend A value to add it to all the coordinate values of this
     * Vector.
     * @return A reference to this Vector with the result of the addition of it
     * with the given value, for use in chain calls.
     */
    @Contract(pure = false)
    public @NotNull Vector add(double addend) {
        this.setAll(i -> this.get(i) + addend);
        return this;
    }

    /**
     * Calculates the difference of this Vector with the given subtrahend Vector
     * and stores it in this Vector.
     * @param subtrahend A subtrahend Vector to perform the subtraction with
     * this Vector.
     * @return A reference to this Vector with the result of the subtraction of
     * it with the given subtrahend Vector, for use in chain calls.
     * @throws IllegalArgumentException If the given Vector's' don't have the
     * same size.
     */
    @Contract(pure = false)
    public @NotNull Vector subtract(@NotNull Vector subtrahend) {
        Vector.subtract(this, subtrahend, this);
        return this;
    }

    /**
     * Subtracts the given subtrahend value from all the coordinate values of
     * this Vector.
     * @param value A value to subtract it from all the coordinate values of
     * this Vector.
     * @return A reference to this Vector with the result of the subtraction of
     * it with the given value, for use in chain calls.
     */
    @Contract(pure = false)
    public @NotNull Vector subtract(double value) {
        return this.add(-value);
    }

    /**
     * Calculates the product of this Vector with the given factor Vector and
     * stores it in this Vector, with values result(i) = this(i) * factor(i).
     * @param factor A factor Vector to perform the multiplication with this
     * Vector.
     * @return A reference to this Vector with the result of the multiplication
     * of it with the given factor Vector, for use in chain calls, with values
     * result(i) = this(i) * factor(i).
     * @throws IllegalArgumentException If the given Vector's' don't have the
     * same size.
     */
    @Contract(pure = false)
    public @NotNull Vector multiplyFlat(@NotNull Vector factor) {
        Vector.multiplyFlat(this, factor, this);
        return this;
    }

    /**
     * Multiplies all the coordinate values of this Vector with the given value.
     * @param value A value to multiply with all the coordinate values of this
     * Vector.
     * @return A reference to this Vector with the result of the multiplication
     * of it with the given value, for use in chain calls.
     */
    @Contract(pure = false)
    public @NotNull Vector multiply(double value) {
        this.setAll(i -> this.get(i) * value);
        return this;
    }

    /**
     * Calculates the quotient of this Vector with the given divisor Vector and
     * stores it in this Vector, with values result(i) = this(i) / divisor(i).
     * @param divisor A divisor Vector to perform the division with this Vector.
     * @return A reference to this Vector with the result of the division of it
     * with the given divisor Vector, for use in chain calls, with values
     * result(i) = this(i) / divisor(i).
     * @throws IllegalArgumentException If the given Vector's' don't have the
     * same size.
     */
    @Contract(pure = false)
    public @NotNull Vector divideFlat(@NotNull Vector divisor) {
        Vector.divideFlat(this, divisor, this);
        return this;
    }

    /**
     * Divides all the coordinate values of this Vector with the given divisor
     * value.
     * @param divisor A value to divide with all the coordinate values of this
     * Vector.
     * @return A reference to this Vector with the result of the division of it
     * with the given value, for use in chain calls.
     */
    @Contract(pure = false)
    public @NotNull Vector divide(double divisor) {
        return this.multiply(1.0 / divisor);
    }

    /**
     * Sets all the coordinate values of this Vector to 0.0.
     */
    public void clear() {
        this.setAll(i -> 0.0);
    }

    /**
     * Calculates the dot product of this Vector and a given one.
     * @param other A Vector to find its dot product with this Vector.
     * @return The dot product of this Vector with the given one.
     * @throws IllegalArgumentException If the given Vector does not have the
     * same size as this Vector.
     */
    public double dotProduct(@NotNull Vector other) {
        return Vector.dotProduct(this, other);
    }

    /**
     * Calculates the p-norm of this Vector.
     * @param p The p parameter in the p-norm of this Vector.
     * @return The p-norm of this Vector.
     */
    public double pNorm(final double p) {
        return Math.pow(Arrays.stream(this.coordinates)
                   .parallel()
                   .map(x -> Math.pow(Math.abs(x), p))
                   .sum(), 1.0 / p);
    }

    /**
     * Calculates the 1-norm of this Vector.
     * @return The 1-norm of this Vector.
     */
    public double norm1() {
        return Arrays.stream(this.coordinates)
                     .parallel()
                     .map(Math::abs)
                     .sum();
    }

    /**
     * Calculates the 2-norm of this Vector.
     * @return The 2-norm of this Vector.
     */
    public double norm2() {
        return Math.sqrt(Arrays.stream(this.coordinates)
                   .parallel()
                   .map(x -> x * x)
                   .sum());
    }

    /**
     * Calculates the infinity-norm of this Vector.
     * @return The infinity-norm of this Vector.
     */
    public double normInf() {
        return Arrays.stream(this.coordinates)
                     .parallel()
                     .map(Math::abs)
                     .max()
                     .getAsDouble();
    }

    /**
     * Calculates the magnitude/length/2-norm of this Vector.
     * @return The magnitude/length/2-norm of this Vector.
     */
    public double magnitude() {
        return this.norm2();
    }

    /**
     * Normalizes this Vector so its magnitude will be 1.0.
     * @return This Vector for chain calls.
     */
    public @NotNull Vector normalize() {
        return this.divide(this.magnitude());
    }

    @Override
    public String toString() {
        return Arrays.toString(this.coordinates);
    }

}//end class Vector