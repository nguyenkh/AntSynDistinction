package space;

import java.util.Comparator;

public class Neighbor {
    public String word;
    public double sim;

    public Neighbor(String word, double sim) {
        this.word = word;
        this.sim = sim;
    }

    public static Comparator<Neighbor> NeighborComparator = new Comparator<Neighbor>() {

                                                              @Override
                                                              public int compare(
                                                                      Neighbor o1,
                                                                      Neighbor o2) {
                                                                  if (o1.sim > o2.sim) {
                                                                      return -1;
                                                                  } else if (o1.sim == o2.sim) {
                                                                      return 0;
                                                                  } else {
                                                                      return 1;
                                                                  }
                                                              }

                                                          };
}
