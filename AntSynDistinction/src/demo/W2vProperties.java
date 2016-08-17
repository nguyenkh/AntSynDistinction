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
        
        // TRAIN DIR
        String sTrainDir = properties.getProperty("STrainDirName");
        String sTrainDirPath = projectDir + "/" + sTrainDir;
        properties.setProperty("STrainDir", sTrainDirPath);
        
        String outputDir = projectDir;
        properties.setProperty("OutputDir", outputDir);
        
        // OUTPUT NAME
        String sOutputName = properties.getProperty("SOutputFileTemplate");
        String sOutputFilePath = outputDir + "/" + sOutputName;
        properties.setProperty("SOutputFile", sOutputFilePath);
        
        // VOCAB FILE
        String vocabFileName = properties.getProperty("VocabFileName");
        String vocabFile = outputDir + "/" + vocabFileName;
        properties.setProperty("VocabFile", vocabFile);
        
        // OUTPUT W2V
        
        String wordVectorFileName = properties.getProperty("WordVectorFileName");
        String wordVectorFilePath = outputDir + "/" + wordVectorFileName;
        properties.setProperty("WordVectorFile", wordVectorFilePath);
    }
    
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
}
