package io.github.tivrfoa.mapresultset;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParseQueryTest {

    public void testCleanQuery() {
        String sql = """
        
            select *
            from   client
            
        """;
        assertEquals("SELECT * FROM client", ParseQuery.cleanQuery(sql));

        sql = """
        select *
        FRoM client aS c
        where id > 10
        ordEr By name
        """;
        assertEquals("SELECT * FROM client AS c WHERE id > 10 ORDER BY name", ParseQuery.cleanQuery(sql));
    }
}
