/*
// Licensed to Julian Hyde under one or more contributor license
// agreements. See the NOTICE file distributed with this work for
// additional information regarding copyright ownership.
//
// Julian Hyde licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except in
// compliance with the License. You may obtain a copy of the License at:
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
*/
package net.hydromatic.linq4j.test;

import net.hydromatic.linq4j.*;
import net.hydromatic.linq4j.expressions.*;
import net.hydromatic.linq4j.function.*;

import junit.framework.TestCase;

import java.util.*;

/**
 * Tests for LINQ4J.
 */
public class Linq4jTest extends TestCase {

    public static final Function1<Employee, String> EMP_NAME_SELECTOR =
        new Function1<Employee, String>() {
            public String apply(Employee employee) {
                return employee.name;
            }
        };

    public static final Function1<Employee, Integer> EMP_DEPTNO_SELECTOR =
        new Function1<Employee, Integer>() {
            public Integer apply(Employee employee) {
                return employee.deptno;
            }
        };

    public static final Function1<Employee, Integer> EMP_EMPNO_SELECTOR =
        new Function1<Employee, Integer>() {
            public Integer apply(Employee employee) {
                return employee.empno;
            }
        };

    public static final Function1<Department, Enumerable<Employee>>
        DEPT_EMPLOYEES_SELECTOR =
        new Function1<Department, Enumerable<Employee>>() {
            public Enumerable<Employee> apply(Department a0) {
                return Linq4j.asEnumerable(a0.employees);
            }
        };

    public static final Function1<Department, String> DEPT_NAME_SELECTOR =
        new Function1<Department, String>() {
            public String apply(Department department) {
                return department.name;
            }
        };

    public static final Function1<Department, Integer> DEPT_DEPTNO_SELECTOR =
        new Function1<Department, Integer>() {
            public Integer apply(Department department) {
                return department.deptno;
            }
        };

    public static final IntegerFunction1<Department> DEPT_DEPTNO_SELECTOR2 =
        new IntegerFunction1<Department>() {
            public int apply(Department department) {
                return department.deptno;
            }
        };

    public static final Function1<Object, Integer> ONE_SELECTOR =
        new Function1<Object, Integer>() {
            public Integer apply(Object employee) {
                return 1;
            }
        };

    private static final Function2<Object, Object, Integer> PAIR_SELECTOR =
        new Function2<Object, Object, Integer>() {
            public Integer apply(Object employee, Object v2) {
                return 1;
            }
        };

    public void testSelect() {
        List<String> names =
            Linq4j.asEnumerable(emps)
                .select(EMP_NAME_SELECTOR)
                .toList();
        assertEquals("[Fred, Bill, Eric, Janet]", names.toString());
    }

    public void testWhere() {
        List<String> names =
            Linq4j.asEnumerable(emps)
                .where(
                    new Predicate1<Employee>() {
                        public boolean apply(Employee employee) {
                            return employee.deptno < 15;
                        }
                    })
                .select(EMP_NAME_SELECTOR)
                .toList();
        assertEquals("[Fred, Eric, Janet]", names.toString());
    }

    public void testWhereIndexed() {
        // Returns every other employee.
        List<String> names =
            Linq4j.asEnumerable(emps)
                .where(
                    new Predicate2<Employee, Integer>() {
                        public boolean apply(Employee employee, Integer n) {
                            return n % 2 == 0;
                        }
                    })
                .select(EMP_NAME_SELECTOR)
                .toList();
        assertEquals("[Fred, Eric]", names.toString());
    }

    public void testSelectMany() {
        final List<String> nameSeqs =
            Linq4j.asEnumerable(depts)
                .selectMany(DEPT_EMPLOYEES_SELECTOR)
                .select(
                    new Function2<Employee, Integer, String>() {
                        public String apply(Employee v1, Integer v2) {
                            return "#" + v2 + ": " + v1.name;
                        }
                    })
                .toList();
        assertEquals(
            "[#0: Fred, #1: Eric, #2: Janet, #3: Bill]", nameSeqs.toString());
    }

    public void testCount() {
        final int count = Linq4j.asEnumerable(depts).count();
        assertEquals(3, count);
    }

    public void testCountPredicate() {
        final int count =
            Linq4j.asEnumerable(depts).count(
                new Predicate1<Department>() {
                    public boolean apply(Department v1) {
                        return v1.employees.size() > 0;
                    }
                });
        assertEquals(2, count);
    }

    public void testLongCount() {
        final long count = Linq4j.asEnumerable(depts).longCount();
        assertEquals(3, count);
    }

    public void testLongCountPredicate() {
        final long count =
            Linq4j.asEnumerable(depts).longCount(
                new Predicate1<Department>() {
                    public boolean apply(Department v1) {
                        return v1.employees.size() > 0;
                    }
                });
        assertEquals(2, count);
    }

    public void testAverageSelector() {
        assertEquals(
            20,
            Linq4j.asEnumerable(depts).average(DEPT_DEPTNO_SELECTOR2));
    }

    public void testMin() {
        assertEquals(
            10,
            (int) Linq4j.asEnumerable(depts).select(DEPT_DEPTNO_SELECTOR)
                .min());
    }

    public void testMinSelector() {
        assertEquals(
            10,
            (int) Linq4j.asEnumerable(depts).min(DEPT_DEPTNO_SELECTOR));
    }

    public void testMinSelector2() {
        assertEquals(
            10,
            Linq4j.asEnumerable(depts).min(DEPT_DEPTNO_SELECTOR2));
    }

    public void testMax() {
        assertEquals(
            30,
            (int) Linq4j.asEnumerable(depts).select(DEPT_DEPTNO_SELECTOR)
                .max());
    }

    public void testMaxSelector() {
        assertEquals(
            30,
            (int) Linq4j.asEnumerable(depts).max(DEPT_DEPTNO_SELECTOR));
    }

    public void testMaxSelector2() {
        assertEquals(
            30,
            Linq4j.asEnumerable(depts).max(DEPT_DEPTNO_SELECTOR2));
    }

    public void testAggregate() {
        assertEquals(
            "Sales,HR,Marketing",
            Linq4j.asEnumerable(depts)
                .select(DEPT_NAME_SELECTOR)
                .aggregate(
                    null,
                    new Function2<String, String, String>() {
                        public String apply(String v1, String v2) {
                            return v1 == null ? v2 : v1 + "," + v2;
                        }
                    }));
    }

    public void testToMap() {
        final Map<Integer, Employee> map =
            Linq4j.asEnumerable(emps)
                .toMap(EMP_EMPNO_SELECTOR);
        assertEquals(4, map.size());
        assertTrue(map.get(110).name.equals("Bill"));
    }

    public void testToMap2() {
        final Map<Integer, Integer> map =
            Linq4j.asEnumerable(emps)
                .toMap(EMP_EMPNO_SELECTOR, EMP_DEPTNO_SELECTOR);
        assertEquals(4, map.size());
        assertTrue(map.get(110) == 30);
    }

    public void testToLookup() {
        final Lookup<Integer, Employee> lookup =
            Linq4j.asEnumerable(emps).toLookup(
                EMP_DEPTNO_SELECTOR);
        int n = 0;
        for (Grouping<Integer, Employee> grouping : lookup) {
            ++n;
            switch (grouping.getKey()) {
            case 10:
                assertEquals(3, grouping.count());
                break;
            case 30:
                assertEquals(1, grouping.count());
                break;
            default:
                fail("unknown department number " + grouping);
            }
        }
        assertEquals(n, 2);
    }

    public void testToLookupSelector() {
        final Lookup<Integer, String> lookup =
            Linq4j.asEnumerable(emps).toLookup(
                EMP_DEPTNO_SELECTOR,
                EMP_NAME_SELECTOR);
        int n = 0;
        for (Grouping<Integer, String> grouping : lookup) {
            ++n;
            switch (grouping.getKey()) {
            case 10:
                assertEquals(3, grouping.count());
                assertTrue(grouping.contains("Fred"));
                assertTrue(grouping.contains("Eric"));
                assertTrue(grouping.contains("Janet"));
                assertFalse(grouping.contains("Bill"));
                break;
            case 30:
                assertEquals(1, grouping.count());
                assertTrue(grouping.contains("Bill"));
                assertFalse(grouping.contains("Fred"));
                break;
            default:
                fail("unknown department number " + grouping);
            }
        }
        assertEquals(n, 2);

        assertEquals(
            "[10:3, 30:1]",
            lookup.applyResultSelector(
                new Function2<Integer, Enumerable<String>, Object>() {
                    public Object apply(Integer v1, Enumerable<String> v2) {
                        return v1 + ":" + v2.count();
                    }
                }
            )
                .toList()
                .toString());
    }

    public void testToLookupSelectorComparer() {
        final Lookup<String, Employee> lookup =
            Linq4j.asEnumerable(emps).toLookup(
                EMP_NAME_SELECTOR,
                new EqualityComparer<String>() {
                    public boolean equal(String v1, String v2) {
                        return v1.length() == v2.length();
                    }

                    public int hashCode(String s) {
                        return s.length();
                    }
                });
        assertEquals(2, lookup.size());
        assertEquals("[Fred, Janet]", lookup.keySet().toString());

        StringBuilder buf = new StringBuilder();
        for (Grouping<String, Employee> grouping : lookup) {
            buf.append(grouping).append("\n");
        }
        assertEquals(
            "Fred: [Employee(name: Fred, deptno:10), Employee(name: Bill, deptno:30), Employee(name: Eric, deptno:10)]\n"
            + "Janet: [Employee(name: Janet, deptno:10)]\n",
            buf.toString());
    }

    /**
     * Tests the version of {@link ExtendedEnumerable#groupBy}
     * that uses an accumulator; does not build intermediate lists.
     */
    public void testGroupBy() {
        String s =
            Linq4j.asEnumerable(emps)
                .groupBy(
                    EMP_DEPTNO_SELECTOR,
                    new Function0<String>() {
                        public String apply() {
                            return null;
                        }
                    },
                    new Function2<String, Employee, String>() {
                        public String apply(String v1, Employee e0) {
                            return v1 == null ? e0.name : (v1 + "+" + e0.name);
                        }
                    },
                    new Function2<Integer, String, String>() {
                        public String apply(Integer v1, String v2) {
                            return v1 + ": " + v2;
                        }
                    })
                .toList()
                .toString();
        assertEquals(
            "[10: Fred+Eric+Janet, 30: Bill]",
            s);
    }

    /**
     * Tests the version of
     * {@link ExtendedEnumerable#aggregate}
     * that has a result selector. Note how similar it is to
     * {@link #testGroupBy()}.
     */
    public void testAggregate2() {
        String s =
            Linq4j.asEnumerable(emps)
                .aggregate(
                    new Function0<String>() {
                        public String apply() {
                            return null;
                        }
                    }.apply(),
                    new Function2<String, Employee, String>() {
                        public String apply(String v1, Employee e0) {
                            return v1 == null ? e0.name : (v1 + "+" + e0.name);
                        }
                    },
                    new Function1<String, String>() {
                        public String apply(String v2) {
                            return "<no key>: " + v2;
                        }
                    })
                .toString();
        assertEquals(
            "<no key>: Fred+Bill+Eric+Janet",
            s);
    }

    public void testCast() {
        final List<Number> numbers = Arrays.asList((Number) 2, null, 3.14, 5);
        final Enumerator<Integer> enumerator =
            Linq4j.asEnumerable(numbers)
                .cast(Integer.class)
                .enumerator();
        checkCast(enumerator);
    }

    public void testIterableCast() {
        final List<Number> numbers = Arrays.asList((Number) 2, null, 3.14, 5);
        final Enumerator<Integer> enumerator =
            Linq4j.cast(numbers, Integer.class)
                .enumerator();
        checkCast(enumerator);
    }

    private void checkCast(Enumerator<Integer> enumerator) {
        assertTrue(enumerator.moveNext());
        assertEquals(Integer.valueOf(2), enumerator.current());
        assertTrue(enumerator.moveNext());
        assertNull(enumerator.current());
        assertTrue(enumerator.moveNext());
        try {
            Object x = enumerator.current();
            fail("expected error, got " + x);
        } catch (ClassCastException e) {
            // good
        }
        assertTrue(enumerator.moveNext());
        assertEquals(Integer.valueOf(5), enumerator.current());
        assertFalse(enumerator.moveNext());
        enumerator.reset();
        assertTrue(enumerator.moveNext());
        assertEquals(Integer.valueOf(2), enumerator.current());
    }

    public void testOfType() {
        final List<Number> numbers = Arrays.asList((Number) 2, null, 3.14, 5);
        final Enumerator<Integer> enumerator =
            Linq4j.asEnumerable(numbers)
                .ofType(Integer.class)
                .enumerator();
        checkIterable(enumerator);
    }

    public void testIterableOfType() {
        final List<Number> numbers = Arrays.asList((Number) 2, null, 3.14, 5);
        final Enumerator<Integer> enumerator =
            Linq4j.ofType(numbers, Integer.class)
                .enumerator();
        checkIterable(enumerator);
    }

    private void checkIterable(Enumerator<Integer> enumerator) {
        assertTrue(enumerator.moveNext());
        assertEquals(Integer.valueOf(2), enumerator.current());
        assertTrue(enumerator.moveNext());
        assertNull(enumerator.current());
        assertTrue(enumerator.moveNext());
        assertEquals(Integer.valueOf(5), enumerator.current());
        assertFalse(enumerator.moveNext());
        enumerator.reset();
        assertTrue(enumerator.moveNext());
        assertEquals(Integer.valueOf(2), enumerator.current());
    }

    public void testConcat() {
        assertEquals(
            5,
            Linq4j.asEnumerable(emps)
                .concat(Linq4j.asEnumerable(badEmps))
                .count());
    }

    public void testUnion() {
        assertEquals(
            5,
            Linq4j.asEnumerable(emps)
                .union(Linq4j.asEnumerable(badEmps))
                .union(Linq4j.asEnumerable(emps))
                .count());
    }

    public void testIntersect() {
        final Employee[] emps2 = {
            new Employee(150, "Theodore", 10),
            emps[3],
        };
        assertEquals(
            1,
            Linq4j.asEnumerable(emps)
                .intersect(Linq4j.asEnumerable(emps2))
                .count());
    }

    public void testExcept() {
        final Employee[] emps2 = {
            new Employee(150, "Theodore", 10),
            emps[3],
        };
        assertEquals(
            3,
            Linq4j.asEnumerable(emps)
                .except(Linq4j.asEnumerable(emps2))
                .count());
    }

    public void testGroupJoin() {
        // Note #1: Group join is a "left join": "bad employees" are filtered
        //   out, but empty departments are not.
        // Note #2: Order of departments is preserved.
        String s =
            Linq4j.asEnumerable(depts)
                .groupJoin(
                    Linq4j.asEnumerable(emps)
                        .concat(Linq4j.asEnumerable(badEmps)),
                    DEPT_DEPTNO_SELECTOR,
                    EMP_DEPTNO_SELECTOR,
                    new Function2<Department, Enumerable<Employee>, String>() {
                        public String apply(
                            Department v1, Enumerable<Employee> v2)
                        {
                            final StringBuilder buf = new StringBuilder("[");
                            int n = 0;
                            for (Employee employee : v2) {
                                if (n++ > 0) {
                                    buf.append(", ");
                                }
                                buf.append(employee.name);
                            }
                            return buf.append("] work(s) in ").append(v1.name)
                                .toString();
                        }
                    }
                ).toList()
                .toString();
        assertEquals(
            "[[Fred, Eric, Janet] work(s) in Sales, "
            + "[] work(s) in HR, "
            + "[Bill] work(s) in Marketing]",
            s);
    }

    public void testJoin() {
        // Note #1: Inner on both sides. Employees with bad departments,
        //   and departments with no employees are eliminated.
        // Note #2: Order of employees is preserved.
        String s =
            Linq4j.asEnumerable(emps)
                .concat(Linq4j.asEnumerable(badEmps))
                .join(
                    Linq4j.asEnumerable(depts),
                    EMP_DEPTNO_SELECTOR,
                    DEPT_DEPTNO_SELECTOR,
                    new Function2<Employee, Department, String>() {
                        public String apply(Employee v1, Department v2) {
                            return v1.name + " works in " + v2.name;
                        }
                    })
                .toList()
                .toString();
        assertEquals(
            "[Fred works in Sales, "
            + "Eric works in Sales, "
            + "Janet works in Sales, "
            + "Bill works in Marketing]",
            s);
    }

    public void testJoinCartesianProduct() {
        int n =
            Linq4j.asEnumerable(emps)
                .<Department, Integer, Integer>join(
                    Linq4j.asEnumerable(depts),
                    (Function1) ONE_SELECTOR,
                    (Function1) ONE_SELECTOR,
                    (Function2) PAIR_SELECTOR)
            .count();
        assertEquals(12, n); // 4 employees times 3 departments
    }

    @SuppressWarnings("unchecked")
    public void testCartesianProductEnumerator() {
        final Enumerable<String> abc =
            Linq4j.asEnumerable(Arrays.asList("a", "b", "c"));
        final Enumerable<String> xy =
            Linq4j.asEnumerable(Arrays.asList("x", "y"));

        final Enumerator<List<String>> product0 =
            Linq4j.product(
                Arrays.asList(Linq4j.<String>emptyEnumerator()));
        assertFalse(product0.moveNext());

        final Enumerator<List<String>> productFullEmpty =
            Linq4j.product(
                Arrays.asList(
                    abc.enumerator(), Linq4j.<String>emptyEnumerator()));
        assertFalse(productFullEmpty.moveNext());

        final Enumerator<List<String>> productEmptyFull =
            Linq4j.product(
                Arrays.asList(
                    abc.enumerator(), Linq4j.<String>emptyEnumerator()));
        assertFalse(productEmptyFull.moveNext());

        final Enumerator<List<String>> productAbcXy =
            Linq4j.product(
                Arrays.asList(abc.enumerator(), xy.enumerator()));
        assertTrue(productAbcXy.moveNext());
        assertEquals(Arrays.asList("a", "x"), productAbcXy.current());
        assertTrue(productAbcXy.moveNext());
        assertEquals(Arrays.asList("a", "y"), productAbcXy.current());
        assertTrue(productAbcXy.moveNext());
        assertEquals(Arrays.asList("b", "x"), productAbcXy.current());
        assertTrue(productAbcXy.moveNext());
        assertTrue(productAbcXy.moveNext());
        assertTrue(productAbcXy.moveNext());
        assertFalse(productAbcXy.moveNext());
    }

    public void testAsQueryable() {
        // "count" is an Enumerable method.
        final int n =
        Linq4j.asEnumerable(emps)
            .asQueryable()
            .count();
        assertEquals(4, n);

        // "where" is a Queryable method
        // first, use a lambda
        ParameterExpression parameter =
            Expressions.parameter(Employee.class);
        final Queryable<Employee> nh =
            Linq4j.asEnumerable(emps)
                .asQueryable()
                .where(
                    Expressions.lambda(
                        Predicate1.class,
                        Expressions.equal(
                            Expressions.field(
                                parameter,
                                Employee.class,
                                "deptno"),
                            Expressions.constant(10)),
                        parameter));
        assertEquals(3, nh.count());

        // second, use an expression
        final Queryable<Employee> nh2 =
            Linq4j.asEnumerable(emps)
                .asQueryable()
                .where(
                    Expressions.lambda(
                        new Predicate1<Employee>() {
                            public boolean apply(Employee v1) {
                                return v1.deptno == 10;
                            }
                        }
                    ));
        assertEquals(3, nh2.count());

        // use lambda, this time call whereN
        ParameterExpression parameterE =
            Expressions.parameter(Employee.class);
        ParameterExpression parameterN =
            Expressions.parameter(Integer.TYPE);
        final Queryable<Employee> nh3 =
            Linq4j.asEnumerable(emps)
                .asQueryable()
                .whereN(
                    Expressions.lambda(
                        Predicate2.class,
                        Expressions.andAlso(
                            Expressions.equal(
                                Expressions.field(
                                    parameterE,
                                    Employee.class,
                                    "deptno"),
                                Expressions.constant(10)),
                            Expressions.lessThan(
                                parameterN,
                                Expressions.constant(3))),
                        parameterE,
                        parameterN));
        assertEquals(2, nh3.count());
    }

    public void testTake_enumerable() {
        final Enumerable<Department> enumerableDepts =
            Linq4j.asEnumerable(depts);
        final List<Department> enumerableDeptsResult =
            EnumerableDefaults.take(enumerableDepts, 2).toList();
        assertEquals(2, enumerableDeptsResult.size());
        assertEquals(depts[0], enumerableDeptsResult.get(0));
        assertEquals(depts[1], enumerableDeptsResult.get(1));
    }

    public void testTake_queryable() {
        final Queryable<Department> querableDepts =
            Linq4j.asEnumerable(depts).asQueryable();
        final List<Department> queryableResult =
            QueryableDefaults.take(querableDepts, 2).toList();

        assertEquals(2, queryableResult.size());
        assertEquals(depts[0], queryableResult.get(0));
        assertEquals(depts[1], queryableResult.get(1));
    }

    public void testTake_enumerable_zero_or_negative_size() {
        assertEquals(
            0,
            EnumerableDefaults.take(Linq4j.asEnumerable(depts), 0)
                .toList().size());
        assertEquals(
            0,
            EnumerableDefaults.take(Linq4j.asEnumerable(depts), -2)
                .toList().size());
    }

    public void testTake_queryable_zero_or_negative_size() {
        assertEquals(
            0,
            QueryableDefaults.take(Linq4j.asEnumerable(depts).asQueryable(), 0)
                .toList().size());
        assertEquals(
            0,
            QueryableDefaults.take(Linq4j.asEnumerable(depts).asQueryable(), -2)
                .toList().size());
    }

    public void testTake_enumerable_greater_than_length() {
        final Enumerable<Department> enumerableDepts =
            Linq4j.asEnumerable(depts);
        final List<Department> depList =
            EnumerableDefaults.take(enumerableDepts, 5).toList();
        assertEquals(3, depList.size());
        assertEquals(depts[0], depList.get(0));
        assertEquals(depts[1], depList.get(1));
        assertEquals(depts[2], depList.get(2));
    }

    public void testTake_queryable_greater_than_length() {
        final Enumerable<Department> enumerableDepts =
            Linq4j.asEnumerable(depts);
        final List<Department> depList =
            EnumerableDefaults.take(enumerableDepts, 5).toList();
        assertEquals(3, depList.size());
        assertEquals(depts[0], depList.get(0));
        assertEquals(depts[1], depList.get(1));
        assertEquals(depts[2], depList.get(2));
    }

    public void testTakeWhile_enumerable_predicate() {
        final Enumerable<Department> enumerableDepts =
            Linq4j.asEnumerable(depts);
        final List<Department> deptList =
            EnumerableDefaults.takeWhile(
                enumerableDepts, new Predicate1<Department>() {
                public boolean apply(Department v1) {
                    return v1.name.contains("e");
                }
            }).toList();

        // Only one department:
        // 0: Sales --> true
        // 1: HR --> false
        // 2: Marketing --> never get to it (we stop after false)
        assertEquals(1, deptList.size());
        assertEquals(depts[0], deptList.get(0));
    }

    public void testTakeWhile_enumerable_function() {
        final Enumerable<Department> enumerableDepts =
            Linq4j.asEnumerable(depts);
        final List<Department> deptList =
            EnumerableDefaults.takeWhile(
                enumerableDepts, new Predicate2<Department, Integer>() {
                int index = 0;

                public boolean apply(Department v1, Integer v2) {
                    // Make sure we're passed the correct indices
                    assertEquals(
                        "Invalid index passed to function", index++, (int) v2);
                    return 20 != v1.deptno;
                }
            }).toList();

        assertEquals(1, deptList.size());
        assertEquals(depts[0], deptList.get(0));
    }

    public void testTakeWhile_queryable_functionexpression_predicate() {
        final Queryable<Department> queryableDepts =
            Linq4j.asEnumerable(depts).asQueryable();
        Predicate1<Department> predicate = new Predicate1<Department>() {
            public boolean apply(Department v1) {
                return "HR".equals(v1.name);
            }
        };
        List<Department> deptList =
            QueryableDefaults.takeWhile(
                queryableDepts, Expressions.lambda(predicate))
            .toList();

        assertEquals(0, deptList.size());

        predicate = new Predicate1<Department>() {
            public boolean apply(Department v1) {
                return "Sales".equals(v1.name);
            }
        };
        deptList =
            QueryableDefaults.takeWhile(
                queryableDepts, Expressions.lambda(predicate))
            .toList();

        assertEquals(1, deptList.size());
        assertEquals(depts[0], deptList.get(0));
    }

    public void testTakeWhileN() {
        final Queryable<Department> queryableDepts =
            Linq4j.asEnumerable(depts).asQueryable();
        Predicate2<Department, Integer> function2 =
            new Predicate2<Department, Integer>() {
                int index = 0;
                public boolean apply(Department v1, Integer v2) {
                    // Make sure we're passed the correct indices
                    assertEquals(
                        "Invalid index passed to function", index++, (int) v2);
                    return v2 < 2;
                }
            };

        final List<Department> deptList =
            QueryableDefaults.takeWhileN(
                queryableDepts, Expressions.lambda(function2))
            .toList();

        assertEquals(2, deptList.size());
        assertEquals(depts[0], deptList.get(0));
        assertEquals(depts[1], deptList.get(1));
    }

    public void testTakeWhileN_no_match() {
        final Queryable<Department> queryableDepts =
            Linq4j.asEnumerable(depts).asQueryable();
        Predicate2<Department, Integer> function2 = Functions.falsePredicate2();
        final List<Department> deptList =
            QueryableDefaults.takeWhileN(
                queryableDepts,
                Expressions.lambda(function2))
            .toList();

        assertEquals(0, deptList.size());
    }

    public void testSkip() {
        assertEquals(2, Linq4j.asEnumerable(depts).skip(1).count());
        assertEquals(
            2,
            Linq4j.asEnumerable(depts).skipWhile(
                new Predicate1<Department>() {
                    public boolean apply(Department v1) {
                        return v1.name.equals("Sales");
                    }
                }).count());
        assertEquals(
            3,
            Linq4j.asEnumerable(depts).skipWhile(
                new Predicate1<Department>() {
                    public boolean apply(Department v1) {
                        return !v1.name.equals("Sales");
                    }
                }).count());
        assertEquals(
            1,
            Linq4j.asEnumerable(depts).skipWhile(
                new Predicate2<Department, Integer>() {
                    public boolean apply(Department v1, Integer v2) {
                        return v1.name.equals("Sales")
                            || v2 == 1;
                    }
                }).count());

        assertEquals(
            2, Linq4j.asEnumerable(depts).asQueryable().skip(1).count());
        assertEquals(
            1,
            Linq4j.asEnumerable(depts).asQueryable().skipWhileN(
                Expressions.<Predicate2<Department, Integer>>lambda(
                    new Predicate2<Department, Integer>() {
                        public boolean apply(Department v1, Integer v2) {
                            return v1.name.equals("Sales")
                                   || v2 == 1;
                        }
                    })).count());
    }

    public void testOrderBy() {
        // Note: sort is stable. Records occur Fred, Eric, Janet in input.
        assertEquals(
            "[Employee(name: Fred, deptno:10),"
            + " Employee(name: Eric, deptno:10),"
            + " Employee(name: Janet, deptno:10),"
            + " Employee(name: Bill, deptno:30)]",
            Linq4j.asEnumerable(emps).orderBy(EMP_DEPTNO_SELECTOR)
                .toList().toString());
    }

    public void testOrderByComparator() {
        assertEquals(
            "[Employee(name: Bill, deptno:30),"
            + " Employee(name: Eric, deptno:10),"
            + " Employee(name: Fred, deptno:10),"
            + " Employee(name: Janet, deptno:10)]",
            Linq4j.asEnumerable(emps)
                .orderBy(EMP_NAME_SELECTOR)
                .orderBy(
                    EMP_DEPTNO_SELECTOR, Collections.<Integer>reverseOrder())
                .toList().toString());
    }

    public void testOrderByInSeries() {
        // OrderBy in series works because sort is stable.
        assertEquals(
            "[Employee(name: Eric, deptno:10),"
            + " Employee(name: Fred, deptno:10),"
            + " Employee(name: Janet, deptno:10),"
            + " Employee(name: Bill, deptno:30)]",
            Linq4j.asEnumerable(emps)
                .orderBy(EMP_NAME_SELECTOR)
                .orderBy(EMP_DEPTNO_SELECTOR)
                .toList().toString());
    }

    public void testOrderByDescending() {
        assertEquals(
            "[Employee(name: Janet, deptno:10),"
            + " Employee(name: Fred, deptno:10),"
            + " Employee(name: Eric, deptno:10),"
            + " Employee(name: Bill, deptno:30)]",
            Linq4j.asEnumerable(emps)
                .orderByDescending(EMP_NAME_SELECTOR)
                .toList().toString());
    }

    public void testReverse() {
        assertEquals(
            "[Employee(name: Janet, deptno:10),"
            + " Employee(name: Eric, deptno:10),"
            + " Employee(name: Bill, deptno:30),"
            + " Employee(name: Fred, deptno:10)]",
            Linq4j.asEnumerable(emps)
                .reverse()
                .toList()
                .toString());
    }

    public static class Employee {
        public final int empno;
        public final String name;
        public final int deptno;

        public Employee(int empno, String name, int deptno) {
            this.empno = empno;
            this.name = name;
            this.deptno = deptno;
        }

        public String toString() {
            return "Employee(name: " + name + ", deptno:" + deptno + ")";
        }
    }

    public static class Department {
        public final String name;
        public final int deptno;
        public final List<Employee> employees;

        public Department(String name, int deptno, List<Employee> employees) {
            this.name = name;
            this.deptno = deptno;
            this.employees = employees;
        }

        public String toString() {
            return "Department(name: " + name
                   + ", deptno:" + deptno
                   + ", employees: " + employees
                   + ")";
        }
    }

    // Cedric works in a non-existent department.
    public static final Employee[] badEmps = {
        new Employee(140, "Cedric", 40),
    };

    public static final Employee[] emps = {
        new Employee(100, "Fred", 10),
        new Employee(110, "Bill", 30),
        new Employee(120, "Eric", 10),
        new Employee(130, "Janet", 10),
    };

    public static final Department[] depts = {
        new Department("Sales", 10, Arrays.asList(emps[0], emps[2], emps[3])),
        new Department("HR", 20, Collections.<Employee>emptyList()),
        new Department("Marketing", 30, Arrays.asList(emps[1])),
    };
}

// End Linq4jTest.java

