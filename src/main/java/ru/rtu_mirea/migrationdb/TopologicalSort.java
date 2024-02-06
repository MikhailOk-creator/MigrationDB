package ru.rtu_mirea.migrationdb;

/*
    * Topological sort (DFS) of a graph represented as an adjacency list
    * Input example (adjacency list):
    * 0 0 0
    * 1 0 1
    * 0 0 0
    * Output example (return value of the method topologicalSort()):
    * 0 2 1
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


public class TopologicalSort {
    private final int V;
    private final List<List<Integer>> adj;

    public TopologicalSort(int V) {
        this.V = V;
        adj = new ArrayList<>(V);
        for (int i = 0; i < V; i++)
            adj.add(new ArrayList<>());
    }

    public void addEdge(int v, int w) {
        adj.get(v).add(w);
    }

    public void topologicalSortUtil(int v, boolean[] visited, Stack<Integer> stack) {
        visited[v] = true;
        Integer i;

        for (Integer integer : adj.get(v)) {
            i = integer;
            if (!visited[i])
                topologicalSortUtil(i, visited, stack);
        }

        stack.push(v);
    }

    public String topologicalSort() {
        Stack<Integer> stack = new Stack<>();
        String result = "";

        boolean[] visited = new boolean[V];
        for (int i = 0; i < V; i++)
            visited[i] = false;

        for (int i = 0; i < V; i++)
            if (!visited[i])
                topologicalSortUtil(i, visited, stack);

        while (!stack.empty())
            result += stack.pop() + " ";

        return result;
    }
}