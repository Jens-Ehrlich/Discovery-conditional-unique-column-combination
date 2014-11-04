package de.metanome.algorithms;

import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class MainTest {

  @Test
  @Ignore
  public void testMain() throws UnsupportedEncodingException {
    String
        pathToInputFile =
        URLDecoder.decode(
            Thread.currentThread().getContextClassLoader().getResource("bridges.csv").getPath(),
            "utf-8");
    System.out.println(pathToInputFile);

    File file = new File(pathToInputFile);
    System.out.println(file.getParent());
    String[] args = new String[1];
    args[0] = file.getParent();
    Main.main(args);
  }
}