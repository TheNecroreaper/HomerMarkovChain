import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;

public class HomerMarkovChain {
    public static Random randomGen;
    private static class WordMarkov{
        int frequency;
        HashMap<String, Integer> wordFrequency;
        public WordMarkov(){
            frequency = 0;
            wordFrequency = new HashMap<String, Integer>();
        }
        public WordMarkov(String word){
            this();
            addWord(word);
        }
        public WordMarkov addWord(String word){
            frequency++;
            wordFrequency.merge(word, 1, (prev, one) -> prev + one);
            return this;
        }
        public String getNextWord(){
            int tempFreq = randomGen.nextInt(frequency);
            Iterator<String> wordSelector = wordFrequency.keySet().iterator();
            String word = wordSelector.next();
            tempFreq -= wordFrequency.get(word);
            while(wordSelector.hasNext() && tempFreq > 0){
                word = wordSelector.next();
                tempFreq -= wordFrequency.get(word);
            }
            return word;
        }

        @Override
        public String toString() {
            return "WordMarkov{" +
                    "frequency=" + frequency +
                    ", wordFrequency=" + wordFrequency +
                    '}';
        }
    }

    public static WordMarkov firstWords = new WordMarkov();

    public static void main(String args[]){
        HashMap<String, WordMarkov> homerWords = createTable();
//        for(String key: homerWords.keySet()){
//            System.out.println(key + " " + homerWords.get(key));
//        }
        randomGen = new Random(System.currentTimeMillis());
        writeHomer(250, homerWords);
    }

    public static void writeHomer(int words, HashMap<String, WordMarkov> homerWords){
        File output = new File("HomerGen.txt");
        try {
            output.createNewFile();
            FileWriter outputWriter = new FileWriter(output);
            int newLine = 100;
            String word = firstWords.getNextWord();
            outputWriter.write(word);
            while(words > 0){
                String nextWord = homerWords.get(word).getNextWord();
//                char lastChar = nextWord.toLowerCase().charAt(nextWord.length() - 1);
                if(!nextWord.contains(".")) {
                    outputWriter.write(" ");
                }
                if(newLine < 0){
                    newLine = 100;
                    outputWriter.write("\n");
                }
                newLine -= nextWord.length();
                outputWriter.write(nextWord);
                words--;
            }
            outputWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static HashMap<String, WordMarkov> createTable(){
        File odysseyFile = new File("Odyssey");
        File iliadFile = new File("Iliad");
        HashMap<String, WordMarkov> homerWords = new HashMap<String, WordMarkov>();
        readFile(odysseyFile, homerWords);
        readFile(iliadFile, homerWords);
        return homerWords;
    }
    
    public static void readFile(File file, HashMap<String, WordMarkov> words){
        try {
            Scanner inputScanner = new Scanner(file);
            //reads in each word
            boolean newSentence = true;
            String prevWord = "";
            while(inputScanner.hasNext()){
                String word = inputScanner.next();
                //starts adding followers for new sentences
                if(newSentence){
                    firstWords.addWord(word);
                    newSentence = false;
                }
                else {
                    if (word.charAt(word.length() - 1) == '.') {
                        newSentence = true;
                        words.merge(word, new WordMarkov("."), (prev, addedWord) -> prev.addWord("."));
                    }
                    if (word.contains(".")) {
                        newSentence = true;
                        word = word.replaceAll(".", "");
                        words.merge(word, new WordMarkov("."), (prev, addedWord) -> prev.addWord("."));
                    }
                    String newWord = word.replaceAll("[^a-zA-Z0-9]", "");
                    //if the length has changed, the word had weird characters
                    if (newWord.length() != word.length() && newWord.length() > 0) {
                        newSentence = true;
                        String punctuation = word.substring(newWord.length() - 1);
                        words.merge(newWord, new WordMarkov(punctuation), (prev, addedWord) -> prev.addWord(punctuation));
                    }
                    words.putIfAbsent(newWord, new WordMarkov());
                    words.put(newWord, words.get(newWord).addWord(prevWord));
                    word = newWord;
                }
                prevWord = word;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
