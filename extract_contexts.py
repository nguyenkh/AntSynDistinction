import argparse
import spacy
from spacy.en import English
import gzip
from collections import Counter
import numpy as np
import math

def main():
    """
    TODO: extracts the relation between target and contexts in the window-size and computes LMI
    Usage: python create_contexts.py -input <corpus_name> -output <output-file-name>
    """
    parser = argparse.ArgumentParser()
    parser.add_argument('-input', type=str)
    parser.add_argument('-output', type=str)
    args = parser.parse_args()
    
    nlp = English()
    
    window_size = 5
    nouns = Counter()
    verbs = Counter()
    adjectives = Counter()
    freqs = Counter()
    
    output_dir = '/mount/arbeitsdaten34/projekte/slu/KimAnh/'
    with gzip.open(args.input,'rb') as fin:
        para_num = 0
        # Read each paragraph in corpus
        for paragraph in fin:
            # Check empty paragraph
            paragraph = paragraph.strip()
            if len(paragraph) == 0: continue
            para_num += 1
            print 'Processing para: %d' %para_num
            # Parse each sentence
            parsed_para = nlp(unicode(paragraph))
            for sent in parsed_para.sents:
                noun, verb, adj, freq = process_one_sentence(sent, window_size)
                nouns.update(noun)
                verbs.update(verb)
                adjectives.update(adj)
                freqs.update(freq)
                
    print 'Parsing corpus done....!'
    
    new_nouns, new_verbs, new_adjectives = calc_lmi(nouns, verbs, adjectives, freqs) 
    print 'Computing LMI done.....!'          
    # Write to file
    with gzip.open(output_dir  + args.output + '_noun', 'wb') as fnoun:
        for pair, value in new_nouns.iteritems():
            st = '\t'.join([pair[0], pair[1], '\t'.join([str(value[0]), str(value[1])])])
            fnoun.write(st + '\n')
        
    with gzip.open(output_dir  + args.output + '_verb', 'wb') as fverb:
        for pair, value in new_verbs.iteritems():
            st = '\t'.join([pair[0], pair[1], '\t'.join([str(value[0]), str(value[1])])])
            fverb.write(st + '\n')
            
    with gzip.open(output_dir  + args.output + '_adj', 'wb') as fadj:
        for pair, value in new_adjectives.iteritems():
            st = '\t'.join([pair[0], pair[1], '\t'.join([str(value[0]), str(value[1])])])
            fadj.write(st + '\n')
            
    print 'Done.........!'
                    
                    
def process_one_sentence(sent, window_size):
    noun = Counter()
    verb = Counter()
    adj = Counter()
    freq = Counter()
    
    for idx,token in enumerate(sent):
        if token.tag_[:2] == 'NN' and len(token.string.strip()) > 2:
            for idw in range(idx-window_size, idx+window_size):
                if idw != idx and idw >= 0 and idw < len(sent): 
                    noun[(sent[idx], sent[idw])] += 1
                    freq[sent[idx]] += 1
                    freq[sent[idw]] += 1
                    
        elif token.tag_[:2] == 'VB' and len(token.string.strip()) > 2:
            for idw in range(idx-window_size, idx+window_size):
                if idw != idx and idw >= 0 and idw < len(sent): 
                    verb[(sent[idx], sent[idw])] += 1
                    freq[sent[idx]] += 1
                    freq[sent[idw]] += 1
                    
        elif token.tag_[:2] == 'JJ' and len(token.string.strip()) > 2:
            for idw in range(idx-window_size, idx+window_size):
                if idw != idx and idw >= 0 and idw < len(sent): 
                    adj[(sent[idx], sent[idw])] += 1
                    freq[sent[idx]] += 1
                    freq[sent[idw]] += 1
                    
    return noun, verb, adj, freq

def calc_lmi(nouns, verbs, adjectives, freqs):
    total_freqs = np.sum(freqs.values())
    
    for pair, value in nouns.iteritems():
        o_11 = value
        r_1 = freqs[pair[0]]
        c_1 = freqs[pair[1]]
        e_11 = (r_1 * c_1) / float(total_freqs)
        lmi = o_11 * math.log(o_11 / e_11)
        nouns[pair] = [value, lmi]
    
    for pair, value in verbs.iteritems():
        o_11 = value
        r_1 = freqs[pair[0]]
        c_1 = freqs[pair[1]]
        e_11 = (r_1 * c_1) / float(total_freqs)
        lmi = o_11 * math.log(o_11 / e_11)
        verbs[pair] = [value, lmi] 
    
    for pair, value in adjectives.iteritems():
        o_11 = value
        r_1 = freqs[pair[0]]
        c_1 = freqs[pair[1]]
        e_11 = (r_1 * c_1) / float(total_freqs)
        lmi = o_11 * math.log(o_11 / e_11)
        adjectives[pair] = [value, lmi]
        
    return nouns, verbs, adjectives

if __name__=='__main__':
    main()
    
    
    
