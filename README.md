## Antonym-Syntonym Distinction
Kim Anh Nguyen, nguyenkh@ims.uni-stuttgart.de

Code for the paper [Integrating Distributional Lexical Contrast into Word Embeddings for Antonym-Synonym Distinction](http://www.ims.uni-stuttgart.de/institut/mitarbeiter/anhnk/papers/acl2016/ant-syn-distinction.pdf) (ACL 2016). For more details please refer to Nguyen et al. (2016).

### Lexical Contrast Information:

- Antonym files (adj, noun, verb): Each line contains one target word and its antonyms as follows (tab delimited):

  ```good bad evil```
- Synonym files (adj, noun, verb): Each line contains one target word and its synonyms as follows (tab delimited):

  ```good	practiced	expert	skillful	in-force	well	estimable	secure	beneficial	unspoilt	dear	honest...```

- Context files (adj, noun, verb) that refer as W(c) in the Equation 3:
  
  - To create files of features, the system requires the [spaCy](https://spacy.io) tool.   

  - Extract relations between target and contexts: 

    ```python create_contexts.py -input <corpus_file> -output <output-file-name>```
  
  - Create features (across adj, noun, verb): 
    
    ```python create_features.py -input <contexts_file> -output <features_file>```
  
- Corpus: a plain-text corpus is used to train word embeddings.

### Configuration

See config.cfg to set agruments for model.

### Running model

Command line:

  ```java -jar dLCE.jar config.cfg vector-size window-size adj-boolean noun-boolean verb-boolean iteration```
  
For example, training model with 300 dimensions; window-size = 5; lexical contrast of adj, noun, verb; and 3 iterations:

  ```java -jar dLCE.jar config.cfg 300 5 True True True 3```
  
### Pre-trained embeddings
- Wikipedia corpus, 100dim, min-count=100: [dLCE_100d_minFreq_100](http://www.ims.uni-stuttgart.de/data/dLCE/wiki_en_dLCE_100d_minFreq_100.bin)
- Wikipedia corpus, 100dim, min-count=5: [dLCE_100d_minFreq_5](http://www.ims.uni-stuttgart.de/data/dLCE/wiki_en_dLCE_100d_minFreq_5.bin)

### Reference
```
@InProceedings{nguyen:2016:antsyn
  author    = {Nguyen, Kim Anh and Schulte im Walde, Sabine and Vu, Ngoc Thang},
  title     = {Integrating Distributional Lexical Contrast into Word Embeddings for Antonym-Synonym Distinction},
  booktitle = {Proceedings of the 54th Annual Meeting of the Association for Computational Linguistics (ACL)},
  year      = {2016},
}
```
