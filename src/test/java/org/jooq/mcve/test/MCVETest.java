/*
 * This work is dual-licensed
 * - under the Apache Software License 2.0 (the "ASL")
 * - under the jOOQ License and Maintenance Agreement (the "jOOQ License")
 * =============================================================================
 * You may choose which license applies to you:
 *
 * - If you're using this work with Open Source databases, you may choose
 *   either ASL or jOOQ License.
 * - If you're using this work with at least one commercial database, you must
 *   choose jOOQ License
 *
 * For more information, please visit http://www.jooq.org/licenses
 *
 * Apache Software License 2.0:
 * -----------------------------------------------------------------------------
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * jOOQ License and Maintenance Agreement:
 * -----------------------------------------------------------------------------
 * Data Geekery grants the Customer the non-exclusive, timely limited and
 * non-transferable license to install and use the Software under the terms of
 * the jOOQ License and Maintenance Agreement.
 *
 * This library is distributed with a LIMITED WARRANTY. See the jOOQ License
 * and Maintenance Agreement for more details: http://www.jooq.org/licensing
 */
package org.jooq.mcve.test;

import org.hamcrest.CoreMatchers;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.mcve.tables.CompletedTask;
import org.jooq.mcve.tables.Task;
import org.jooq.mcve.tables.records.CompletedTaskRecord;
import org.jooq.mcve.tables.records.TaskRecord;
import org.jooq.mcve.tables.records.TestRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import static org.jooq.mcve.Tables.*;
import static org.junit.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;

public class MCVETest {

    Connection connection;
    DSLContext ctx;

    @Before
    public void setup() throws Exception {
        connection = DriverManager.getConnection("jdbc:h2:~/mcve", "sa", "");
        ctx = DSL.using(connection);
    }

    @After
    public void after() throws Exception {
        ctx = null;
        connection.close();
        connection = null;
    }

    @Test
    public void mcveTest() {
        TestRecord result =
        ctx.insertInto(TEST)
           .columns(TEST.VALUE)
           .values(42)
           .returning(TEST.ID)
           .fetchOne();

        result.refresh();
        assertEquals(42, (int) result.getValue());
        ctx.batchStore(
                new TaskRecord(100, 1),
                new TaskRecord(100, 2),
                new TaskRecord(100, 3),
                new TaskRecord(200, 1),
                new TaskRecord(200, 2),
                new TaskRecord(200, 3),
                new CompletedTaskRecord(100, 1),
                new CompletedTaskRecord(200, 1),
                new CompletedTaskRecord(200, 2)
        );

        Task t1 = TASK.as("t1");
        CompletedTask t2 = COMPLETED_TASK.as("t2");

        int groupId = 1;
        List<Integer> todoTasks = ctx.select(t1.MEMBER_ID)
                .from(
                        t1.leftAntiJoin(t2)
                                .on(t1.MEMBER_ID.eq(t2.MEMBER_ID))
                )
                .where(t1.GROUP_ID.eq(groupId))
                .and(t2.GROUP_ID.eq(groupId))
                .fetch(t1.MEMBER_ID);

        assertThat(todoTasks, CoreMatchers.hasItems(2, 3));
    }
}
