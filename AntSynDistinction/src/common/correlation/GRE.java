package common.correlation;

//import java.io.IOException;
import java.util.ArrayList;

import space.RawSemanticSpace;
import common.IOUtils;

public class GRE {
    String[][] dataset;
    public GRE(String greFile) {
        ArrayList<String> data = IOUtils.readFile(greFile);
        dataset = new String[data.size()][7];
        for (int i = 0; i < data.size(); i++) {
            String dataPiece = data.get(i);
            String[] elements = dataPiece.split("\t");
            for (int j = 0; j < elements.length; j++) {
                dataset[i][j] = elements[j];
            }
        }
    }
    
    public double evaluation(RawSemanticSpace space) {
        int rightNum = 0;
        int skipNum = 0;
        int quesNum = 0;
        for (int i = 0; i < dataset.length; i++) {
            String[] tuple = dataset[i];
            quesNum++;
            if (!space.contains(tuple[0]) || !space.contains(tuple[tuple.length-1])) {
                skipNum++;
                //continue;
            }
            else {
                double min = space.getSim(tuple[0], tuple[0]);
                int index = 0;
                for (int j = 1; j < tuple.length-1; j++) {
                    if (space.contains(tuple[j])) {
                        double sim = space.getSim(tuple[0], tuple[j]);
                        if (min > sim) {
                            min = sim;
                            index = j;
                        }
                    }
                }
                if (tuple[index].equals(tuple[tuple.length-1])) rightNum++;
            }
            /*double minSim = 0.0;
            int index = 0;
            for (int j = 1; j < 6; j++) {
                if (!space.contains(tuple[j])) continue;
                minSim = space.getSim(tuple[0], tuple[j]); 
                index = j;
                break;
            }
            String target = tuple[index];
            if (index == 5) {
                rightNum++;
                continue;
            }
            for (int j = index + 1; j < 6; j++) {
                if (!space.contains(tuple[j])) continue;
                double sim = space.getSim(tuple[0], tuple[j]);
                if (minSim < sim) {
                    minSim = sim;
                    target = tuple[j];
                }
            }
            if (target.equals(tuple[6])) rightNum += 1;*/
        }
        System.out.println("The number of right answers: " + rightNum);
        System.out.println("The number of skipped questions: " + skipNum);
        return rightNum / (double)(quesNum - skipNum);
    }
}
