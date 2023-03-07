package eu.nites.compressor.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Compress {
    private MultipartFile file;
    private String fileAsString;
    private Map<Character, Integer> stringAsChars;
    private char[] chars;
    private Map<Integer, Map<Character, Double>> probabilities = new HashMap<>();
    private Map<Integer, Map<Character, Double[]>> rangeValues = new HashMap<>();
    private Double[] range = new Double[]{(double) 0, (double) 1};
    private String code;
    private String filename;

    public Compress (MultipartFile file, String code) throws IOException {
        this.file = file;
        this.code = code;
        this.fileToString(this.file);
        this.stringToChars(this.fileAsString);
        this.makeProbabilities();
        this.run();
        this.makeFile();
    }

    public String getLink () {
        return this.filename;
    }

    // Choose new range for each character

    private void chooseRange (char character) {
        for (int i = 0; i < this.rangeValues.size(); i++) {
            for (char probability:this.rangeValues.get(i).keySet()) {
                if (character == probability) { // umesto for-a this.rangeValues.get(i).get(character)
                    this.range = new Double[]{this.rangeValues.get(i).get(probability)[0], this.rangeValues.get(i).get(probability)[1]};
                }
            }
        }
    }

    // Write new values between ranges and calculate it with probabilities

    private void makeBetweenRangeValues (char c) {
        this.rangeValues = new HashMap<>();
        Double prev = this.range[0];

        for (int i = 0; i < this.probabilities.size(); i++) {
            char pro_key = this.probabilities.get(i).keySet().toArray()[0].toString().charAt(0);
            Double probability = this.probabilities.get(i).get(pro_key);
            Double new_upper_limit = this.range[0] + ( ( this.range[1] - this.range[0] ) * probability );
            Map<Character, Double[]> range = new HashMap<>();
            Double[] arr = new Double[] {prev, new_upper_limit};
            range.put(pro_key, arr);
            this.rangeValues.put(i, range);
            prev = new_upper_limit;
        }

        this.chooseRange(c);

    }

    // Start making ranges for all probabilities and characters

    private void run () {
        for(int i = 0; i < this.chars.length; i++) {
            if (i == 0) {
                this.chooseRange(this.chars[i]);
            } else {
                this.makeBetweenRangeValues(this.chars[i]);
            }
        }
    }

    // Make probabilities and first range values

    private void makeProbabilities () {
        int i = 0;
        for(char c : this.stringAsChars.keySet()) {
            Double prev = (double) 0;
            if (i > 0) {
                char probability = this.probabilities.get(i - 1).keySet().toArray()[0].toString().charAt(0);
                prev = this.probabilities.get(i-1).get(probability);
            }
            Map<Character, Double> value = new HashMap<>();
            Map<Character, Double[]> range = new HashMap<>();
            Double after = ((double) this.stringAsChars.get(c) / this.chars.length) + prev;
            value.put(c, after);
            Double[] arr = new Double[] {prev, after};
            range.put(c, arr);
            this.probabilities.put(i, value);
            this.rangeValues.put(i, range);
            i++;
        }
    }

    // Write file after compressing data with Gson dependency

    private void makeFile () throws IOException {
        long time = new Date().getTime() / 1000;
        String filename = "compress" + time + ".txt";
        Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("./src/main/resources/static/storage/" + filename), "utf-8"));
        Random r = new Random();
        double random = this.range[0] + r.nextDouble() * (this.range[1] - this.range[0]);
        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(this.probabilities);
        writer.write( this.code + "##" + random + "##" + this.chars.length + "##" + json.toString());
        writer.close();
        this.filename = filename;
    }

    // Convert uploaded file to string

    private void fileToString (MultipartFile file) throws IOException {
        this.fileAsString = new String(file.getBytes(), StandardCharsets.UTF_8);
    }

    // Convert String to array of chars and make probabilities with distinct char values

    private void stringToChars (String string) {
        this.chars = string.toCharArray();

        this.stringAsChars = new HashMap<>();
        for(char c : this.chars)
        {
            if(this.stringAsChars.containsKey(c)) {
                int counter = this.stringAsChars.get(c);
                this.stringAsChars.put(c, ++counter);
            } else {
                this.stringAsChars.put(c, 1);
            }
        }
    }
}
