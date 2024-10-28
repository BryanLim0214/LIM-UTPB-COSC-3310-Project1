/**
 * <h1>UInt</h1>
 * Represents an unsigned integer using a boolean array to store the binary representation.
 * Each bit is stored as a boolean value, where true represents 1 and false represents 0.
 * Supports basic arithmetic, bitwise operations, and binary conversions.
 *
 * @version 1.0 (Sept 30, 2024)
 */
import java.util.Arrays;

public class UInt {

    // Array representing the bits of the unsigned integer.
    protected boolean[] bits;

    // Number of bits used to represent the unsigned integer.
    protected int length;

    // Constructors

    /**
     * Constructs a new UInt by cloning an existing UInt object.
     *
     * @param toClone The UInt object to clone.
     */
    public UInt(UInt toClone) {
        this.length = toClone.length;
        this.bits = Arrays.copyOf(toClone.bits, this.length);
    }

    /**
     * Constructs a new UInt from an integer value.
     * Converts the integer to its binary representation and stores it in the bits array.
     *
     * @param i The integer value to convert to a UInt.
     */
    public UInt(int i) {
        if (i == 0) {
            length = 1;
            bits = new boolean[length];
            bits[0] = false;
        } else {
            length = (int)(Math.ceil(Math.log(i) / Math.log(2.0)) + 1);
            bits = new boolean[length];

            // Populate the bits array with the binary representation of i.
            for (int b = length - 1; b >= 0; b--) {
                bits[b] = i % 2 == 1;
                i = i >> 1;
            }
        }
    }

    // Cloning Methods

    /**
     * Creates and returns a copy of this UInt object.
     *
     * @return A new UInt object that is a clone of this instance.
     */
    @Override
    public UInt clone() {
        return new UInt(this);
    }

    /**
     * Creates and returns a copy of the specified UInt object.
     *
     * @param u The UInt object to clone.
     * @return A new UInt object that is a copy of the given object.
     */
    public static UInt clone(UInt u) {
        return new UInt(u);
    }

    // Conversion Methods

    /**
     * Converts this UInt to its integer representation.
     *
     * @return The integer value corresponding to this UInt.
     */
    public int toInt() {
        int result = 0;
        for (int i = 0; i < length; i++) {
            // Shift each bit to build up the integer result
            result = result + (bits[i] ? 1 : 0);
            result = result << 1;
        }
        return result >> 1; // Final adjustment to shift back
    }

    /**
     * Static method to retrieve the integer value from a generic UInt object.
     *
     * @param u The UInt to convert.
     * @return The integer value represented by u.
     */
    public static int toInt(UInt u) {
        return u.toInt();
    }

    /**
     * Returns a String representation of this binary object with a leading 0b.
     *
     * @return The binary representation as a string.
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("0b");
        for (int i = 0; i < length; i++) {
            s.append(bits[i] ? "1" : "0");
        }
        return s.toString();
    }

    // Bitwise Operations

    /**
     * Performs a logical AND operation with this.bits and u.bits, storing the result in this.bits.
     *
     * @param u The UInt to AND with this.
     */
    public void and(UInt u) {
        for (int i = 0; i < Math.min(this.length, u.length); i++) {
            this.bits[this.length - i - 1] &= u.bits[u.length - i - 1];
        }
        if (this.length > u.length) {
            // Clear any remaining bits if this length is greater than u's length
            for (int i = u.length; i < this.length; i++) {
                this.bits[this.length - i - 1] = false;
            }
        }
    }

    /**
     * Static method to safely perform an AND operation without modifying operands.
     *
     * @param a The first UInt.
     * @param b The second UInt.
     * @return A new UInt object containing the result of the AND operation.
     */
    public static UInt and(UInt a, UInt b) {
        UInt temp = a.clone();
        temp.and(b);
        return temp;
    }

    public void or(UInt u) {
        for (int i = 0; i < Math.min(this.length, u.length); i++) {
            this.bits[this.length - i - 1] |= u.bits[u.length - i - 1];
        }
    }

    public static UInt or(UInt a, UInt b) {
        UInt temp = a.clone();
        temp.or(b);
        return temp;
    }

    public void xor(UInt u) {
        for (int i = 0; i < Math.min(this.length, u.length); i++) {
            this.bits[this.length - i - 1] ^= u.bits[u.length - i - 1];
        }
    }

    public static UInt xor(UInt a, UInt b) {
        UInt temp = a.clone();
        temp.xor(b);
        return temp;
    }

    // Arithmetic Operations

    /**
     * Performs binary addition with padding adjustments based on operand length.
     *
     * @param u The UInt to add to this UInt.
     */
    public void add(UInt u) {
        int maxLength = Math.max(this.length, u.length);
        boolean[] paddedA = new boolean[maxLength];
        boolean[] paddedB = new boolean[maxLength];

        // Zero-pad this.bits and u.bits for addition
        Arrays.fill(paddedA, 0, maxLength - this.length, false);
        System.arraycopy(this.bits, 0, paddedA, maxLength - this.length, this.length);

        Arrays.fill(paddedB, 0, maxLength - u.length, false);
        System.arraycopy(u.bits, 0, paddedB, maxLength - u.length, u.length);

        boolean carry = false; // Keeps track of any carry during addition
        boolean[] result = new boolean[maxLength];
        for (int i = maxLength - 1; i >= 0; i--) {
            // Calculate sum with carry for each bit position
            result[i] = paddedA[i] ^ paddedB[i] ^ carry;
            carry = (paddedA[i] && paddedB[i]) || (carry && (paddedA[i] || paddedB[i]));
        }

        // Handle overflow if carry is true
        if (carry) {
            boolean[] extendedResult = new boolean[maxLength + 1];
            System.arraycopy(result, 0, extendedResult, 1, maxLength);
            extendedResult[0] = true;
            result = extendedResult;
        }

        this.bits = result;
        this.length = result.length;
    }

    /**
     * Adds a UInt to this UInt, allowing for sign extension if negative padding is required.
     *
     * @param u The UInt to add, supporting negative padding for signed addition.
     */
    private void addWithNegative(UInt u) {
        int maxLength = Math.max(this.length, u.length);
        boolean[] paddedA = new boolean[maxLength];
        boolean[] paddedB = new boolean[maxLength];

        // Extend the leftmost bit if necessary for sign extension
        Arrays.fill(paddedA, 0, maxLength - this.length, this.length > 1 ? this.bits[0] : false);
        System.arraycopy(this.bits, 0, paddedA, maxLength - this.length, this.length);

        Arrays.fill(paddedB, 0, maxLength - u.length, u.length > 1 ? u.bits[0] : false);
        System.arraycopy(u.bits, 0, paddedB, maxLength - u.length, u.length);

        boolean carry = false; // Keeps track of any carry during addition
        boolean[] result = new boolean[maxLength];
        for (int i = maxLength - 1; i >= 0; i--) {
            // Calculate sum with carry for each bit position
            result[i] = paddedA[i] ^ paddedB[i] ^ carry;
            carry = (paddedA[i] && paddedB[i]) || (carry && (paddedA[i] || paddedB[i]));
        }

        this.bits = result;
        this.length = maxLength;
    }

    public static UInt add(UInt a, UInt b) {
        UInt temp = a.clone();
        temp.add(b);
        return temp;
    }

    /**
     * Flips all bits and adds one to this UInt, effectively negating it.
     */
    public void negate() {
        // Flip all bits to get the one's complement
        for (int i = 0; i < length; i++) {
            bits[i] = !bits[i];
        }
        this.add(new UInt(1)); // Add one to complete two's complement negation
    }

    /**
     * Subtracts the provided UInt from this UInt using two's complement.
     *
     * @param b The UInt to subtract from this UInt.
     */
    public void sub(UInt b) {
        UInt negatedB = b.clone();
        negatedB.negate(); // Convert b to its two's complement (negative form)
        this.addWithNegative(negatedB); // Add the negative of b
        this.trimLengthForSubtraction(Math.max(this.length, b.length)); // Trim to appropriate length
    }

    private void trimLengthForSubtraction(int targetLength) {
        // Trim the length to the target, retaining only the necessary bits
        if (this.length > targetLength) {
            this.bits = Arrays.copyOf(this.bits, targetLength);
            this.length = targetLength;
        }
    }

    public static UInt sub(UInt a, UInt b) {
        UInt temp = a.clone();
        temp.sub(b);
        return temp;
    }

    /**
     * Multiplies this UInt by another UInt using Booth's algorithm.
     *
     * @param u The UInt to multiply by.
     */
    public void mul(UInt u) {
        // Determine the longer and shorter values
        UInt M = this.length >= u.length ? this.clone() : u.clone();
        UInt R = this.length < u.length ? this.clone() : u.clone();

        int maxLength = Math.max(M.length, R.length); // Find the maximum length to determine padding
        M.padToLength(maxLength); // Pad M to ensure it has the proper length
        UInt negM = M.clone();
        negM.negate(); // Get the negative of M

        R.padToLength(maxLength); // Pad R to match length

        // Prepare A and S for Booth's multiplication
        UInt A = new UInt(0);
        A.bits = new boolean[maxLength * 2 + 1];
        System.arraycopy(M.bits, 0, A.bits, 0, maxLength); // Set A to the value of M
        A.length = maxLength * 2 + 1;

        UInt S = new UInt(0);
        S.bits = new boolean[maxLength * 2 + 1];
        System.arraycopy(negM.bits, 0, S.bits, 0, maxLength); // Set S to the negative value of M
        S.length = maxLength * 2 + 1;

        // Initialize P, which holds the result of each step
        UInt P = new UInt(0);
        P.bits = new boolean[maxLength * 2 + 1];
        System.arraycopy(R.bits, 0, P.bits, maxLength, R.length); // Place R into the lower half of P
        P.length = maxLength * 2 + 1;

        int shiftCounter = M.length; // Set the counter to M's length

        // Perform the multiplication with Booth's algorithm
        while (shiftCounter > 0) {
            String lastTwoBits = P.getLastTwoBits();

            if (lastTwoBits.equals("00")) {
                // No action, just shift
                P = rightShift(P);
            } else if (lastTwoBits.equals("10")) {
                // Add -M (S) to P and shift
                P.addWithNegative(S);
                P = trimLengthForMultiplication(P, A.length); // Trim excess bits after addition
                P = rightShift(P);
            } else if (lastTwoBits.equals("01")) {
                // Add M (A) to P and shift
                P.add(A);
                P = trimLengthForMultiplication(P, A.length); // Trim excess bits after addition
                P = rightShift(P);
            } else if (lastTwoBits.equals("11")) {
                // No action, just shift
                P = rightShift(P);
            }

            shiftCounter--; // Decrement the counter after each operation
        }

        // Store the result by removing the two leftmost digits
        if (P.length > 2) {
            this.bits = Arrays.copyOfRange(P.bits, 2, P.length);
            this.length = P.length - 3;
        } else {
            this.bits = new boolean[]{false};
            this.length = 1;
        }
    }

    private UInt trimLengthForMultiplication(UInt u, int targetLength) {
        // Trim the length for multiplication, keeping only the necessary bits
        if (u.length > targetLength) {
            u.bits = Arrays.copyOfRange(u.bits, u.length - targetLength, u.length);
            u.length = targetLength;
        }
        return u;
    }

    // Utility and Helper Methods

    private UInt rightShift(UInt p) {
        UInt shifted = new UInt(0);
        shifted.bits = new boolean[p.length];
        shifted.bits[0] = p.bits[0]; // Preserve the sign bit if shifting for a signed integer
        System.arraycopy(p.bits, 0, shifted.bits, 1, p.length - 1); // Shift all bits to the right
        shifted.length = p.length;
        return shifted;
    }

    public String getLastTwoBits() {
        // Returns the last two bits as a string
        if (length < 2) {
            return bits[length - 1] ? "1" : "0";
        }
        String lastTwoBits = "" + (bits[length - 1] ? "1" : "0") + (bits[length - 2] ? "1" : "0");

        return lastTwoBits.equals("01") ? "10" : lastTwoBits.equals("10") ? "01" : lastTwoBits;
    }

    private void padToLength(int targetLength) {
        // Pads the bits array to the target length with leading zeros
        if (this.length < targetLength) {
            boolean[] paddedBits = new boolean[targetLength];
            int padding = targetLength - this.length;
            System.arraycopy(this.bits, 0, paddedBits, padding, this.length);
            this.bits = paddedBits;
            this.length = targetLength;
        }
    }

    public static UInt mul(UInt a, UInt b) {
        UInt temp = a.clone();
        temp.mul(b);
        return temp;
    }
}
