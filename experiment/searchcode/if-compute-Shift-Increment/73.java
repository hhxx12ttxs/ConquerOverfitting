* by Sun in the GPL Version 2 section of the License file that
* accompanied this code. If applicable, add the following below the
return computePrimitiveType(types, info, ((ForLoopTree) currentPath.getLeaf()).getCondition(), unresolved, TypeKind.BOOLEAN);
case IF:
return computePrimitiveType(types, info, ((IfTree) currentPath.getLeaf()).getCondition(), unresolved, TypeKind.BOOLEAN);

