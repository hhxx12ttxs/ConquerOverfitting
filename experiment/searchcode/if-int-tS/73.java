this.logicalTs = logicalTs;
this.peerId = peerId;
}

public int compareTo(ItemID other) {
if (this.logicalTs < other.logicalTs)
return -1;
if (this.logicalTs > other.logicalTs)
return 1;
return this.peerId.compareTo(other.peerId);

