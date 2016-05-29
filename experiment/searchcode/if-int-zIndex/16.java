public static Node<ZIndex> formatZIndexNumber(Node<ZIndex> root) {
return Funcs._formatZIndexNumber(root, new LinkedIntArray(10));
}

private static Node<ZIndex> _formatZIndexNumber(Node<ZIndex> node, LinkedIntArray nums) {

