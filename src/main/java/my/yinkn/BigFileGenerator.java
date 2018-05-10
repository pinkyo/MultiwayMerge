package my.yinkn;

import org.apache.logging.log4j.core.lookup.MainMapLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

/**
 * This class is a tool to generate big file,
 * one key each row, key includes alpha and num.
 */
public class BigFileGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final int KEY_LEN = 6;

    private static final char[] AVAILABLE_CHAR = {
     '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
     'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
     'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
     'u', 'v', 'w', 'x', 'y', 'z',
     'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
     'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
     'U', 'V', 'W', 'X', 'Y', 'Z'
    };

    private static final Random RANDOM = new Random();

    /**
     * generate big file
     * @param filename filename of big file
     * @param fileSize filesize fo big file, in GB
     */
    public boolean generate(String filename, double fileSize) {
        if  (fileSize < 0) {
            LOGGER.warn("FileSize[{}] can't be negative.", fileSize);
            return false;
        } // gate validation

        double sizeInBytes = fileSize * Constants.GIGABYTE; // size in bytes
        int keyNum = (int) (sizeInBytes / KEY_LEN + 1);

        Path filePath = Paths.get(filename);
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(filePath.toFile()), Constants.BUFFER_SIZE * Constants.KILOBYTE)) {
            for (int i = 0; i < keyNum; i++) {
                String key = generateKey();
                writer.write(key);
                writer.newLine();
            }
        } catch (IOException e) {
            LOGGER.warn("Fail to write file={}, exception={}", filename, e);
            return false;
        }

        return true;
    }

    private String generateKey() {
        int min=0, max = AVAILABLE_CHAR.length;
        int range = max - min;
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < KEY_LEN; i++) {
            int index = RANDOM.nextInt(range) + min;
            sb.append(AVAILABLE_CHAR[index]);
        }

        return sb.toString();
    }

    public static void main(String[] args) {
        MainMapLookup.setMainArguments(args);

        BigFileGenerator generator = new BigFileGenerator();
        boolean result = generator.generate("D:\\data\\merge_data.txt", 20);
        if (result) {
            LOGGER.info("Done.");
        }
    }
}
