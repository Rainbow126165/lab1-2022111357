package test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

import src.Main;

public class QueryBridgeWordsTest_B {

    private Map<String, Map<String, Integer>> testGraph;

    @Before
    public void setUp() {
        // 创建测试图
        testGraph = new HashMap<>();

        // 添加节点
        testGraph.put("word1", new HashMap<>());
        testGraph.put("word2", new HashMap<>());
        testGraph.put("word3", new HashMap<>());
        testGraph.put("word4", new HashMap<>());
        testGraph.put("word5", new HashMap<>());

        // 添加边
        // word1 -> word2 -> word4
        testGraph.get("word1").put("word2", 1);
        testGraph.get("word2").put("word4", 1);

        // word1 -> word3 -> word4
        testGraph.get("word1").put("word3", 1);
        testGraph.get("word3").put("word4", 1);

        // word3 -> word5
        testGraph.get("word3").put("word5", 1);

        // word4 -> word5
        testGraph.get("word4").put("word5", 1);

        // 直接设置测试图
        setTestGraph(testGraph);
    }

    // 通过反射设置测试图到Main类
    private void setTestGraph(Map<String, Map<String, Integer>> graph) {
        try {
            java.lang.reflect.Field graphField = Main.class.getDeclaredField("graph");
            graphField.setAccessible(true);
            graphField.set(null, graph);
        } catch (Exception e) {
            System.err.println("无法设置测试图: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void printResults(Set<String> bridgeWords, String word1, String word2) {
        // 检查2个单词是否在图中
        if (!testGraph.containsKey(word1)) {
            if (!testGraph.containsKey(word2)) {
                System.out.println("No \"" + word1 + "\" and \"" + word2 + "\" in the graph!");
            } else {
                System.out.println("No \"" + word1 + "\" in the graph!");
            }
            return;
        } else if (!testGraph.containsKey(word2)) {
            System.out.println("No \"" + word2 + "\" in the graph!");
            return;
        }
        // 如果桥接词集合不为null或空，输出相应信息
        if (bridgeWords == null) {
            return;
        }
        if (bridgeWords.isEmpty()) {
            System.out.println("No bridge words from \"" + word1 + "\" to \"" + word2 + "\"!");
        } else if (bridgeWords.size() == 1) {
            String singleWord = new ArrayList<>(bridgeWords).get(0);
            System.out
                    .println("The bridge words from \"" + word1 + "\" to \"" + word2
                            + "\" is: \"" + singleWord + "\"");
        } else {
            String bridgeWordsStr = bridgeWords.stream()
                    .sorted()
                    .collect(Collectors.joining(", "));
            System.out.println(
                    "The bridge words from \"" + word1 + "\" to \"" + word2
                            + "\" are: \"" + bridgeWordsStr + "\"");
        }
    }

    // 测试 1: 两个单词都存在，且有多个桥接词
    @Test
    public void testMultipleBridgeWords() {
        Set<String> bridges = Main.queryBridgeWords("word1", "word4");
        printResults(bridges, "word1", "word4");
        assertNotNull("桥接词集合不应为空", bridges);
        assertEquals("应有两个桥接词", 2, bridges.size());
        assertTrue("应包含word2作为桥接词", bridges.contains("word2"));
        assertTrue("应包含word3作为桥接词", bridges.contains("word3"));
    }

    // 测试 2: 两个单词都存在，且只有一个桥接词
    @Test
    public void testSingleBridgeWord() {
        Set<String> bridges = Main.queryBridgeWords("word1", "word5");
        printResults(bridges, "word1", "word5");
        assertNotNull("桥接词集合不应为空", bridges);
        assertEquals("应有一个桥接词", 1, bridges.size());
        assertTrue("应包含word3作为桥接词", bridges.contains("word3"));
    }

    // 测试 3: 两个单词都存在，但没有桥接词
    @Test
    public void testNoBridgeWords() {
        Set<String> bridges = Main.queryBridgeWords("word2", "word3");
        printResults(bridges, "word2", "word3");
        assertNotNull("返回值不应为空", bridges);
        assertTrue("应该没有桥接词", bridges.isEmpty());
    }

    // 测试 4: 第一个单词不在图中（无效等价类）
    @Test
    public void testFirstWordNotInGraph() {
        Set<String> bridges = Main.queryBridgeWords("notExist", "word2");
        printResults(bridges, "notExist", "word2");
        assertNull("单词不在图中时应返回null", bridges);
    }

    // 测试 5: 第二个单词不在图中（无效等价类）
    @Test
    public void testSecondWordNotInGraph() {
        Set<String> bridges = Main.queryBridgeWords("word1", "notExist");
        printResults(bridges, "word1", "notExist");
        assertNull("单词不在图中时应返回null", bridges);
    }

    // 测试 6: 两个单词都不在图中（无效等价类）
    @Test
    public void testBothWordsNotInGraph() {
        Set<String> bridges = Main.queryBridgeWords("notExist1", "notExist2");
        printResults(bridges, "notExist1", "notExist2");
        assertNull("单词不在图中时应返回null", bridges);
    }

}