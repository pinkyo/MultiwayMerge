package my.yinkn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Iterator;
import java.util.Objects;
import java.util.PriorityQueue;

public class KTopRepeatSearcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * search top k most repeated key.
     * @param filename name of file sorted.
     */
    public void search(String filename, int k) {
        PriorityQueue<SearchNode> result = new PriorityQueue<>(k);

        try (BufferedReader br = new BufferedReader(
                new FileReader(filename), Constants.BUFFER_SIZE * Constants.KILOBYTE)) {
            String preKey = br.readLine(), key;
            if (Objects.isNull(preKey)) return;
            int count = 1;
            do {
                key = br.readLine();
                if (preKey.equals(key)) {
                    count ++;
                } else {
                    handleNewNode(result, new SearchNode(preKey, count), k);
                    count = 1;
                    preKey = key;
                }
            } while (Objects.nonNull(key));

            printResult(result);
        } catch (FileNotFoundException e) {
            LOGGER.warn(String.format("File[%s] doesn't exist.", filename), e);
        } catch (IOException e) {
            LOGGER.warn(String.format("Fail to read file[%s].", filename), e);
        }
    }

    private void handleNewNode(PriorityQueue<SearchNode> queue, SearchNode newNode, int k) {
        if (queue.size() < k) {
            queue.add(newNode);
            return;
        }

        SearchNode leastNode = queue.poll();
        if (Objects.isNull(leastNode) ||
                leastNode.getCount() < newNode.getCount()) {
            queue.add(newNode);
        } else {
            queue.add(leastNode);
        }
    }

    private void printResult(PriorityQueue<SearchNode> result) {
        Iterator<SearchNode > iterator = result.iterator();
        for (;iterator.hasNext();) {
            SearchNode node = iterator.next();
            System.out.println(String.format("key[%s], count[%d]", node.getKey(), node.getCount()));
        }
    }

    private static class SearchNode implements Comparable<SearchNode> {
        private String key;
        private int count;

        public SearchNode(String key, int count) {
            this.key = key;
            this.count = count;
        }

        public String getKey() {
            return key;
        }

        public int getCount() {
            return count;
        }

        @Override
        public int compareTo(SearchNode o) {
            return count - o.count;
        }
    }

    public static void main(String[] args) {
        new KTopRepeatSearcher().search("D:\\data\\merge_data.txt", 20);
    }
}
