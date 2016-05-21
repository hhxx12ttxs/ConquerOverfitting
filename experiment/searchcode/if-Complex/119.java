    public VoidValue visitList(ListPattern p) {
     if (checkContext(DISALLOW_LIST, \"list\", p) && !alreadyChecked(p))
        p.getChild().accept(listVisitor);
          er.error(\"group_data_other_children\
        if (complex)
      if (checkContext(DISALLOW_EMPTY, \"optional\", p))
    public VoidValue visitOptional(OptionalPattern p) {
        if ((complex && simpleCount > 0) || (simple && hadComplex)) {
        super.visitOptional(p);
    public VoidValue visitDefine(DefineComponent c) {
      if (c.getName() != DefineComponent.START)
        c.getBody().accept(topLevelVisitor);
          hadComplex = true;
          simpleCount++;
        boolean complex = ct.contains(ChildType.TEXT) || ct.contains(ChildType.ELEMENT);

