package test;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

import src.Main;

public class CalcShortestPathTest_W {

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

        // 添加边
        // word1 -> word2 -> word4
        testGraph.get("word1").put("word2", 1);
        testGraph.get("word2").put("word4", 2);

        // word1 -> word3 -> word4
        testGraph.get("word1").put("word3", 3);
        testGraph.get("word3").put("word4", 1);

        // 添加一个孤立节点
        testGraph.put("isolated", new HashMap<>());

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

    /**
     * 测试基本路径1：起点不存在的情况
     */
    @Test
    public void testStartNodeNotExist() {
        String result = Main.calcShortestPath("nonexistent", "word1");
        System.out.println(result);
        assertEquals("No \"nonexistent\" or \"word1\" in the graph!", result);
    }

    /**
     * 测试基本路径1：终点不存在的情况
     */
    @Test
    public void testEndNodeNotExist() {
        String result = Main.calcShortestPath("word1", "nonexistent");
        System.out.println(result);
        assertEquals("No \"word1\" or \"nonexistent\" in the graph!", result);
    }

    /**
     * 测试基本路径1：起点和终点都不存在的情况
     */
    @Test
    public void testBothNodesNotExist() {
        String result = Main.calcShortestPath("nonexistent1", "nonexistent2");
        System.out.println(result);
        assertEquals("No \"nonexistent1\" or \"nonexistent2\" in the graph!", result);
    }

    /**
     * 测试基本路径2：从word1到word4的最短路径
     */
    @Test
    public void testShortestPathWord1ToWord4() {
        String result = Main.calcShortestPath("word1", "word4");
        System.out.println(result);
        assertEquals("*word1* → word2 → *word4*", result);
    }

    /**
     * 测试基本路径2：节点到自身的路径
     */
    @Test
    public void testPathToSelf() {
        String result = Main.calcShortestPath("word1", "word1");
        System.out.println(result);
        assertEquals("*word1*", result);
    }

    /**
     * 测试基本路径2：直接相连的节点
     */
    @Test
    public void testAnotherDirectConnection() {
        String result = Main.calcShortestPath("word1", "word3");
        System.out.println(result);
        assertEquals("*word1* → *word3*", result);
    }

    /**
     * 测试基本路径2：在有多条路径时选择权重较小的路径
     */
    //    @Test
    //    public void testChooseLowerWeightPath() {
    //        // 修改图，添加一条直接从word1到word4的边，权重为5
    //        Main.graph.get("word1").put("word4", 5);
    //
    //        String result = Main.calcShortestPath("word1", "word4");
    //        System.out.println(result);
    //        assertEquals("*word1* → word2 → *word4*", result);
    //    }

    /**
     * 测试基本路径3：无法从word1到isolated的路径
     */
    @Test
    public void testNoConnectionFromWord1ToIsolated() {
        String result = Main.calcShortestPath("word1", "isolated");
        System.out.println(result);
        assertEquals("\"word1\" and \"isolated\" are not connected!", result);
    }

    /**
     * 测试基本路径3：无法从isolated到word1的路径
     */
    @Test
    public void testNoConnectionFromIsolatedToWord1() {
        String result = Main.calcShortestPath("isolated", "word1");
        System.out.println(result);
        assertEquals("\"isolated\" and \"word1\" are not connected!", result);
    }
}