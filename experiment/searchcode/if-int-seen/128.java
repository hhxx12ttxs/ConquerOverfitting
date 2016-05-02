package org.sonatype.aether.util.graph.selector;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.sonatype.aether.collection.DependencyCollectionContext;
import org.sonatype.aether.collection.DependencySelector;
import org.sonatype.aether.graph.Dependency;

/**
 * A dependency selector that combines zero or more other selectors using a logical {@code AND}.
 * 
 * @author Benjamin Bentmann
 */
public class AndDependencySelector
    implements DependencySelector
{

    private final Collection<DependencySelector> selectors;

    /**
     * Creates a new selector from the specified selectors.
     * 
     * @param selectors The selectors to combine, may be {@code null}.
     */
    public AndDependencySelector( DependencySelector... selectors )
    {
        if ( selectors != null && selectors.length > 0 )
        {
            this.selectors = new LinkedHashSet<DependencySelector>();
            Collections.addAll( this.selectors, selectors );
        }
        else
        {
            this.selectors = Collections.emptySet();
        }
    }

    /**
     * Creates a new selector from the specified selectors.
     * 
     * @param selectors The selectors to combine, may be {@code null}.
     */
    public AndDependencySelector( Set<DependencySelector> selectors )
    {
        if ( selectors != null && !selectors.isEmpty() )
        {
            this.selectors = selectors;
        }
        else
        {
            this.selectors = Collections.emptySet();
        }
    }

    /**
     * Creates a new selector from the specified selectors.
     * 
     * @param selector1 The first selector to combine, may be {@code null}.
     * @param selector2 The first selector to combine, may be {@code null}.
     * @return The combined selector or {@code null} if both selectors were {@code null}.
     */
    public static DependencySelector newInstance( DependencySelector selector1, DependencySelector selector2 )
    {
        if ( selector1 == null )
        {
            return selector2;
        }
        else if ( selector2 == null )
        {
            return selector1;
        }
        return new AndDependencySelector( selector1, selector2 );
    }

    public boolean selectDependency( Dependency dependency )
    {
        for ( DependencySelector selector : selectors )
        {
            if ( !selector.selectDependency( dependency ) )
            {
                return false;
            }
        }
        return true;
    }

    public DependencySelector deriveChildSelector( DependencyCollectionContext context )
    {
        int seen = 0;
        Set<DependencySelector> childSelectors = null;

        for ( DependencySelector selector : selectors )
        {
            DependencySelector childSelector = selector.deriveChildSelector( context );
            if ( childSelectors != null )
            {
                childSelectors.add( childSelector );
            }
            else if ( !selector.equals( childSelector ) )
            {
                childSelectors = new LinkedHashSet<DependencySelector>();
                if ( seen > 0 )
                {
                    for ( DependencySelector s : selectors )
                    {
                        if ( childSelectors.size() >= seen )
                        {
                            break;
                        }
                        childSelectors.add( s );
                    }
                }
                childSelectors.add( childSelector );
            }
            else
            {
                seen++;
            }
        }

        return childSelectors != null ? new AndDependencySelector( childSelectors ) : this;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        else if ( null == obj || !getClass().equals( obj.getClass() ) )
        {
            return false;
        }

        AndDependencySelector that = (AndDependencySelector) obj;
        return selectors.equals( that.selectors );
    }

    @Override
    public int hashCode()
    {
        int hash = getClass().hashCode();
        hash = hash * 31 + selectors.hashCode();
        return hash;
    }

}

