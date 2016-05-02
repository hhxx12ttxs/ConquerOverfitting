/*
 * This file is part of the GeoLatte project.
 *
 *     GeoLatte is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     GeoLatte is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with GeoLatte.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2010 - 2011 and Ownership of code is shared by:
 * Qmino bvba - Esperantolaan 4 - 3001 Heverlee  (http://www.qmino.com)
 * Geovise bvba - Generaal Eisenhowerlei 9 - 2140 Antwerpen (http://www.geovise.com)
 */

package org.geolatte.graph.algorithms;

import org.geolatte.graph.PredGraph;

/**
 * <p>
 * Encapsulates the relaxation step of a shortest path algorithm.
 * </p>
 * <p>
 * From "Introduction to Algorithms 3rd edition":
 * The process of relaxing an edge (u, v) consists of testing whether we can improve the shortest path to v found so
 * far
 * by going through u and, if so, updating (decreasing) the new total weight of v and its predecessor internalNode
 * </p>
 *
 * @param <N> The type of node.
 * @param <E> The edge label type.
 * @author Karel Maesen
 * @author Bert Vanhooff
 */
interface Relaxer<N, E> {

    /**
     * Relaxes the edge from internalNode {@code u} to internalNode {@code v}. Both are given by their predecessor
     * graphs.
     *
     * @param u           Predecessor graph representing current shortest path to internalNode u.
     * @param v           Predecessor graph representing current shortest path to internalNode v.
     * @param weightIndex The index used to lookup the weight.
     * @return True if the weight of v was updated, false otherwise
     */
    public boolean relax(PredGraph<N, E> u, PredGraph<N, E> v, int weightIndex);

    /**
     * Returns the new total weight of the path to internalNode v after relaxation has occured. Is only meaningful
     * after
     * {@link #relax(PredGraph, PredGraph, int)}
     * has been called.
     *
     * @return The new total weight of the path to internalNode v.
     */
    public float newTotalWeight();

}

