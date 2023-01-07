package com.asvoip.rewrite.sql;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.SourceSpecs;
import org.openrewrite.text.PlainTextParser;

class IdempotentCreateTableSqlTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new IdempotentCreateTableSql())
            .parser(PlainTextParser.builder());
    }

    @Test
    void rewriteIdempotentCreateTableSql1() {
        rewriteRun(
            SourceSpecs.text(
                "CREATE TABLE hobbies_r (name		text, person 		text);",
                "CREATE TABLE IF NOT EXISTS hobbies_r (name		text, person 		text);"
            )
        );
    }

}