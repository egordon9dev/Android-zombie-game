package com.ethan.siege;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

class NodeComparator implements Comparator<Node> {

    @Override
    public int compare(Node n1, Node n2) {
        if (n1.fScore < n2.fScore) {
            return -1;
        }
        if (n1.fScore > n2.fScore) {
            return 1;
        }
        return 0;
    }
}

class Node {
    public boolean walkable;
    public double x, y, gScore, fScore;

    public Node() {
        walkable = true;
        gScore = 999999999.99;
    }
    public Node(boolean b) {
        this();
        walkable = b;
    }

    public boolean isCloseTo(double a, double b) {
        return Math.abs(a - b) < 0.001;
    }

    @Override
    public boolean equals(Object o) {
        Node n = (Node) o;
        return isCloseTo(n.x, x) && isCloseTo(n.y, y);
    }
}

public class JPS {
    private int gridWidth, gridHeight, nodeSpace;
    private Node nodes[][];
    private Node start, end;
    private HashMap<Node, Node> parent;

    public long t0, t1;
    public Node[][] getNodes() { return nodes; }
    public int getGridWidth() { return gridWidth; }
    public int getGridHeight() { return gridHeight; }
    public int getNodeSpace() { return nodeSpace; }
    public long getDt() { return t1 - t0; }
    public JPS(int width, int height, int space) {
        gridWidth = width;
        gridHeight = height;
        nodeSpace = space;
        nodes = new Node[gridHeight][gridWidth];
        reset();
    }
    private void reset() {
        for (int i = 0; i < nodes.length; i++) {
            for (int j = 0; j < nodes[i].length; j++) {
                boolean b = true;
                if(nodes[i][j] != null) b = nodes[i][j].walkable;
                nodes[i][j] = new Node(b);
                nodes[i][j].x = j * nodeSpace;
                nodes[i][j].y = i * nodeSpace;
            }
        }
    }

    private double dist(Node n1, Node n2) {
        return Math.sqrt(Math.pow(n2.x - n1.x, 2.0) + Math.pow(n2.y - n1.y, 2.0));
    }

    private Node[] idSuccessors(Node cur) {
        Node successors[] = new Node[8];
        for(int i = 0; i < successors.length; i++) {
            successors[i] = null;
        }
        Node n00 = nodes[0][0];
        int cx = (int)(cur.x - n00.x) / nodeSpace, cy = (int)(cur.y - n00.y) / nodeSpace;
        if(parent.containsKey(cur)) {
            Node p = parent.get(cur);
            int dx = cx - (int)(p.x - n00.x)/nodeSpace, dy = cy - (int)(p.y - n00.y)/nodeSpace;
            if(dx > 1) dx = 1;
            if(dx < -1) dx = -1;
            if(dy > 1) dy = 1;
            if(dy < -1) dy = -1;
            Node neighbors[] = getNeighborsPrune(cx, cy, dx, dy);
            for(int i = 0; i < neighbors.length; i++) {
                Node n = neighbors[i];
                if(n == null) continue;
                int nx = (int)(n.x-n00.x)/nodeSpace, ny = (int)(n.y-n00.y)/nodeSpace;
            }
            for(int i = 0; i < neighbors.length; i++) {
                Node n = neighbors[i];
                if(n == null) continue;
                int nx = (int)(n.x-n00.x)/nodeSpace, ny = (int)(n.y-n00.y)/nodeSpace;
                dx = nx - cx;
                dy = ny - cy;
                if(dx > 1) dx = 1;
                if(dx < -1) dx = -1;
                if(dy > 1) dy = 1;
                if(dy < -1) dy = -1;
                Node jumpPt = jump(cx, cy, dx, dy);
                successors[i] = jumpPt;
            }
        } else {
            if(isWalk(cy, cx-1)) successors[0] = nodes[cy][cx-1];
            if(isWalk(cy-1, cx-1)) successors[1] = nodes[cy-1][cx-1];
            if(isWalk(cy-1, cx)) successors[2] = nodes[cy-1][cx];
            if(isWalk(cy-1, cx+1)) successors[3] = nodes[cy-1][cx+1];
            if(isWalk(cy, cx+1)) successors[4] = nodes[cy][cx+1];
            if(isWalk(cy+1, cx+1)) successors[5] = nodes[cy+1][cx+1];
            if(isWalk(cy+1, cx)) successors[6] = nodes[cy+1][cx];
            if(isWalk(cy+1, cx-1)) successors[7] = nodes[cy+1][cx-1];
        }
        return successors;
    }
    private boolean isWalk(int y, int x) {
        return y >= 0 && x >= 0 && y < nodes.length && x < nodes[0].length && nodes[y][x].walkable;
    }
    private Node jump(int cx, int cy, int dx, int dy) {
        int nx = cx + dx;
        int ny = cy + dy;
        if(!isWalk(ny, nx)) return null;
        Node next = nodes[ny][nx];
        if(next.equals(end)) return next;

        if(dx != 0 && dy != 0) { //diagonal
            //diagonal forced neighbor check
            if((!isWalk(ny, nx-dx) && isWalk(ny+dy, nx) && isWalk(ny+dy, nx-dx)) || (!isWalk(ny-dy, nx) && isWalk(ny-dy, nx+dx) && isWalk(ny, nx+dx))) {
                return next;
            }

            if(jump(nx, ny, dx, 0) != null || jump(nx, ny, 0, dy) != null) {
                return next;
            }
        } else {
            // vertical/horizontal forced neighbor check
            if( (!isWalk(ny, nx+1) && isWalk(ny+dy, nx+1)) || (!isWalk(ny, nx-1) && isWalk(ny+dy, nx-1)) || (!isWalk(ny+1, nx) && isWalk(ny+1, nx+dx)) || (!isWalk(ny-1, nx) && isWalk(ny-1, nx+dx))) {
                return next;
            }
        }
        return jump(nx, ny, dx, dy);
    }
    private Node[] getNeighborsPrune(int cx, int cy, int dx, int dy){
        Node[] neighbors = new Node[5];
        for(int i = 0; i < neighbors.length; i++) {
            neighbors[i] = null;
        }
        if (dx!=0 && dy!=0) { // moving diagonal
            // normal 3 neighbors
            if (isWalk(cy+dy, cx)) {
                neighbors[0] = nodes[cy+dy][cx];
            }
            if (isWalk(cy, cx+dx)) neighbors[1] = nodes[cy][cx+dx];
            if ((isWalk(cy+dy, cx) || isWalk(cy, cx+dx)) && isWalk(cy+dy, cx+dx)) neighbors[2] = nodes[cy+dy][cx+dx];
            // 2 forced neighbors
            if (!isWalk(cy, cx-dx) && isWalk(cy+dy, cx) && isWalk(cy+dy, cx-dx)) neighbors[3] = nodes[cy+dy][cx-dx];
            if (!isWalk(cy-dy, cx) && isWalk(cy, cx+dx) && isWalk(cy-dy, cx+dx)) neighbors[4] = nodes[cy-dy][cx+dx];
        } else {
            if (dx == 0){ // moving vertical
                if (isWalk(cy+dy, cx)){
                    // normal 1 neighbor
                    neighbors[0] = nodes[cy+dy][cx];
                    // 2 forced neighbors
                    if (!isWalk(cy, cx+1) && isWalk(cy+dy, cx+1)){
                        neighbors[1] = nodes[cy+dy][cx+1];
                    }
                    if (!isWalk(cy, cx-1) && isWalk(cy+dy, cx-1)){
                        neighbors[2] = nodes[cy+dy][cx-1];
                    }
                }
            } else { // moving horizontal
                if (isWalk(cy, cx+dx)){
                    // normal 1 neighbor
                    neighbors[0] = nodes[cy][cx+dx];
                    // 2 forced neighbors
                    if (!isWalk(cy+1, cx) && isWalk(cy+1, cx+dx)){
                        neighbors[1] = nodes[cy+1][cx+dx];
                    }
                    if (!isWalk(cy-1, cx) && isWalk(cy-1, cx+dx)){
                        neighbors[2] = nodes[cy-1][cx+dx];
                    }
                }
            }
        }
        return neighbors;
    }
    private ArrayList<Node> reconstructPath() {
        ArrayList<Node> path = new ArrayList<Node>();
        Node n = end;
        path.add(n);
        while(parent.containsKey(n)) {
            n = parent.get(n);
            path.add(n);
        }
        return path;
    }
    public ArrayList<Node> findPath(int startX, int startY, int endX, int endY) {
        reset();
        this.start = nodes[startY][startX];
        this.end = nodes[endY][endX];

        t0 = System.currentTimeMillis();
        t1 = Long.MAX_VALUE;
        start.gScore = 0;
        start.fScore = dist(start, end);
        Comparator<Node> comparator = new NodeComparator();
        PriorityQueue<Node> openSet = new PriorityQueue<Node>(10, comparator);
        HashMap<Node, Object> closedSet = new HashMap<Node, Object>();
        openSet.add(start);
        parent = new HashMap<Node, Node>();
        Node successors[];
        t0 = System.currentTimeMillis();
        t1 = Long.MAX_VALUE;
        while (openSet.size() > 0) {
            Node current = openSet.remove();
            if (current == end) {
                t1 = System.currentTimeMillis();
                return reconstructPath();
            }
            closedSet.put(current, null);
            successors = idSuccessors(current);
            for (int i = 0; i < successors.length; i++) {
                Node n = successors[i];
                if(n == null) continue;
                if(closedSet.containsKey(n)) continue;
                double tentative_gScore = current.gScore + dist(current, n);
                //not a better path
                if (tentative_gScore >= n.gScore) continue;
                //it's good. save it
                n.gScore = tentative_gScore;
                n.fScore = n.gScore + dist(n, end);
                openSet.add(n);
                parent.put(n, current);
            }
        }
        return null;
    }
}