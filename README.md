# Antonym-Syntonym Distinction
Kim Anh Nguyen, nguyenkh@ims.uni-stuttgart.de

Code for the paper [Integrating Distributional Lexical Contrast into Word Embeddings for Antonym-Synonym Distinction](http://www.ims.uni-stuttgart.de/institut/mitarbeiter/anhnk/papers/ant-syn-distinction.pdf) (ACL 2016). For more details please refer to Nguyen et al. (2016).

### Lexical Contrast Information:

- Antonym files (adj, noun, verb): Each line contains one target word and its antonyms as follows (tab delimited):

  ```good bad evil```
- Synonym files (adj, noun, verb): Each line contains one target word and its synonyms as follows (tab delimited):

  ```good	practiced	expert	skillful	in-force	well	estimable	secure	beneficial	unspoilt	dear	honest...```

- Context files (adj, noun, verb) that refer as W(c) in the Equation 3: Each line contains one context word (only considers adj, noun, verb as contexts and target words) and its target (tab delimited). Considering two sentences, for example, ```The cat sat on the mat``` and ```The dog sat on the matress```, the context ```sat``` is collected as follows (window size = 5):

  ```sat  cat mat dog matress```
  
- Corpus: a plain-text corpus is used to train word embeddings.

### Compile



### Running model

### Reference
```
@InProceedings{nguyen:2016:antsyn
  author    = {Nguyen, Kim Anh and Schulte im Walde, Sabine and Vu, Ngoc Thang},
  title     = {Integrating Distributional Lexical Contrast into Word Embeddings for Antonym-Synonym Distinction},
  booktitle = {Proceedings of the 54th Annual Meeting of the Association for Computational Linguistics (ACL)},
  year      = {2016},
}
```
