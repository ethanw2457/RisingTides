package tides;

/**
 * DO NOT EDIT!
 * This class is an implmentation of Weighted Quick Union. 
 * 
 * Feel free to take a look at the method descriptions for a better 
 * understanding of Weighted Quick-Union.
 * 
 * @author Vian Miranda (Rutgers University)
 */
public class WeightedQuickUnionUF {

    // Each parent[i][j] will contain a GridLocation of its parent
    private GridLocation[][] parent;

    // Each size[i][j] is the number of children the node has, including itself
    private int[][] size;


    /**
     * Constructor for WeightedQuickUnionUF.
     * 
     * Instantiates the total number of rows and columns provided (size of the 
     * .terrain file) and the parent and size 2d arrays.
     * 
     * @param rows of the grid provided from the .terrain file
     * @param cols of the grid provided from the .terrain file
     */
    public WeightedQuickUnionUF(int rows, int cols) {
        parent = new GridLocation[rows][cols];
        size = new int[rows][cols];

        for (int rr = 0; rr < rows; rr++) {
            for (int cc = 0; cc < cols; cc++) {
                parent[rr][cc] = new GridLocation(rr, cc);
                size[rr][cc] = 1;
            }
        }
    }
    
    /**
     * Since this is a Quick-Union implementation, this method iteratively goes 
     * up the parent nodes until arriving at the root node (curr == currParent).
     * 
     * Thanks to using Weighted Union rather than the normal Union, the Find 
     * operation time complexity is O(logn) compared to O(n).
     * 
     * @param cell location of which we want to find the root parent
     * @return GridLocation, parent cell
     */
    public GridLocation find(GridLocation cell) {
        GridLocation currParent = parent[cell.row][cell.col];

        while (cell.hashCode() != currParent.hashCode() || !cell.equals(currParent)) {
            cell = new GridLocation(currParent.row, currParent.col);
            currParent = parent[cell.row][cell.col];
        }

        return currParent;
    }

    /**
     * Implementation of Weighted Union.
     * 
     * Find the root parents of the cell parameters, find the larger branch, 
     * set the the smaller branch as the child of the larger branch, then set 
     * the size of the larger branch as the sum of both branches. 
     * 
     * Union is limited to the time complexity of the Find operation, as all 
     * the other operations take constant time. Due to the improvement of 
     * making this operation Weighted, the Find operation is sped up to a time 
     * complexity of O(logn), thus making this implementation of Union also 
     * O(logn), compared to the O(n) time complexity for the normal Union. 
     * 
     * @param cell1 location of which we want to union
     * @param cell2 location of which we want to union
     */
    public void union(GridLocation cell1, GridLocation cell2) {
        GridLocation root1 = find(cell1);
        GridLocation root2 = find(cell2);
        if (root1 == root2) return;

        // If root1 is root of larger tree, this makes it root of smaller tree
        if (getSize(root1) >= getSize(root2)) {
            GridLocation temp = root1;
            root1 = root2;
            root2 = temp;
        }

        parent[root1.row][root1.col] = root2;
        size[root2.row][root2.col] += getSize(root1);
    }

    /**
     * @param cell location of which we want the size of
     * @return int, the number of child nodes (including itself)
     */
    public int getSize(GridLocation cell) {
        return size[cell.row][cell.col];
    }
}