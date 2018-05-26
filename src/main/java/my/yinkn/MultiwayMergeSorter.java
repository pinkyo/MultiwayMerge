package my.yinkn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class MultiwayMergeSorter {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final int CHUNK_SIZE = 20; // in Megabyte;
    private static final String MIDDLE_FILE_NAME_PATTERN = "middle.%d.tmp";

    /**
     * Sort big file to a new file.
     * @param filename name of file to sort
     * @return
     */
    private String sort(String filename) {
        Path filePath = Paths.get(filename);
        if (!Files.exists(filePath)) {
            LOGGER.warn("File[{}] doesn't exist.", filename);
            return null;
        }

        int chunkSizeInByte = CHUNK_SIZE * Constants.MEGABYTE;
        List<String> stringList = new ArrayList<>();
        String dirname = filePath.getParent().toString();
        try (BufferedReader br = new BufferedReader(
                new FileReader(filePath.toFile()), Constants.BUFFER_SIZE * Constants.KILOBYTE)) {
            String key = null;
            List<String> middleFileNameList = new ArrayList<>();
            do {
                for (int readSizeTotal = 0;readSizeTotal < chunkSizeInByte;
                     readSizeTotal += key.getBytes().length) {
                    key = br.readLine();
                    if (Objects.isNull(key)) { // jump out of loop when reach eof
                        break;
                    }
                    stringList.add(key);
                }

                if (!stringList.isEmpty()) {
                    Collections.sort(stringList);
                    String middleFileName = generateMiddleFileName(dirname, middleFileNameList.size());
                    writeToFile(middleFileName, stringList);
                    middleFileNameList.add(middleFileName);
                }

                stringList.clear();
            } while (key != null);

            String targetFile = filename + ".sort";
            merge(targetFile, middleFileNameList);

            return targetFile;
        } catch (Exception e) {
            LOGGER.warn("Fail to read file[{}], exception={}.", filename, e);
            return null;
        }
    }

    /**
     * write sorted string list to middle file.
     * @param filename
     * @param sortedList
     */
    private void writeToFile(String filename, List<String> sortedList) {
        LOGGER.debug("Write to file[{}]", filename);

        Iterator<String> iterator = sortedList.iterator();
        Path filePath = Paths.get(filename);
        try(BufferedWriter writer = new BufferedWriter(
                new FileWriter(filePath.toFile()), Constants.BUFFER_SIZE * Constants.KILOBYTE)) {
            while (iterator.hasNext()) {
                writer.write(iterator.next());
                writer.newLine();
            }
        } catch (Exception e) {
            LOGGER.warn("Fail to write file[{}], exception={}.", filename, e);
        }
    }

    private static String generateMiddleFileName(String dirname, int order) {
        return Paths.get(dirname, String.format(MIDDLE_FILE_NAME_PATTERN, order)).toString();
    }

    /**
     * merge middle file to a file.
     * @param targetFile name of target file
     * @param srcList names of middle file
     */
    private void merge(String targetFile, List<String> srcList) {
        LOGGER.debug("Starting to merge, target file={}, src file={}", targetFile, srcList);
        PriorityQueue<KeyWrapper> queue = new PriorityQueue<>(srcList.size());
        List<BufferedReader> readerList = new ArrayList<>(srcList.size());
        try(BufferedWriter bw = new BufferedWriter(
                new FileWriter(targetFile), Constants.BUFFER_SIZE * Constants.KILOBYTE)) {
            for (int i = 0; i < srcList.size(); i++) {
                readerList.add(new BufferedReader(
                        new FileReader(srcList.get(i)), Constants.BUFFER_SIZE * Constants.KILOBYTE));
                queue.add(new KeyWrapper(readerList.get(i).readLine(), i));
            }

            while (!queue.isEmpty()) {
                KeyWrapper kw = queue.poll();
                bw.write(kw.getKey());
                bw.newLine();

                String tmp = readerList.get(kw.getNode()).readLine();
                if (Objects.nonNull(tmp)) {
                    queue.add(new KeyWrapper(tmp, kw.getNode()));
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Error when do merge operation.", e);
        } finally {
            for (int i = 0; i < readerList.size(); i++) {
                if (Objects.nonNull(readerList.get(i))) {
                    try {
                        readerList.get(i).close();
                    } catch (IOException e) {
                        // do nothing.
                    }
                }
            }
        }
    }

    private static class KeyWrapper implements Comparable<KeyWrapper> {
        private String key;
        private int node;

        public KeyWrapper(String key, int node) {
            this.key = key;
            this.node = node;
        }

        public String getKey() {
            return key;
        }

        public int getNode() {
            return node;
        }

        @Override
        public int compareTo(KeyWrapper o) {
            return key.compareTo(o.getKey());
        }
    }

    public static void main(String[] args) {
        new MultiwayMergeSorter().sort("D:\\data\\merge_data.txt");
    }
}
