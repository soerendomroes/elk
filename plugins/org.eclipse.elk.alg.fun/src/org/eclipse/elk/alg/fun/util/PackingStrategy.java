package org.eclipse.elk.alg.fun.util;

/**
 * Specifies the strategy employed to pack rectangles sequentially.
 * 
 * @author dalu
 */
public enum PackingStrategy {
    /** Aspect ratio-driven packing heuristic. */
    ASPECT_RATIO_DRIVEN,
    /** Max scale-driven packing heuristic. */
    MAX_SCALE_DRIVEN,
    /** Area driven packing heuristic. */
    AREA_DRIVEN;
}