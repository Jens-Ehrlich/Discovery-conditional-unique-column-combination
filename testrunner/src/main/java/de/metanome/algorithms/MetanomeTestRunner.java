package de.metanome.algorithms;

import de.metanome.algorithms.mocks.MetanomeMock;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MetanomeTestRunner {
  public static void run(String[] args) {
    if (args.length != 1) {
      return;
    }
    Path dir = Paths.get(args[0]);
    System.out.println("Executing algorithm on files in path " + dir);

    try (DirectoryStream<Path> fileIterator = Files.newDirectoryStream(dir) ) {
      for (Path file : fileIterator) {
        run(file, dir);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void run(Path file, Path dir) {
    long time = System.currentTimeMillis();
    Path result = Paths.get(dir.toString(), "result", file.getFileName().toString());
    MetanomeMock.executeDCUCC(file, result);

//    System.out.println(
//        "(" + runLabel + ") Runtime " + algorithmName + ": " + (System.currentTimeMillis() - time)
//        + " ms");
  }
}
