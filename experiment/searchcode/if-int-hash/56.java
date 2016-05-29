private final int bindHash;

/**
* Create the HashQuery.
*/
public HashQuery(HashQueryPlan planHash, int bindHash) {
this.planHash = planHash;
this.bindHash = bindHash;
}

/**
* Return the query plan hash.

