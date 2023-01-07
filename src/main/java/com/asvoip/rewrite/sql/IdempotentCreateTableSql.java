package com.asvoip.rewrite.sql;

import org.openrewrite.*;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.text.PlainText;

public class IdempotentCreateTableSql extends Recipe {

    private static final String IF_NOT_EXISTS_SUBSTRING = "IF NOT EXISTS";

    //https://github.com/antlr/grammars-v4/blob/master/sql/postgresql/examples/create_table.sql
    //https://github.com/antlr/grammars-v4/blob/master/sql/postgresql/pom.xml
    @Option(displayName = "Database",
            description = "Kind of database.",
            example = "postgresql")
    String database;

    @Override
    public String getDisplayName() {
        return "Idempotent 'CREATE TABLE IF NOT EXISTS table_name...' SQL";
    }

    @Override
    public String getDescription() {
        return "Rewrite 'CREATE TABLE table_name...' SQL as 'CREATE TABLE IF NOT EXISTS table_name...'.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new TreeVisitor<Tree, ExecutionContext>() {
            @Nullable
            @Override
            public Tree visit(@Nullable Tree tree, ExecutionContext executionContext) {
                if (tree instanceof PlainText) {
                    PlainText sqlPlainText = (PlainText) tree;
                    String sql = sqlPlainText.getText();
                    String[] sqlTokens = sql.split(" ");
                    if (sqlTokens.length >= 3
                            && sqlTokens[0].trim().toUpperCase().equals("CREATE")
                            && sqlTokens[1].trim().toUpperCase().equals("TABLE")
                            && !sql.toUpperCase().contains(IF_NOT_EXISTS_SUBSTRING)) {
                        StringBuilder rewriteSql = new StringBuilder();
                        for (int i = 0; i < sqlTokens.length; i++) {
                            String token = sqlTokens[i];
                            if (i == 2) {
                                rewriteSql.append(IF_NOT_EXISTS_SUBSTRING).append(" ");
                            }
                            rewriteSql.append(token);
                            if (i < sqlTokens.length - 1) {
                                rewriteSql.append(" ");
                            }
                        }
                        PlainText rewriteSqlPlainText = new PlainText(
                                sqlPlainText.getId(),
                                sqlPlainText.getSourcePath(),
                                sqlPlainText.getMarkers(),
                                sqlPlainText.getCharsetName(),
                                sqlPlainText.isCharsetBomMarked(),
                                sqlPlainText.getFileAttributes(),
                                sqlPlainText.getChecksum(),
                                rewriteSql.toString()
                        );
                        return rewriteSqlPlainText;
                    } else {
                        return sqlPlainText;
                    }
                }
                return super.visit(tree, executionContext);
            }
        };
    }

}
