"""
Task: Antonym-Synonym Distinction (dLCE model)
TODO: extracts the set of features in which each feature consists a list of targets in window-size of 5 words.
Extracts features across adj, noun, verb
"""

import gzip
from collections import defaultdict
import argparse

def main():
    """
    Format of input file likes: target \t context \t Freq \t LMI
    Usage: python -input <contexts_file> -output <features_file>
    """
    parser = argparse.ArgumentParser()
    parser.add_argument('-input', type=str)
    parser.add_argument('-output', type=str)
    args = parser.parse_args()
    
    features = create_features(args.input)
    save_file(features, args.output)

def create_features(infile):
    features = defaultdict(list)
    with gzip.open(infile, 'rb') as f:
        for line in f:
            temp = line.strip().split('\t')
            features[temp[1]].append(temp[0])

    print 'Created feature: ' + str(len(features))
    return features

def save_file(features, outfile):              
    with gzip.open(outfile, 'wb') as f:         
        for e in features:
            f.write(e)
            values = features[e]
            for v in values:
                f.write('\t' + v)
            f.write('\n')
    print "Saved file!"

if __name__ == '__main__':
    main()
    
