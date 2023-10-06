package org.eclipse.elk.alg.fun.util;

/**Useful math functions.
 * 
 * @author dalu
 */
public class MathFunctions {

    /**
     * Calculates the sum of every integer smaller than the given integer. E.g. : Given integer of 4, 3+2+1=6.
     * 
     */
    public static int sumOfSeries(final int integer) {
        int result = 0;
        for (int i = 1; i < integer; i++) {
            result += i;
        }
        return result;
    }
}
