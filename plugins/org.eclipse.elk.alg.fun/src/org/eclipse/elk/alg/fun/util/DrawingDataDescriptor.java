package org.eclipse.elk.alg.fun.util;

/**
 * Enumerate that specifies the placement option of a rectangle
 * 
 * @author dalu
 */
public enum DrawingDataDescriptor {
    /** Placing it on the right of the lastly placed node. */
    PLACEMENT_OPTION_LAST_PLACED_RIGHT,
    /** Placing it below the lastly placed node. */
    PLACEMENT_OPTION_LAST_PLACED_BELOW,
    /** Placing it on the right of the whole drawing. */
    PLACEMENT_OPTION_WHOLE_DRAWING_RIGHT,
    /** Placing it below the whole drawing. */
    PLACEMENT_OPTION_WHOLE_DRAWING_BELOW,
    /** Indicating that a Drawing Data object refers to a actual drawing and not a placement option. */
    WHOLE_DRAWING
}
