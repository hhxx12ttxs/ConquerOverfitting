for (Pair p : accumulatorArray) {

if (counter <= p.value) {
valueToAdd = p.attached;
break;
}
}

if (valueToAdd == null) {
continue;
} else {
newSet.add(valueToAdd);
}
}

return newSet;

}

}

