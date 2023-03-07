package eu.nites.compressor.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Decompress {
    private MultipartFile file;
    private Double decodeNumber;
    private int charsCount = 0;
    private Map<Integer, Map<Character, Double>> probabilities = new HashMap<>();
    private Map<Integer, Map<Character, Double[]>> rangeValues = new HashMap<>();
    private Double[] range = new Double[]{(double) 0, (double) 1};
    private String code;
    private String filename;
    private String outputString = "";

    public Decompress (MultipartFile file) throws IOException {
        this.file = file;
        this.fileToStringArray(this.file);
        this.run();
        this.makeFile();
    }

    public boolean checkHash () throws IOException {
        return Hash.check(this.filename, this.code);
    }

    public String getLink () {
        return this.filename;
    }

    // Start making range values by decode number gotten from compression

    private void run () {
        for(int i = 0; i < this.charsCount; i++) {
            if (i == 0) {
                this.chooseRange();
            } else {
                this.makeBetweenRangeValues();
            }
        }
    }

    // Choose range for every character with decode number

    private void chooseRange () {
        for (int i = 0; i < this.rangeValues.size(); i++) {
            char probability = this.rangeValues.get(i).keySet().toArray()[0].toString().charAt(0);
            if (this.decodeNumber > this.rangeValues.get(i).get(probability)[0] && this.decodeNumber <= this.rangeValues.get(i).get(probability)[1]) { // umesto for-a this.rangeValues.get(i).get(character)
                this.range = new Double[]{this.rangeValues.get(i).get(probability)[0], this.rangeValues.get(i).get(probability)[1]};
                this.outputString += probability;
            }
        }
    }

    // Make ranges for all probabilities

    private void makeBetweenRangeValues () {
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
        this.chooseRange();

    }

    // Make file after decompression

    private void makeFile () throws IOException {
        long time = new Date().getTime() / 1000;
        String filename = "decompress" + time + ".txt";
        Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("./src/main/resources/static/storage/" + filename), "UTF-8"));
        writer.write(this.outputString);
        writer.close();
        this.filename = filename;
    }

    // Convert file to string to divide into crc, decode number, probabilities and character count

    private void fileToStringArray (MultipartFile file) throws IOException {
        String[] fileAsStringArray = new String(file.getBytes(), "UTF-8").split("##");
        if (fileAsStringArray.length == 4) {
            this.code = fileAsStringArray[0];
            this.decodeNumber = Double.parseDouble(fileAsStringArray[1]);
            this.charsCount = Integer.parseInt(fileAsStringArray[2]);
            this.makeProbabilities(fileAsStringArray[3]);
        } else {
            throw new IllegalArgumentException("File cannot be decompressed!");
        }
    }

    // Converting Gson to mixed map probabilities and creating first ranges

    private void makeProbabilities (String map) {
        Gson gson = new GsonBuilder().create();
        Type typeOfHashMap = new TypeToken<Map<Integer, Map<Character, Double>>>() {
        }.getType();
        this.probabilities = gson.fromJson(map, typeOfHashMap);
        Double prev = this.range[0];
        for (int i = 0; i < this.probabilities.size(); i++) {
            if (i > 0) {
                prev = this.probabilities.get(i - 1).get(this.probabilities.get(i - 1).keySet().toArray()[0]);
            }
            Map<Character, Double[]> range = new HashMap<>();
            Double[] arr = new Double[]{prev, this.probabilities.get(i).get(this.probabilities.get(i).keySet().toArray()[0])};
            range.put(this.probabilities.get(i).keySet().toArray()[0].toString().charAt(0), arr);
            this.rangeValues.put(i, range);
        }
    }
}
