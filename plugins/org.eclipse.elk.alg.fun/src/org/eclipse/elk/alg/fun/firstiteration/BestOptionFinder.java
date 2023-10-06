package org.eclipse.elk.alg.fun.firstiteration;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.elk.alg.fun.util.DrawingData;

/**
 * Compares placement options, i.e. different drawings and offers methods to find the best options out of the given
 * ones.
 * 
 * @author dalu
 */
public final class BestOptionFinder {

    private BestOptionFinder() {
    }

    /**
     * Filters the given list for the element with the aspect ratio that is closest to the given desired aspect ratio.
     * Returns a list with the elements that have smallest deviation to the desired aspect ratio as multiple options may
     * have the same aspect ratio.
     * 
     * @param candidates
     *            List of still considerable options.
     * @param dar
     *            Desired aspect ratio.
     * @param absoluteAR
     *            Indicates whether best aspect ratio should be determined by absolute difference to desired aspect
     *            ratio or ratio.
     * @return List of elements with aspect ratio being as close to the desired aspect ratio as possible.
     */
    public static List<DrawingData> findBestAspectRatio(final List<DrawingData> candidates, final double dar,
            final boolean absoluteAR) {
        List<DrawingData> remainingCandidates = new ArrayList<DrawingData>();
        if (absoluteAR) {
            // ALTERNATIVE ASPECT RATIO CALCULATION. Measures absolute difference.
            double smallestDeviation = Double.MAX_VALUE;
            for (DrawingData opt : candidates) {
                smallestDeviation = Math.min(smallestDeviation, Math.abs(opt.getAspectRatio() - dar));
            }
            for (DrawingData candidate : candidates) {
                if (Math.abs(candidate.getAspectRatio() - dar) == smallestDeviation) {
                    remainingCandidates.add(candidate);
                }
            }
            return remainingCandidates;
        } else {
            // Measurement measures the ratio of actual aspect ratio to desired aspect ratio and the smallest ratio is
            // the best, see https://rtsys.informatik.uni-kiel.de/confluence/pages/viewpage.action?pageId=45318210
            double smallestRatio = Double.MAX_VALUE;
            for (DrawingData opt : candidates) {
                if (opt.getAspectRatio() < dar) {
                    smallestRatio = Math.min(smallestRatio, dar / opt.getAspectRatio());
                } else {
                    smallestRatio = Math.min(smallestRatio, opt.getAspectRatio() / dar);
                }
            }
            for (DrawingData candidate : candidates) {
                if (candidate.getAspectRatio() < dar) {
                    if (dar / candidate.getAspectRatio() == smallestRatio) {
                        remainingCandidates.add(candidate);
                    }
                } else {
                    if (candidate.getAspectRatio() / dar == smallestRatio) {
                        remainingCandidates.add(candidate);
                    }
                }
            }
            return remainingCandidates;
        }
    }

    /**
     * Filters the given list for the element with the minimal area. Returns a list with the elements that have the
     * smallest area as multiple options may have the same area.
     * 
     * @param candidates
     *            List of still considerable options.
     * @return List of elements with the smallest area.
     */
    public static List<DrawingData> findMinArea(final List<DrawingData> candidates) {
        List<DrawingData> remainingCandidates = new ArrayList<DrawingData>();
        double minArea = Double.MAX_VALUE;
        for (DrawingData opt : candidates) {
            minArea = Math.min(minArea, opt.getArea());
        }
        for (DrawingData candidate : candidates) {
            if (candidate.getArea() == minArea) {
                remainingCandidates.add(candidate);
            }
        }
        return remainingCandidates;
    }

    /**
     * Filters the given list for the element with the maximum scale measure. Returns a list with the elements that have
     * the biggest scale measure as multiple options may have the same scale measure.
     * 
     * @param candidates
     *            List of still considerable options.
     * @return List of elements with the highest scale measure.
     */
    public static List<DrawingData> findMaxScale(final List<DrawingData> candidates) {
        List<DrawingData> remainingCandidates = new ArrayList<DrawingData>();
        double maxScale = Double.MIN_VALUE;
        for (DrawingData opt : candidates) {
            maxScale = Math.max(maxScale, opt.getScaleMeasure());
        }
        for (DrawingData candidate : candidates) {
            if (candidate.getScaleMeasure() == maxScale) {
                remainingCandidates.add(candidate);
            }
        }
        return remainingCandidates;
    }

}
