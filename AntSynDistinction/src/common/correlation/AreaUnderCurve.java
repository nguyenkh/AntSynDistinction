package common.correlation;

import java.util.Arrays;

import common.exception.ValueException;

public class AreaUnderCurve {
    public static double computeAUC(double[] golds, double[] predicted) {
        int positive = 0;
        for (double score: golds) {
            if (score == 1) positive++;
        }
        int negative = golds.length - positive;

        int total_count = golds.length;
        Point[] point_set = new Point[total_count];
        for (int i = 0; i < golds.length; i++) {
            if (!(golds[i]==1) && !(golds[i] == 0)) {
                throw new ValueException("For evaluating AUC, gold scores are required to be 0 or 1.");
            }
            point_set[i] = new Point(golds[i], predicted[i]);
        }

        Arrays.sort(point_set);

        double xi = 1.0;
        double yi = 1.0;
        double xi_old = 1.0;
        double true_positive = positive;
        double false_positive = negative;
        double auc = 0;

        for (int i = 0; i < total_count; i++) {
            if (point_set[i].gold == 1) {
                true_positive -= 1;
                yi = true_positive / positive;
            } else {
                false_positive -= 1;
                xi = false_positive / negative;
                auc += (xi_old - xi) * yi;
                xi_old = xi;
            }
        }
        return auc;
    }
    
    
    static class Point implements Comparable<Point>{
        double gold;
        double score;
        public Point(double gold, double score) {
            this.gold = gold;
            this.score = score;
        }
        @Override
        public int compareTo(Point o) {
            // TODO Auto-generated method stub
            if (this.score > o.score) return 1;
            if (this.score < o.score) return -1;
            return 0;
        }
    }
}
