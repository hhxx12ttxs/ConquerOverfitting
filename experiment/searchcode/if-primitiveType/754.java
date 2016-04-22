/*     */ package com.google.javascript.rhino.jstype;
/*     */ 
/*     */ import com.google.javascript.rhino.ErrorReporter;
/*     */ import com.google.javascript.rhino.Node;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class EnumElementType
/*     */   extends ObjectType
/*     */ {
/*     */   private static final long serialVersionUID = 1L;
/*     */   private JSType primitiveType;
/*     */   private ObjectType primitiveObjectType;
/*     */   private final String name;
/*     */   
/*     */   EnumElementType(JSTypeRegistry registry, JSType elementType, String name)
/*     */   {
/*  69 */     super(registry);
/*  70 */     this.primitiveType = elementType;
/*  71 */     this.primitiveObjectType = elementType.toObjectType();
/*  72 */     this.name = name;
/*     */   }
/*     */   
/*     */   public PropertyMap getPropertyMap() {
/*  76 */     return this.primitiveObjectType == null ? PropertyMap.immutableEmptyMap() : this.primitiveObjectType.getPropertyMap();
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */   public EnumElementType toMaybeEnumElementType()
/*     */   {
/*  83 */     return this;
/*     */   }
/*     */   
/*     */   public boolean matchesNumberContext()
/*     */   {
/*  88 */     return this.primitiveType.matchesNumberContext();
/*     */   }
/*     */   
/*     */   public boolean matchesStringContext()
/*     */   {
/*  93 */     return this.primitiveType.matchesStringContext();
/*     */   }
/*     */   
/*     */   public boolean matchesObjectContext()
/*     */   {
/*  98 */     return this.primitiveType.matchesObjectContext();
/*     */   }
/*     */   
/*     */   public boolean canBeCalled()
/*     */   {
/* 103 */     return this.primitiveType.canBeCalled();
/*     */   }
/*     */   
/*     */   public boolean isObject()
/*     */   {
/* 108 */     return this.primitiveType.isObject();
/*     */   }
/*     */   
/*     */   public TernaryValue testForEquality(JSType that)
/*     */   {
/* 113 */     return this.primitiveType.testForEquality(that);
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public boolean isNullable()
/*     */   {
/* 124 */     return this.primitiveType.isNullable();
/*     */   }
/*     */   
/*     */   public boolean isNominalType()
/*     */   {
/* 129 */     return hasReferenceName();
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public int hashCode()
/*     */   {
/* 138 */     if (hasReferenceName()) {
/* 139 */       return getReferenceName().hashCode();
/*     */     }
/* 141 */     return super.hashCode();
/*     */   }
/*     */   
/*     */ 
/*     */   String toStringHelper(boolean forAnnotations)
/*     */   {
/* 147 */     String str1 = String.valueOf(String.valueOf(getReferenceName()));String str2 = String.valueOf(String.valueOf(this.primitiveType));return 3 + str1.length() + str2.length() + str1 + ".<" + str2 + ">";
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */   public String getReferenceName()
/*     */   {
/* 154 */     return this.name;
/*     */   }
/*     */   
/*     */   public boolean hasReferenceName()
/*     */   {
/* 159 */     return true;
/*     */   }
/*     */   
/*     */   public boolean isSubtype(JSType that)
/*     */   {
/* 164 */     if (JSType.isSubtypeHelper(this, that)) {
/* 165 */       return true;
/*     */     }
/* 167 */     return this.primitiveType.isSubtype(that);
/*     */   }
/*     */   
/*     */ 
/*     */   public <T> T visit(Visitor<T> visitor)
/*     */   {
/* 173 */     return (T)visitor.caseEnumElementType(this);
/*     */   }
/*     */   
/*     */   <T> T visit(RelationshipVisitor<T> visitor, JSType that) {
/* 177 */     return (T)visitor.caseEnumElementType(this, that);
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */   boolean defineProperty(String propertyName, JSType type, boolean inferred, Node propertyNode)
/*     */   {
/* 184 */     return true;
/*     */   }
/*     */   
/*     */   public ObjectType getImplicitPrototype()
/*     */   {
/* 189 */     return null;
/*     */   }
/*     */   
/*     */   public JSType findPropertyType(String propertyName)
/*     */   {
/* 194 */     return this.primitiveType.findPropertyType(propertyName);
/*     */   }
/*     */   
/*     */   public FunctionType getConstructor()
/*     */   {
/* 199 */     return this.primitiveObjectType == null ? null : this.primitiveObjectType.getConstructor();
/*     */   }
/*     */   
/*     */ 
/*     */   public JSType autoboxesTo()
/*     */   {
/* 205 */     return this.primitiveType.autoboxesTo();
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */   public JSType getPrimitiveType()
/*     */   {
/* 212 */     return this.primitiveType;
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   JSType meet(JSType that)
/*     */   {
/* 232 */     JSType meetPrimitive = this.primitiveType.getGreatestSubtype(that);
/* 233 */     if (meetPrimitive.isEmptyType()) {
/* 234 */       return null;
/*     */     }
/* 236 */     return new EnumElementType(this.registry, meetPrimitive, this.name);
/*     */   }
/*     */   
/*     */ 
/*     */   JSType resolveInternal(ErrorReporter t, StaticScope<JSType> scope)
/*     */   {
/* 242 */     this.primitiveType = this.primitiveType.resolve(t, scope);
/* 243 */     this.primitiveObjectType = ObjectType.cast(this.primitiveType);
/* 244 */     return this;
/*     */   }
/*     */ }


/* Location:              /home/bonnie/WorkStation/js.io/compilers/jsio_compile/compiler/compiler.jar!/com/google/javascript/rhino/jstype/EnumElementType.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */
