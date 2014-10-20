package de.metanome.algorithms;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import de.metanome.algorithms.config.Config;
import de.metanome.algorithms.mocks.MetanomeMock;
import de.uni_potsdam.hpi.utils.CollectionUtils;

public class MetanomeTestRunner {
  public static void run(String[] args) {
    if (args.length != 1) {
      return;
    }
    Path dir = Paths.get(args[0]);
    Path result = Paths.get(args[0], "result");
    System.out.println("Executing algorithm on files in path" + dir);
    try {
      Files.createDirectories(result);
    } catch (IOException e) {
      e.printStackTrace();
    }
    try (DirectoryStream<Path> fileIterator = Files.newDirectoryStream(dir) ) {
      for (Path file : fileIterator) {
        run(file, result);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void run(Path file, Path result) {
    long time = System.currentTimeMillis();

    MetanomeMock.executeDCUCC(file, result);

//    System.out.println(
//        "(" + runLabel + ") Runtime " + algorithmName + ": " + (System.currentTimeMillis() - time)
//        + " ms");
  }
}
