package org.cpsolver.exam.neighbours;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cpsolver.exam.criteria.DistributionPenalty;
import org.cpsolver.exam.criteria.RoomPenalty;
import org.cpsolver.exam.criteria.RoomSizePenalty;
import org.cpsolver.exam.model.Exam;
import org.cpsolver.exam.model.ExamDistributionConstraint;
import org.cpsolver.exam.model.ExamModel;
import org.cpsolver.exam.model.ExamPeriodPlacement;
import org.cpsolver.exam.model.ExamPlacement;
import org.cpsolver.exam.model.ExamRoomPlacement;
import org.cpsolver.exam.model.ExamRoomSharing;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.heuristics.NeighbourSelection;
import org.cpsolver.ifs.model.LazySwap;
import org.cpsolver.ifs.model.Neighbour;
import org.cpsolver.ifs.solution.Solution;
import org.cpsolver.ifs.solver.Solver;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.ifs.util.ToolBox;


/**
 * Try to swap a period between two exams. 
 * Two examinations are randomly selected. A new placement is generated by swapping periods of the two exams.
 * For each exam, the best possible room placement is found. If the two exams are in the same period, it just tries
 * to change the room assignments by looking for the best available room placement ignoring the existing room assignments
 * of the two exams. If no conflict results from the swap the assignment is returned.
 * The following exams of the second exam in the pair are tried for an exam swap otherwise.
 * <br><br>
 * 
 * @version ExamTT 1.3 (Examination Timetabling)<br>
 *          Copyright (C) 2013 - 2014 Tomas Muller<br>
 *          <a href="mailto:muller@unitime.org">muller@unitime.org</a><br>
 *          <a href="http://muller.unitime.org">http://muller.unitime.org</a><br>
 * <br>
 *          This library is free software; you can redistribute it and/or modify
 *          it under the terms of the GNU Lesser General Public License as
 *          published by the Free Software Foundation; either version 3 of the
 *          License, or (at your option) any later version. <br>
 * <br>
 *          This library is distributed in the hope that it will be useful, but
 *          WITHOUT ANY WARRANTY; without even the implied warranty of
 *          MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *          Lesser General Public License for more details. <br>
 * <br>
 *          You should have received a copy of the GNU Lesser General Public
 *          License along with this library; if not see
 *          <a href='http://www.gnu.org/licenses/'>http://www.gnu.org/licenses/</a>.
 */
public class ExamPeriodSwapMove implements NeighbourSelection<Exam,ExamPlacement> {
    private boolean iCheckStudentConflicts = false;
    private boolean iCheckDistributionConstraints = true;
    
    /**
     * Constructor
     * @param properties problem properties
     */
    public ExamPeriodSwapMove(DataProperties properties) {
        iCheckStudentConflicts = properties.getPropertyBoolean("ExamPeriodSwapMove.CheckStudentConflicts", iCheckStudentConflicts);
        iCheckDistributionConstraints = properties.getPropertyBoolean("ExamPeriodSwapMove.CheckDistributionConstraints", iCheckDistributionConstraints);
    }
    
    /**
     * Initialization
     */
    @Override
    public void init(Solver<Exam,ExamPlacement> solver) {}

    /**
     * Select an exam randomly,
     * select an available period randomly (if it is not assigned), 
     * use rooms if possible, select rooms using {@link Exam#findBestAvailableRooms(Assignment, ExamPeriodPlacement)} if not (exam is unassigned, a room is not available or used).
     */
    @Override
    public Neighbour<Exam,ExamPlacement> selectNeighbour(Solution<Exam,ExamPlacement> solution) {
        ExamModel model = (ExamModel)solution.getModel();
        Assignment<Exam, ExamPlacement> assignment = solution.getAssignment();
        Exam x1 = ToolBox.random(model.variables());
        ExamPlacement v1 = assignment.getValue(x1);
        if (v1 == null) return null;
        int x = ToolBox.random(model.variables().size());
        for (int v = 0; v < model.variables().size(); v++) {
            Exam x2 = model.variables().get((v + x) % (model.variables().size()));
            ExamPlacement v2 = assignment.getValue(x2);
            if (x1.equals(x2) || v2 == null) continue;
            ExamPeriodPlacement p1 = x1.getPeriodPlacement(v2.getPeriod());
            ExamPeriodPlacement p2 = x2.getPeriodPlacement(v1.getPeriod());
            if (p1 == null || p2 == null) continue;
            if (iCheckStudentConflicts && (x1.countStudentConflicts(assignment, p1) > 0 || x2.countStudentConflicts(assignment, p2) > 0)) continue;
            if (iCheckDistributionConstraints) {
                Map<Exam, ExamPlacement> placements = new HashMap<Exam, ExamPlacement>();
                placements.put(x1, new ExamPlacement(x1, p1, new HashSet<ExamRoomPlacement>()));
                placements.put(x2, new ExamPlacement(x2, p2, new HashSet<ExamRoomPlacement>()));
                if (!checkDistributionConstraints(assignment, x1, p1, placements) || !checkDistributionConstraints(assignment, x2, p2, placements)) continue;
            }
            Set<ExamPlacement> conflicts = new HashSet<ExamPlacement>();
            conflicts.add(v1); conflicts.add(v2);
            Map<Exam, ExamPlacement> placements = new HashMap<Exam, ExamPlacement>();
            Set<ExamRoomPlacement> r1 = findBestAvailableRooms(assignment, x1, p1, conflicts, placements);
            if (r1 == null) continue;
            placements.put(x1, new ExamPlacement(x1, p1, r1));
            Set<ExamRoomPlacement> r2 = findBestAvailableRooms(assignment, x2, p2, conflicts, placements);
            if (r2 == null) continue;
            return new LazySwap<Exam, ExamPlacement>(new ExamPlacement(x1, p1, r1), new ExamPlacement(x2, p2, r2));
        }
        return null;
    }
    
    public boolean checkDistributionConstraints(Assignment<Exam, ExamPlacement> assignment, Exam exam, ExamPeriodPlacement period, Map<Exam, ExamPlacement> placements) {
        for (ExamDistributionConstraint dc : exam.getDistributionConstraints()) {
            if (!dc.isHard())
                continue;
            boolean before = true;
            for (Exam other : dc.variables()) {
                if (other.equals(this)) {
                    before = false;
                    continue;
                }
                ExamPlacement placement = (placements.containsKey(other) ? placements.get(other) : assignment.getValue(other));
                if (placement == null) continue;
                switch (dc.getType()) {
                    case ExamDistributionConstraint.sDistSamePeriod:
                        if (period.getIndex() != placement.getPeriod().getIndex())
                            return false;
                        break;
                    case ExamDistributionConstraint.sDistDifferentPeriod:
                        if (period.getIndex() == placement.getPeriod().getIndex())
                            return false;
                        break;
                    case ExamDistributionConstraint.sDistPrecedence:
                        if (before) {
                            if (period.getIndex() <= placement.getPeriod().getIndex())
                                return false;
                        } else {
                            if (period.getIndex() >= placement.getPeriod().getIndex())
                                return false;
                        }
                        break;
                    case ExamDistributionConstraint.sDistPrecedenceRev:
                        if (before) {
                            if (period.getIndex() >= placement.getPeriod().getIndex())
                                return false;
                        } else {
                            if (period.getIndex() <= placement.getPeriod().getIndex())
                                return false;
                        }
                        break;
                }
            }
        }
        return true;
    }
    
    public boolean checkDistributionConstraints(Assignment<Exam, ExamPlacement> assignment, Exam exam, ExamRoomPlacement room, Set<ExamPlacement> conflictsToIgnore, Map<Exam, ExamPlacement> placements) {
        for (ExamDistributionConstraint dc : exam.getDistributionConstraints()) {
            if (!dc.isHard())
                continue;
            for (Exam other : dc.variables()) {
                if (other.equals(exam)) continue;
                ExamPlacement placement = (placements.containsKey(other) ? placements.get(other) : assignment.getValue(other));
                if (placement == null || conflictsToIgnore.contains(placement)) continue;
                switch (dc.getType()) {
                    case ExamDistributionConstraint.sDistSameRoom:
                        if (!placement.getRoomPlacements().contains(room))
                            return false;
                        break;
                    case ExamDistributionConstraint.sDistDifferentRoom:
                        if (placement.getRoomPlacements().contains(room))
                            return false;
                        break;
                }
            }
        }
        return true;
    }
    
    public int getDistributionConstraintPenalty(Assignment<Exam, ExamPlacement> assignment, Exam exam, ExamRoomPlacement room,  Set<ExamPlacement> conflictsToIgnore, Map<Exam, ExamPlacement> placements) {
        int penalty = 0;
        for (ExamDistributionConstraint dc : exam.getDistributionConstraints()) {
            if (dc.isHard()) continue;
            for (Exam other : dc.variables()) {
                if (other.equals(this)) continue;
                ExamPlacement placement = (placements.containsKey(other) ? placements.get(other) : assignment.getValue(other));
                if (placement == null || conflictsToIgnore.contains(placement)) continue;
                switch (dc.getType()) {
                    case ExamDistributionConstraint.sDistSameRoom:
                        if (!placement.getRoomPlacements().contains(room))
                            penalty += dc.getWeight();
                        break;
                    case ExamDistributionConstraint.sDistDifferentRoom:
                        if (placement.getRoomPlacements().contains(room))
                            penalty += dc.getWeight();
                        break;
                }
            }
        }
        return penalty;
    }
    
    public Set<ExamRoomPlacement> findBestAvailableRooms(Assignment<Exam, ExamPlacement> assignment, Exam exam, ExamPeriodPlacement period, Set<ExamPlacement> conflictsToIgnore, Map<Exam, ExamPlacement> placements) {
        if (exam.getMaxRooms() == 0)
            return new HashSet<ExamRoomPlacement>();
        double sw = exam.getModel().getCriterion(RoomSizePenalty.class).getWeight();
        double pw = exam.getModel().getCriterion(RoomPenalty.class).getWeight();
        double cw = exam.getModel().getCriterion(DistributionPenalty.class).getWeight();
        ExamRoomSharing sharing = ((ExamModel)exam.getModel()).getRoomSharing();
        loop: for (int nrRooms = 1; nrRooms <= exam.getMaxRooms(); nrRooms++) {
            HashSet<ExamRoomPlacement> rooms = new HashSet<ExamRoomPlacement>();
            int size = 0;
            while (rooms.size() < nrRooms && size < exam.getSize()) {
                int minSize = (exam.getSize() - size) / (nrRooms - rooms.size());
                ExamRoomPlacement best = null;
                double bestWeight = 0;
                int bestSize = 0;
                for (ExamRoomPlacement room : exam.getRoomPlacements()) {
                    if (!room.isAvailable(period.getPeriod())) continue;
                    if (rooms.contains(room)) continue;
                    
                    List<ExamPlacement> overlaps = new ArrayList<ExamPlacement>();
                    for (ExamPlacement overlap: room.getRoom().getPlacements(assignment, period.getPeriod()))
                        if (!conflictsToIgnore.contains(overlap)) overlaps.add(overlap);
                    for (ExamPlacement other: placements.values())
                        if (other.getPeriod().equals(period.getPeriod()))
                            for (ExamRoomPlacement r: other.getRoomPlacements())
                                if (r.getRoom().equals(room.getRoom())) {
                                    overlaps.add(other);
                                    continue;
                                }
                    
                    if (nrRooms == 1 && sharing != null) {
                        if (sharing.inConflict(exam, overlaps, room.getRoom()))
                            continue;
                    } else {
                        if (!overlaps.isEmpty())
                            continue;
                    }
                    if (iCheckDistributionConstraints && !checkDistributionConstraints(assignment, exam, room, conflictsToIgnore, placements)) continue;
                    int s = room.getSize(exam.hasAltSeating());
                    if (s < minSize) break;
                    int p = room.getPenalty(period.getPeriod());
                    double w = pw * p + sw * (s - minSize) + cw * getDistributionConstraintPenalty(assignment, exam, room, conflictsToIgnore, placements);
                    double d = 0;
                    if (!rooms.isEmpty()) {
                        for (ExamRoomPlacement r : rooms) {
                            d += r.getDistanceInMeters(room);
                        }
                        w += d / rooms.size();
                    }
                    if (best == null || bestWeight > w) {
                        best = room;
                        bestSize = s;
                        bestWeight = w;
                    }
                }
                if (best == null)
                    continue loop;
                rooms.add(best);
                size += bestSize;
            }
            if (size >= exam.getSize())
                return rooms;
        }
        return null;
    }
}

