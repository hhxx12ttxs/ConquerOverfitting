/*
Given a binary tree and a sum, find all root-to-leaf paths where each path's sum equals the given sum.

For example:
Given the below binary tree and sum = 22,
              5
             / \
            4   8
           /   / \
          11  13  4
         /  \    / \
        7    2  5   1
return

[
   [5,4,11,2],
   [5,8,4,5]
]
*/

/**
 * Definition for binary tree
 * public class TreeNode {
 *     int val;
 *     TreeNode left;
 *     TreeNode right;
 *     TreeNode(int x) { val = x; }
 * }
 */

//By Ke Hu (mrhuke@gmail.com), 2013
public class Solution {
    
    public void pathSum(TreeNode root, int sum, ArrayList<ArrayList<Integer>> allPath, ArrayList<Integer> curPath)
    {
        if (root == null) return;
        if (sum == root.val && root.left == null && root.right == null){
            curPath.add(root.val);
            allPath.add(new ArrayList<Integer>(curPath));
            curPath.remove(curPath.size()-1);
            return;
        }
    
        curPath.add(root.val);
        pathSum(root.left, sum-root.val, allPath, curPath);
        pathSum(root.right, sum-root.val, allPath, curPath);
        curPath.remove(curPath.size()-1);

    }
    
    public ArrayList<ArrayList<Integer>> pathSum(TreeNode root, int sum) {
        ArrayList<ArrayList<Integer>> res = new ArrayList<ArrayList<Integer>>();
        ArrayList<Integer> cur = new ArrayList<Integer>();
        pathSum(root, sum, res, cur);
        return res;
    }
}

