package src;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import org.apache.commons.io.FilenameUtils;


/**
 * 主程序类，用于实现文本处理和图分析功能.
 *
 * <p>该类提供以下功能：
 * <ul>
 * <li>从文本文件构建有向图</li>
 * <li>可视化展示有向图</li>
 * <li>查询桥接词</li>
 * <li>根据桥接词生成新文本</li>
 * <li>计算最短路径</li>
 * <li>计算PageRank值</li>
 * <li>进行随机游走</li>
 * </ul>
 */
public class Main {

  public static Map<String, Map<String, Integer>> graph;
  private static final double DAMPING_FACTOR = 0.85;
  private static final int PAGERANK_ITERATIONS = 200;
  private static volatile boolean stopWalk = false; // 用于控制随机游走的标志变量

  /**
   * 主方法，程序入口点.
   *
   * @param args 命令行参数，包含文件路径
   */
  public static void main(String[] args) {
    // 获取文件路径（命令行参数或用户输入）
    String filePath = args.length > 0 ? args[0] : getInputFilePath();

    try {
      // 1. 文本预处理
      String processedText = processFile(filePath);
      List<String> words = Arrays.asList(processedText.split("\\s+"));
      // 2. 构建有向图
      graph = buildGraph(words);
      // 3. 功能选择
      Scanner scanner = new Scanner(System.in, "UTF-8");
      while (true) {
        System.out.println("1. 命令行输出有向图");
        System.out.println("2. 保存为图片格式");
        System.out.println("3. 查询桥接词");
        System.out.println("4. 根据桥接词生成新文本");
        System.out.println("5. 计算最短路径");
        System.out.println("6. 计算PageRank");
        System.out.println("7. 随机游走");
        System.out.println("8. 退出");
        System.out.print("请选择操作: ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
          case "1":
            showDirectedGraph(graph);
            break;
          case "2":
            try {
              // 调用 generateDotImage 生成图片
              BufferedImage image = generateDotImage(graph);

              // 保存图片到文件（可选）
              ImageIO.write(image, "png", new File("output", "graph.png"));
              System.out.println("图片已生成并保存为 output/graph.png");

              // 或者直接在 GUI 中显示图片（如果需要）
              // showImage(image); // 需要实现一个显示图片的函数
            } catch (IOException | InterruptedException e) {
              System.err.println("生成图片时出错: " + e.getMessage());
            }
            break;
          case "3":
            System.out.print("Word 1: ");
            String word1 = scanner.next();
            System.out.print("Word 2: ");
            String word2 = scanner.next();
            // 清空缓冲区剩余内容
            scanner.nextLine();
            // 转换为小写以用来匹配图中的节点
            word1 = word1.toLowerCase();
            word2 = word2.toLowerCase();

            // 检查单词是否在图中
            if (!graph.containsKey(word1)) {
              if (!graph.containsKey(word2)) {
                System.out.println("No \"" + word1 + "\" and \"" + word2 + "\" in the graph!");
              } else {
                System.out.println("No \"" + word1 + "\" in the graph!");
              }
              break;
            } else if (!graph.containsKey(word2)) {
              System.out.println("No \"" + word2 + "\" in the graph!");
              break;
            }

            Set<String> bridgeWords = queryBridgeWords(word1, word2);
            // 输出结果
            if (bridgeWords == null) {
              break;
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
            break;
          case "4":
            System.out.println("已加载有向图，请输入要处理的新文本:");
            String inputText = scanner.nextLine();
            String changedText = generateNewText(inputText);
            System.out.println("\n处理后的文本:");
            System.out.println(changedText);
            break;
          case "5":
            System.out.print("起始单词: ");
            String start = scanner.next();
            System.out.print("目标单词: ");
            String end = scanner.next();
            scanner.nextLine(); // 清空缓冲区
            start = start.toLowerCase();
            end = end.toLowerCase();

            // 调用 calcShortestPath 获取路径字符串
            String pathStr = calcShortestPath(start, end);
            System.out.println("路径: " + pathStr);

            // 如果路径有效，计算路径长度
            if (!pathStr.contains("not connected") && !pathStr.contains("No")) {
              List<String> path = Arrays.stream(pathStr.replace("*", "").split(" → "))
                  .collect(Collectors.toList());
              int length = 0;
              for (int i = 0; i < path.size() - 1; i++) {
                length += graph.get(path.get(i)).get(path.get(i + 1));
              }
              System.out.println("路径长度: " + length);
            }
            break;
          case "6":
            System.out.print("请输入单词:");
            String word = scanner.nextLine().trim();
            if (!graph.containsKey(word)) {
              System.out.println("No \"" + word + "\" in the graph!");
              break;
            }
            Double pageRank = calPageRank(word);
            System.out.println("节点 " + word + " 的PageRank值为: " + pageRank);
            break;
          case "7":
            if (graph.isEmpty()) {
              System.out.println("图为空，无法进行随机游走");
              break;
            }
            System.out.println("\n=== 开始随机游走 ===");
            System.out.println("提示: 按Enter键停止遍历，或等待遍历自动结束");

            // 启动随机游走线程
            stopWalk = false;
            Thread walkThread = new Thread(() -> {
              String result = randomWalk();
              System.out.println("\n随机游走路径: " + result);

              // 保存结果到文件
              String filename = "./random_walk.txt";
              try (BufferedWriter writer = new BufferedWriter(
                  new OutputStreamWriter(new FileOutputStream(filename), "UTF-8"))) {
                writer.write(result);
                System.out.println("结果已保存到文件: " + filename);
              } catch (IOException e) {
                System.err.println("保存文件时出错: " + e.getMessage());
              }
            });
            walkThread.start();

            // 等待用户按下回车键
            try {
              while (walkThread.isAlive()) {
                if (System.in.available() > 0) { // 检查是否有用户输入
                  System.in.read(); // 读取用户输入
                  stopWalk = true; // 设置标志变量，通知线程停止
                  break;
                }
              }
              walkThread.join(); // 等待线程结束
            } catch (IOException | InterruptedException e) {
              System.err.println("中断处理出错: " + e.getMessage());
            }
            break;
          case "8":
            System.out.println("已退出程序");
            scanner.close();
            return;
          default:
            System.out.println("无效输入，请重新选择");
        }
        System.out.print("\n");
      }

    } catch (FileNotFoundException e) {
      System.err.println("错误: 文件未找到 - " + e.getMessage());
    } catch (IOException e) {
      System.err.println("错误: 读取文件失败 - " + e.getMessage());
    }
  }

  private static String getInputFilePath() {
    System.out.print("请输入文本文件路径: ");
    return new Scanner(System.in, "UTF-8").nextLine();
  }

  private static String processFile(String filePath) throws IOException {
    // 读取整个文件内容，换行符替换为空格
    File safeFile = new File("resources/", FilenameUtils.getName(filePath));
    byte[] bytes = Files.readAllBytes(safeFile.toPath());
    String content = new String(bytes, "UTF-8");
    content = content.replaceAll("\\R", " ");

    // 处理文本：非字母字符替换为空格，转换为小写，合并多个空格
    return content.replaceAll("[^a-zA-Z]", " ")
        .toLowerCase()
        .replaceAll("\\s+", " ").trim();
  }

  private static Map<String, Map<String, Integer>> buildGraph(List<String> words) {
    Map<String, Map<String, Integer>> graph = new HashMap<>();
    words.forEach(word -> graph.putIfAbsent(word, new HashMap<>()));
    // 添加边
    for (int i = 0; i < words.size() - 1; i++) {
      String current = words.get(i);
      String next = words.get(i + 1);
      graph.get(current).merge(next, 1, Integer::sum);
    }
    return graph;
  }

  /**
   * 显示有向图的命令行输出.
   *
   * @param graph 有向图，格式为 Map < 源节点, Map < 目标节点, 权重>>
   */
  public static void showDirectedGraph(Map<String, Map<String, Integer>> graph) {
    System.out.println("\n生成的有向图（节点A -> 节点B: 权重）:");
    // 按字母顺序排序源节点
    graph.keySet().stream()
        .sorted()
        .forEach(source -> {
          // 按字母顺序排序目标节点
          graph.get(source).keySet().stream()
              .sorted()
              .forEach(target -> {
                int weight = graph.get(source).get(target);
                System.out.printf("%s -> %s: %d%n", source, target, weight);
              });
        });
    System.out.print("\n");
  }

  /**
   * 生成有向图的DOT格式字符串.
   *
   * @param graph 有向图，格式为 Map < 源节点, Map < 目标节点, 权重>>
   * @return DOT格式字符串
   */
  public static BufferedImage generateDotImage(Map<String, Map<String, Integer>> graph)
      throws IOException, InterruptedException {
    // DOT 文件头部
    StringBuilder dotBuilder = new StringBuilder();
    dotBuilder.append("digraph G {\n");
    dotBuilder.append("    rankdir=LR;  // 从左到右布局\n");
    dotBuilder.append("    node [shape=circle, style=filled, fillcolor=lightblue];\n");
    dotBuilder.append("    edge [fontsize=10];\n\n");

    // 添加所有边（会自动创建节点）
    graph.forEach((source, targets) -> {
      targets.forEach((target, weight) -> {
        dotBuilder.append(String.format("    \"%s\" -> \"%s\" [label=\"%d\"];%n",
            source, target, weight));
      });
    });

    dotBuilder.append("}\n");

    // 使用 Graphviz 的 dot 命令生成图片
    ProcessBuilder processBuilder = new ProcessBuilder("dot", "-Tpng");
    Process process = processBuilder.start();

    // 将 DOT 内容写入 Graphviz 的输入流
    try (BufferedWriter writer
                 = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), "UTF-8"))) {
      writer.write(dotBuilder.toString());
    }

    // 从 Graphviz 的输出流读取生成的图片
    try (InputStream inputStream = process.getInputStream()) {
      BufferedImage image = ImageIO.read(inputStream);
      int exitCode = process.waitFor();

      if (exitCode == 0 && image != null) {
        return image; // 返回生成的图片
      } else {
        throw new IOException("生成图片失败，请检查 Graphviz 是否已安装并配置到 PATH 中。");
      }
    }
  }

  /**
   * 查询两个单词之间的桥接词.
   *
   * @param word1 第一个单词
   * @param word2 第二个单词
   * @return 桥接词集合，如果不存在则返回 null
   */
  public static Set<String> queryBridgeWords(String word1, String word2) {
    if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
      return null;
    }
    // 查找桥接词
    Set<String> bridgeWords = new HashSet<>();
    // 获取word1的所有直接后继
    Set<String> successors = graph.get(word1).keySet();
    // 检查每个后继是否有到word2的边
    for (String candidate : successors) {
      if (graph.containsKey(candidate)
          && graph.get(candidate).containsKey(word2)) {
        bridgeWords.add(candidate);
      }
    }
    return bridgeWords;
  }

  /**
   * 根据输入文本生成新文本，插入桥接词.
   *
   * @param inputText 输入文本
   * @return 生成的新文本
   */
  public static String generateNewText(String inputText) {
    if (graph == null || graph.isEmpty() || inputText == null || inputText.trim().isEmpty()) {
      return inputText;
    }
    String[] parts = inputText.split("(?<=\\W)|(?=\\W)");
    List<String> result = new ArrayList<>();

    String prevWord = null;
    boolean prevWasWord = false;
    for (String part : parts) {
      if (part.matches("[a-zA-Z]+")) {
        // 当前部分是单词
        String currentWord = part.toLowerCase();
        if (prevWasWord && prevWord != null) {
          Set<String> bridges = null;
          if (graph.containsKey(prevWord) && graph.containsKey(currentWord)) {
            bridges = queryBridgeWords(prevWord, currentWord);
          }
          // 如果有桥接词，随机选择一个插入
          if (bridges != null && !bridges.isEmpty()) {
            // 使用 SecureRandom 代替 Random
            int index = SECURE_RANDOM.nextInt(bridges.size());
            String bridge = bridges.stream().skip(index).findFirst().get();
            result.add(bridge);
            result.add(" ");
          }
        }
        result.add(part);
        prevWord = currentWord;
        prevWasWord = true;
      } else if (part.matches("\\s+")) {
        // 跳过空格
        result.add(part);
      } else {
        // 当前部分是非单词（标点等）
        result.add(part);
        prevWasWord = false;
      }
    }

    return String.join("", result);
  }

  /**
   * 计算两个单词之间的最短路径.
   *
   * @param start 起始单词
   * @param end   目标单词
   * @return 最短路径字符串，格式为 "word1 → word2 → ...", 如果不存在则返回错误信息
   */
  public static String calcShortestPath(String start, String end) {
    // 检查节点是否存在
    if (!graph.containsKey(start) || !graph.containsKey(end)) {
      return "No \"" + start + "\" or \"" + end + "\" in the graph!";
    }

    // 初始化数据结构
    Map<String, Integer> distances = new HashMap<>(); // 存储每个节点的最短距离
    final Set<String> visited = new HashSet<>(); // 存储已访问的节点
    final Map<String, String> previous = new HashMap<>(); // 存储每个节点的前驱节点
    PriorityQueue<String> queue = new PriorityQueue<>(Comparator.comparingInt(distances::get));

    // 初始化起点
    for (String node : graph.keySet()) {
      distances.put(node, Integer.MAX_VALUE); // 初始距离为无穷大
    }
    distances.put(start, 0);
    queue.offer(start);

    // Dijkstra 算法
    while (!queue.isEmpty()) {
      String currentNode = queue.poll();
      if (visited.contains(currentNode)) {
        continue;
      }
      visited.add(currentNode);

      // 遍历当前节点的邻居
      for (Map.Entry<String, Integer> neighborEntry
              : graph.getOrDefault(currentNode, Collections.emptyMap())
          .entrySet()) {
        String neighbor = neighborEntry.getKey();
        int edgeWeight = neighborEntry.getValue();

        // 计算新距离
        int newDistance = distances.get(currentNode) + edgeWeight;
        if (newDistance < distances.get(neighbor)) {
          distances.put(neighbor, newDistance);
          previous.put(neighbor, currentNode);
          queue.offer(neighbor);
        }
      }
    }

    // 回溯路径
    List<String> path = new ArrayList<>();
    for (String node = end; node != null; node = previous.get(node)) {
      path.add(node);
    }
    Collections.reverse(path);

    // 检查路径是否有效
    if (path.size() == 1 && !path.get(0).equals(start)) {
      return "\"" + start + "\" and \"" + end + "\" are not connected!";
    }

    // 格式化路径为字符串
    return path.stream()
        .map(word -> word.equals(start) || word.equals(end) ? "*" + word + "*" : word)
        .collect(Collectors.joining(" → "));
  }

  /**
   * 计算指定单词的PageRank值.
   *
   * @param word 要计算PageRank的单词
   * @return PageRank值，如果单词不存在则返回 null
   */
  public static Double calPageRank(String word) {
    if (!graph.containsKey(word)) {
      return null;
    }

    final double d = 0.85; // 阻尼系数
    final int N = graph.size();
    Map<String, Double> pr = new HashMap<>();

    // 初始化
    double initPr = 1.0 / N;
    for (String node : graph.keySet()) {
      pr.put(node, initPr);
    }

    for (int iter = 0; iter < 50; iter++) {
      Map<String, Double> newPr = new HashMap<>();
      double danglingSum = 0;

      // 计算dead ends的PR总和 - 使用entrySet避免重复查找
      for (Map.Entry<String, Map<String, Integer>> entry : graph.entrySet()) {
        if (entry.getValue().isEmpty()) {
          danglingSum += pr.get(entry.getKey());
        }
      }

      // 计算每个节点的新PR
      double sum = 0;
      for (Map.Entry<String, Map<String, Integer>> nodeEntry : graph.entrySet()) {
        String u = nodeEntry.getKey();
        double incoming = 0;
        
        // 使用entrySet迭代外部图
        for (Map.Entry<String, Map<String, Integer>> neighborEntry : graph.entrySet()) {
          String v = neighborEntry.getKey();
          Map<String, Integer> neighborEdge = neighborEntry.getValue();
          
          if (neighborEdge.containsKey(u)) {
            incoming += pr.get(v) / neighborEdge.size();
          }
        }
        
        double newRank = (1 - d) / N + d * (incoming + danglingSum / N);
        newPr.put(u, newRank);
        sum += newRank;
      }

      // 归一化处理（确保总和为1）
      final double normalization = 1.0 / sum;
      newPr.replaceAll((k, v) -> v * normalization);

      pr = newPr;
    }

    return pr.get(word);
  }

  /**
   * 执行随机游走，直到用户停止或没有可用边.
   *
   * @return 随机游走路径字符串
   */
  public static String randomWalk() {
    // 随机选择起始节点
    List<String> nodes = new ArrayList<>(graph.keySet());
    String currentNode = nodes.get(SECURE_RANDOM.nextInt(nodes.size()));

    Set<String> visitedEdges = new HashSet<>();
    List<String> path = new ArrayList<>();
    path.add(currentNode);

    while (!stopWalk) { // 检查标志变量是否被设置为停止
      // 检查当前节点是否有出边
      Map<String, Integer> edges = graph.get(currentNode);
      if (edges == null || edges.isEmpty()) {
        break; // 当前节点没有出边，结束游走
      }

      // 随机选择下一个节点
      List<String> nextNodes = new ArrayList<>(edges.keySet());
      String nextNode = nextNodes.get(SECURE_RANDOM.nextInt(nextNodes.size()));

      // 检查边是否已经访问过
      String edge = currentNode + "->" + nextNode;
      if (visitedEdges.contains(edge)) {
        break; // 检测到重复边，结束游走
      }

      visitedEdges.add(edge);
      path.add(nextNode);
      currentNode = nextNode;

      // 延迟一段时间以模拟游走过程
      try {
        Thread.sleep(500); // 500ms 延迟
      } catch (InterruptedException e) {
        break; // 如果线程被中断，结束游走
      }
    }

    // 返回随机游走路径
    return String.join(" ", path);
  }

  private static final SecureRandom SECURE_RANDOM = new SecureRandom();
}
