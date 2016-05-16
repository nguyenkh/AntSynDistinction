package demo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class W2vProperties{
    protected Properties properties;
    public W2vProperties(String configFile) throws IOException{
        

        properties = new Properties();
        BufferedReader reader = new BufferedReader(new FileReader(configFile));
        properties.load(reader);
        reader.close();
        
        
        // PROJECT DIR        
        String projectDir = properties.getProperty("ProjectDir");
        
        // CONSTRUCTION FILE
        
        String constructionFileName = properties.getProperty("ConstructionFileName");
        String constructionFilePath = projectDir + "/" + constructionFileName;
        properties.setProperty("ConstructionFile", constructionFilePath);
        
        // TRAIN DIR
        String sTrainDir = properties.getProperty("STrainDirName");
        String sTrainDirPath = projectDir + "/" + sTrainDir;
        properties.setProperty("STrainDir", sTrainDirPath);
        
        String outputDir = projectDir + "/output";
        properties.setProperty("OutputDir", outputDir);
        String logDir = projectDir + "/log";
        properties.setProperty("LogDir", logDir);
        
        // OUTPUT NAME
        String sOutputName = properties.getProperty("SOutputFileTemplate");
        String sOutputFilePath = outputDir + "/" + sOutputName;
        properties.setProperty("SOutputFile", sOutputFilePath);
        String sLogFilePath = logDir + "/" + sOutputName;
        properties.setProperty("SLogFile", sLogFilePath);
        
        // VOCAB FILE
        String vocabFileName = properties.getProperty("VocabFileName");
        String vocabFile = outputDir + "/" + vocabFileName;
        properties.setProperty("VocabFile", vocabFile);
        
        // OUTPUT W2V
        
        String wordVectorFileName = properties.getProperty("WordVectorFileName");
        String wordVectorFilePath = outputDir + "/" + wordVectorFileName;
        properties.setProperty("WordVectorFile", wordVectorFilePath);
        properties.setProperty("WordModelFile", wordVectorFilePath.replace(".bin", ".mdl"));
        String wordLogFilePath = logDir + "/" + wordVectorFileName;
        properties.setProperty("WordLogFile", wordLogFilePath);
        
        // SICK FILE 
        // MEN FILE
        // LAPATA DIR
        
        String datasetDir = properties.getProperty("DatasetDir");
        String anFile     = datasetDir + "/an_lemma.txt";
        String nnFile     = datasetDir + "/nn_lemma.txt";
        String svFile     = datasetDir + "/sv_lemma.txt";
        String voFile     = datasetDir + "/vo_lemma.txt";
        properties.setProperty("ANFile", anFile);
        properties.setProperty("NNFile", nnFile);
        properties.setProperty("SVFile", svFile);
        properties.setProperty("ANFile", voFile);
        
        String rteDir  = projectDir + "/rte";
        String rteFile  = rteDir + "/SICK_train_trial.txt";
        String rteFeatureFile          = rteDir + "feature/SICK_train_trial.txt";
        String rteSvmFile              = rteDir + "svm/";
        
        properties.setProperty("RteFile", rteFile);
        properties.setProperty("RteFeatureFile", rteFeatureFile);
        properties.setProperty("RteSvmFile", rteSvmFile);
        
        String imdbDir  = projectDir + "/imdb";
        String imdbFile  = imdbDir + "/SICK_train_trial.txt";
        String imdbFeatureFile          = imdbDir + "feature/SICK_train_trial.txt";
        String imdbSvmFile              = imdbDir + "svm/";
        
        properties.setProperty("ImbdFile", imdbFile);
        properties.setProperty("ImbdFeatureFile", imdbFeatureFile);
        properties.setProperty("ImbdSvmFile", imdbSvmFile);
    }
    
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
}
