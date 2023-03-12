package com.asvoip.rewrite.dockerfile;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.SourceSpecs;
import org.openrewrite.text.PlainTextParser;

class UpgradeBaseImageTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        UpgradeBaseImage recipe = new UpgradeBaseImage();
        recipe.currentBaseImages = "adoptopenjdk/openjdk11,openjdk:8-jdk-alpine";
        recipe.newBaseImage = "bellsoft/liberica-openjdk-alpine-musl:17";
        spec.recipe(recipe).parser(PlainTextParser.builder());
    }

    @Test
    void upgradeBaseImageWithUpgrade() {
        rewriteRun(
            SourceSpecs.text(
                """
                    # from base image JDK
                    FROM openjdk:8-jdk-alpine
                    
                    ARG JAR_FILE=target/find-links.jar
                    ARG JAR_LIB_FILE=target/lib/
                    
                    # cd /usr/local/runme
                    WORKDIR /usr/local/runme
                    
                    # copy target/find-links.jar /usr/local/runme/app.jar
                    COPY ${JAR_FILE} app.jar
                    
                    # copy project dependencies
                    # cp -rf target/lib/  /usr/local/runme/lib
                    ADD ${JAR_LIB_FILE} lib/
                    
                    # java -jar /usr/local/runme/app.jar
                    ENTRYPOINT ["java","-jar","app.jar"]
                    """,
                """
                    # from base image JDK
                    FROM bellsoft/liberica-openjdk-alpine-musl:17
                    
                    ARG JAR_FILE=target/find-links.jar
                    ARG JAR_LIB_FILE=target/lib/
                    
                    # cd /usr/local/runme
                    WORKDIR /usr/local/runme
                    
                    # copy target/find-links.jar /usr/local/runme/app.jar
                    COPY ${JAR_FILE} app.jar
                    
                    # copy project dependencies
                    # cp -rf target/lib/  /usr/local/runme/lib
                    ADD ${JAR_LIB_FILE} lib/
                    
                    # java -jar /usr/local/runme/app.jar
                    ENTRYPOINT ["java","-jar","app.jar"]
                    """
            )
        );
    }

    @Test
    void upgradeBaseImageWithoutUpgrade() {
        rewriteRun(
            SourceSpecs.text(
                """
                    # from base image node
                    FROM node:8.11-slim
                    
                    # command executable and version
                    CMD ["node","-v"]
                    CMD ["node"]                    
                    """
            )
        );
    }

}