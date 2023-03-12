package com.asvoip.rewrite.dockerfile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.text.PlainText;

public class UpgradeBaseImage extends Recipe {

  private static final String BASE_IMAGES_DELIMITER = ",";

  @Option(displayName = "Current base images",
      description = "Current base images.",
      example = "sdk:11-slim")
  String currentBaseImages;

  /**
   * New base image.
   *
   * See <link>https://hub.docker.com/r/bellsoft/liberica-openjdk-alpine-musl</link>.
   */
  @Option(displayName = "New base image",
      description = "New base image.",
      example = "sdk:17-slim")
  String newBaseImage;

  @Override
  public String getDisplayName() {
    return "Upgrade base image";
  }

  @Override
  public String getDescription() {
    return "Upgrade base image.";
  }

  @Override
  public TreeVisitor<?, ExecutionContext> getVisitor() {
    return new TreeVisitor<Tree, ExecutionContext>() {
      @Nullable
      @Override
      public Tree visit(@Nullable Tree tree, ExecutionContext executionContext) {
        if (tree instanceof PlainText) {
          PlainText plainText = (PlainText) tree;
          String text = plainText.getText();
          Set<String> currentBaseImagesSet = Arrays.stream(
              currentBaseImages.split(BASE_IMAGES_DELIMITER)
          ).collect(Collectors.toSet());

          List<String> lines = text.lines().collect(Collectors.toList());

          if (!currentBaseImagesSet.isEmpty() && !lines.isEmpty()) {
            boolean isUpgrated = replaceBaseImage(currentBaseImagesSet, lines);

            if (isUpgrated) {
              return new PlainText(
                  plainText.getId(),
                  plainText.getSourcePath(),
                  plainText.getMarkers(),
                  plainText.getCharsetName(),
                  plainText.isCharsetBomMarked(),
                  plainText.getFileAttributes(),
                  plainText.getChecksum(),
                  String.join(System.lineSeparator(), lines)
              );
            } else {
              return plainText;
            }
          } else {
            return plainText;
          }
        }
        return super.visit(tree, executionContext);
      }
    };
  }

  private boolean replaceBaseImage(Set<String> currentBaseImagesSet, List<String> lines) {
    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i);
      for (String currentBaseImage : currentBaseImagesSet) {
        if (line.contains(currentBaseImage)) {
          String newLine = line.replace(currentBaseImage, newBaseImage);
          lines.set(i, newLine);
          return true;
        }
      }
    }
    return false;
  }

}
