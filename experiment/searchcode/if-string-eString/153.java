package org.eclipselabs.spray2.xtext.scoping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.common.types.JvmEnumerationLiteral;
import org.eclipse.xtext.common.types.JvmEnumerationType;
import org.eclipse.xtext.common.types.JvmGenericType;
import org.eclipse.xtext.common.types.JvmMember;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.common.types.TypesPackage;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.resource.EObjectDescription;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.scoping.Scopes;
import org.eclipse.xtext.scoping.impl.FilteringScope;
import org.eclipse.xtext.scoping.impl.ImportNormalizer;
import org.eclipse.xtext.scoping.impl.ImportScope;
import org.eclipse.xtext.scoping.impl.MapBasedScope;
import org.eclipse.xtext.xbase.scoping.XbaseScopeProvider;
import org.eclipselabs.spray.shapes.scoping.ConnectionScopeRestrictor;
import org.eclipselabs.spray.shapes.scoping.ShapeScopeRestrictor;
import org.eclipselabs.spray.styles.scoping.StyleScopeRestrictor;
import org.eclipselabs.spray2.xtext.spray2.Diagram;
import org.eclipselabs.spray2.xtext.spray2.EdgeBehaviorSection;
import org.eclipselabs.spray2.xtext.spray2.EdgeElement;
import org.eclipselabs.spray2.xtext.spray2.EdgeFigureSection;
import org.eclipselabs.spray2.xtext.spray2.NodeElement;
import org.eclipselabs.spray2.xtext.spray2.NodeFigureSection;
import org.eclipselabs.spray2.xtext.spray2.Spray2Package;
import org.eclipselabs.spray2.xtext.spray2.SprayStyleRef;
import org.eclipselabs.spray2.xtext.util.CustomFeatureRestrictor;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;

@SuppressWarnings("restriction")
public class Spray2ScopeProvider extends XbaseScopeProvider {

    @Inject
    private IQualifiedNameProvider qnProvider;

    @Override
    public IScope getScope(EObject context, EReference reference) {
        IScope scope = IScope.NULLSCOPE;
        if (reference == Spray2Package.Literals.DIAGRAM__MODEL_TYPE) {
            scope = scope_Diagram_ModelType(context, reference);
        } else if (reference == Spray2Package.Literals.SPRAY_ELEMENT__TYPE) {
            scope = scope_MetaClass_Type(context, reference);
        } else if (reference == TypesPackage.Literals.JVM_PARAMETERIZED_TYPE_REFERENCE__TYPE) {
            scope = scope_JvmParametrizedTypeReference_type(context, reference);
        } else if (reference == Spray2Package.Literals.TEXT_PROPERTY_ASSIGNMENT__ATTRIBUTE) {
            return scope_TextPropertyAssignment_Attribute(context);
        } else if (reference == Spray2Package.Literals.TEXT_PROPERTY_ASSIGNMENT__KEY) {
            scope = scope_TextPropertyAssignment_Key(context, reference);
        } else if (reference == Spray2Package.Literals.NODE_BEHAVIOR_SECTION__CREATABLE_IN) {
            scope = scope_NodeBehaviorSectionCreateableIn(context, reference);
        } else if (reference == Spray2Package.Literals.EDGE_BEHAVIOR_SECTION__CREATABLE_IN) {
            scope = scope_EdgeBehaviorSectionCreateableIn(context, reference);
        } else if (reference == Spray2Package.Literals.NODE_BEHAVIOR_SECTION__EDIT_ON_CREATE || reference == Spray2Package.Literals.EDGE_BEHAVIOR_SECTION__EDIT_ON_CREATE) {
            scope = scope_BehaviorSectionEditOnCreate(context, reference);
        } else if (reference == Spray2Package.Literals.NODE_BEHAVIOR_SECTION__REALIZED_BY || reference == Spray2Package.Literals.EDGE_BEHAVIOR_SECTION__REALIZED_BY) {
            scope = scope_BehaviorSectionCustomRealizedBy(context, reference);
        } else if (reference == Spray2Package.Literals.EDGE_BEHAVIOR_SECTION__SOURCE) {
            scope = scope_EdgeBehaviorSection_source(context, reference);
        } else if (reference == Spray2Package.Literals.EDGE_BEHAVIOR_SECTION__TARGET) {
            scope = scope_EdgeBehaviorSection_target(context, reference);
        } else {
            scope = super.getScope(context, reference);
        }
        return scope;
    }

    private IScope getEnumerationLiteralsScopeForShape(JvmGenericType type, String className) {
        JvmEnumerationType enumType = null;
        for (JvmMember member : type.getMembers()) {
            if (member.getSimpleName().equals(className)) {
                enumType = (JvmEnumerationType) member;
            }
        }
        List<IEObjectDescription> descrList = new ArrayList<IEObjectDescription>();
        if (enumType != null) {
            for (JvmEnumerationLiteral literal : enumType.getLiterals()) {
                IEObjectDescription description = EObjectDescription.create(literal.getSimpleName(), literal, null);
                descrList.add(description);
            }
        }
        return MapBasedScope.createScope(IScope.NULLSCOPE, descrList);
    }

    private IScope scope_BehaviorSectionCustomRealizedBy(EObject context, EReference reference) {
        IScope typesScope = delegateGetScope(context, TypesPackage.Literals.JVM_PARAMETERIZED_TYPE_REFERENCE__TYPE);
        Predicate<IEObjectDescription> stylesFilter = new CustomFeatureRestrictor();
        IScope result = new FilteringScope(typesScope, stylesFilter);
        return result;
    }

    private IScope scope_BehaviorSectionEditOnCreate(EObject context, EReference reference) {
        NodeElement nodeElement = EcoreUtil2.getContainerOfType(context, NodeElement.class);
        EdgeElement edgeElement = EcoreUtil2.getContainerOfType(context, EdgeElement.class);
        if (nodeElement != null || edgeElement != null) {
            Predicate<EObject> filterPredicate = new Predicate<EObject>() {
                @Override
                public boolean apply(EObject input) {
                    if (input instanceof EAttribute) {
                        if (((EAttribute) input).getEType().getName().equals("EString")) {
                            return true;
                        }
                    }
                    return false;
                }
            };
            if (nodeElement != null) {
                return MapBasedScope.createScope(IScope.NULLSCOPE, Scopes.scopedElementsFor(Iterables.filter(nodeElement.getType().getEAllAttributes(), filterPredicate)));
            } else {
                return MapBasedScope.createScope(IScope.NULLSCOPE, Scopes.scopedElementsFor(Iterables.filter(edgeElement.getType().getEAllAttributes(), filterPredicate)));
            }
        } else {
            return IScope.NULLSCOPE;
        }
    }

    private IScope scope_Diagram_ModelType(EObject context, EReference reference) {
        // filter out types with URL schema qualified names
        final IScope scope = delegateGetScope(context, reference);
        final Predicate<IEObjectDescription> filter = new Predicate<IEObjectDescription>() {
            @Override
            public boolean apply(IEObjectDescription input) {
                return !input.getQualifiedName().toString().startsWith("http://");
            }
        };
        return new FilteringScope(scope, filter);
    }

    private IScope scope_EdgeBehaviorSection_source(EObject context, EReference reference) {
        final EdgeElement edgeElement = EcoreUtil2.getContainerOfType(context, EdgeElement.class);
        // filter derived and 'from' from the possible references
        Iterable<EReference> targetReferences = Iterables.filter(edgeElement.getType().getEAllReferences(), new Predicate<EReference>() {
            @Override
            public boolean apply(EReference input) {
                return !input.isDerived();
            }
        });
        final IScope result = MapBasedScope.createScope(IScope.NULLSCOPE, Scopes.scopedElementsFor(targetReferences));
        return result;
    }

    private IScope scope_EdgeBehaviorSection_target(EObject context, EReference reference) {
        final EdgeElement edgeElement = EcoreUtil2.getContainerOfType(context, EdgeElement.class);
        final EdgeBehaviorSection edgeBehaviorSection = EcoreUtil2.getContainerOfType(context, EdgeBehaviorSection.class);
        // filter derived references
        Iterable<EReference> targetReferences = Iterables.filter(edgeElement.getType().getEAllReferences(), new Predicate<EReference>() {
            @Override
            public boolean apply(EReference input) {
                return input != edgeBehaviorSection.getSource() && !input.isDerived();
            }
        });
        final IScope result = MapBasedScope.createScope(IScope.NULLSCOPE, Scopes.scopedElementsFor(targetReferences));
        return result;
    }

    private IScope scope_EdgeBehaviorSectionCreateableIn(EObject context, EReference reference) {
        final Diagram diagram = EcoreUtil2.getContainerOfType(context, Diagram.class);
        final EdgeElement edgeElement = EcoreUtil2.getContainerOfType(context, EdgeElement.class);
        if (diagram == null || edgeElement == null) {
            return IScope.NULLSCOPE;
        }
        final EClass diagramModelType = diagram.getModelType();
        Predicate<EReference> filter = new Predicate<EReference>() {
            @Override
            public boolean apply(EReference input) {
                boolean superType = false;
                if (edgeElement != null && edgeElement.getType() != null) {
                    superType = input.getEReferenceType().isSuperTypeOf(edgeElement.getType());
                }
                return superType;
            }
        };
        // get all containments of EClass contained in this package
        List<EReference> containmentReferences = new ArrayList<EReference>();
        containmentReferences.addAll(diagramModelType.getEAllContainments());
        //        // if the MetaClass is a connection take also the containment dependencies of the source type
        //        EClass sourceType = (EClass) ((EdgeBehaviorSection) edgeElement.getBehavior()).getSource().getEType();
        //        if (sourceType != null) {
        //            containmentReferences.addAll(sourceType.getEAllContainments());
        //        }
        return Scopes.scopeFor(Iterables.filter(containmentReferences, filter));
    }

    private IScope scope_EdgeFigureSectionRefScope(EdgeFigureSection edgeFigSec, EObject context, EReference reference) {
        IScope typesScope = delegateGetScope(edgeFigSec, TypesPackage.Literals.JVM_PARAMETERIZED_TYPE_REFERENCE__TYPE);
        Predicate<IEObjectDescription> stylesFilter = new ConnectionScopeRestrictor();
        IScope result = new FilteringScope(typesScope, stylesFilter);
        return result;
    }

    private IScope scope_JvmParametrizedTypeReference_type(EObject context, EReference reference) {
        SprayStyleRef styleRef = EcoreUtil2.getContainerOfType(context, SprayStyleRef.class);
        if (styleRef != null) {
            return scope_ShapeStyleRefScope(styleRef, context, reference);
        }
        NodeFigureSection nodeFigSec = EcoreUtil2.getContainerOfType(context, NodeFigureSection.class);
        if (nodeFigSec != null) {
            return scope_NodeFigureSectionRefScope(nodeFigSec, context, reference);
        }
        EdgeFigureSection edgeFigSec = EcoreUtil2.getContainerOfType(context, EdgeFigureSection.class);
        if (edgeFigSec != null) {
            return scope_EdgeFigureSectionRefScope(edgeFigSec, context, reference);
        }
        return IScope.NULLSCOPE;
    }

    private IScope scope_MetaClass_Type(EObject context, EReference reference) {
        Diagram diagram = EcoreUtil2.getContainerOfType(context, Diagram.class);
        // all eClasses that are direct containments of context's diagram model type
        final EClass diagramModelType = diagram.getModelType();
        if (diagramModelType == null || diagramModelType.getEPackage() == null) {
            return IScope.NULLSCOPE;
        }
        final Predicate<EClassifier> filter = new Predicate<EClassifier>() {
            @Override
            public boolean apply(EClassifier input) {
                return input instanceof EClass && input != diagramModelType && !((EClass) input).isAbstract();
            }
        };
        final Function<EClassifier, IEObjectDescription> toObjDesc = new Function<EClassifier, IEObjectDescription>() {
            @Override
            public IEObjectDescription apply(EClassifier input) {
                return EObjectDescription.create(qnProvider.apply(input), input);
            }
        };
        // Implicit import of the EPackage of the Diagram Model type 
        final List<ImportNormalizer> normalizer = Collections.singletonList(new ImportNormalizer(qnProvider.apply(diagramModelType.getEPackage()), true, false));
        final ImportScope importDiagramTypePackage = new ImportScope(normalizer, delegateGetScope(context, reference), null, null, false);
        final Iterable<IEObjectDescription> descriptions = Iterables.transform(Iterables.filter(diagramModelType.getEPackage().getEClassifiers(), filter), toObjDesc);
        // the delegate scope will provide import scopes
        return MapBasedScope.createScope(importDiagramTypePackage, descriptions);
    }

    private IScope scope_NodeBehaviorSectionCreateableIn(EObject context, EReference reference) {
        Diagram diagram = EcoreUtil2.getContainerOfType(context, Diagram.class);
        NodeElement nodeElement = EcoreUtil2.getContainerOfType(context, NodeElement.class);
        if (diagram == null || nodeElement == null) {
            return IScope.NULLSCOPE;
        }
        // all eClasses that are direct containments of context's diagram model type
        final EClass diagramModelType = diagram.getModelType();
        if (diagramModelType == null || diagramModelType.getEPackage() == null) {
            return IScope.NULLSCOPE;
        }
        List<EReference> containmentReferences = new ArrayList<EReference>();
        for (EClassifier eClassifiers : diagramModelType.getEPackage().getEClassifiers()) {
            if (eClassifiers instanceof EClass) {
                for (EReference ref : ((EClass) eClassifiers).getEAllContainments()) {
                    if (ref.getEType() instanceof EClass && ((EClass) ref.getEType()).isSuperTypeOf(nodeElement.getType())) {
                        containmentReferences.add(ref);
                    }
                }
            }
        }
        return Scopes.scopeFor(containmentReferences);
    }

    private IScope scope_NodeFigureSectionRefScope(NodeFigureSection nodeFigSec, EObject context, EReference reference) {
        IScope typesScope = delegateGetScope(nodeFigSec, TypesPackage.Literals.JVM_PARAMETERIZED_TYPE_REFERENCE__TYPE);
        Predicate<IEObjectDescription> stylesFilter = new ShapeScopeRestrictor();
        IScope result = new FilteringScope(typesScope, stylesFilter);
        return result;
    }

    private IScope scope_ShapeStyleRefScope(SprayStyleRef styleRef, EObject context, EReference reference) {
        IScope typesScope = delegateGetScope(styleRef, TypesPackage.Literals.JVM_PARAMETERIZED_TYPE_REFERENCE__TYPE);
        Predicate<IEObjectDescription> stylesFilter = new StyleScopeRestrictor();
        IScope result = new FilteringScope(typesScope, stylesFilter);
        return result;
    }

    private IScope scope_TextPropertyAssignment_Attribute(EObject context) {
        NodeElement nodeElement = EcoreUtil2.getContainerOfType(context, NodeElement.class);
        EdgeElement edgeElement = EcoreUtil2.getContainerOfType(context, EdgeElement.class);
        if (nodeElement != null || edgeElement != null) {
            Predicate<EObject> filterPredicate = new Predicate<EObject>() {
                @Override
                public boolean apply(EObject input) {
                    if (input instanceof EAttribute) {
                        if (((EAttribute) input).getEType().getName().equals("EString")) {
                            return true;
                        }
                    }
                    return false;
                }
            };
            if (nodeElement != null) {
                return MapBasedScope.createScope(IScope.NULLSCOPE, Scopes.scopedElementsFor(Iterables.filter(nodeElement.getType().getEAllAttributes(), filterPredicate)));
            } else {
                return MapBasedScope.createScope(IScope.NULLSCOPE, Scopes.scopedElementsFor(Iterables.filter(edgeElement.getType().getEAllAttributes(), filterPredicate)));
            }
        } else {
            return IScope.NULLSCOPE;
        }
    }

    private IScope scope_TextPropertyAssignment_Key(EObject context, EReference reference) {
        JvmType jvmType = null;
        final String className = "TextIds";
        final NodeFigureSection nodeFigSec = EcoreUtil2.getContainerOfType(context, NodeFigureSection.class);
        final EdgeFigureSection edgeFigSec = EcoreUtil2.getContainerOfType(context, EdgeFigureSection.class);
        if (nodeFigSec != null) {
            jvmType = nodeFigSec.getShapeRef().getType();
        } else if (edgeFigSec != null) {
            jvmType = edgeFigSec.getShapeRef().getType();
        }
        if (jvmType != null && jvmType instanceof JvmGenericType) {
            return getEnumerationLiteralsScopeForShape((JvmGenericType) jvmType, className);
        } else {
            return IScope.NULLSCOPE;
        }
    }
}

