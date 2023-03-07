package eu.nites.compressor.models;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;

public class Hash {

    public static String make(MultipartFile file) throws IOException {
        InputStream inputStreamn = file.getInputStream();
        CRC32 crc = new CRC32();
        int cnt;
        while ((cnt = inputStreamn.read()) != -1) {
            crc.update(cnt);
        }
        return Long.toHexString(crc.getValue());
    }

    public static String make(File file) throws IOException {
        FileInputStream inputStreamn = new FileInputStream(file);
        CRC32 crc = new CRC32();
        int cnt;
        while ((cnt = inputStreamn.read()) != -1) {
            crc.update(cnt);
        }
        return Long.toHexString(crc.getValue());
    }

    public static boolean check (String path, String code) throws IOException {
        InputStream inputStreamn = new FileInputStream("./src/main/resources/static/storage/" + path);
        CRC32 crc = new CRC32();
        int cnt;
        while ((cnt = inputStreamn.read()) != -1) {
            crc.update(cnt);
        }
        return code.equals(Long.toHexString(crc.getValue()));
    }
}
