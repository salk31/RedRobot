/*
 * Diamond User Administration System
 * Copyright © 2019 Diamond Light Source Ltd
 */

package com.redspr.redrobot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.exec.OS;


/**
 *
 */
public class BinaryUtil {
  private static final Map<String, File> resourceToFile = new HashMap<>();

  public static String getPathForDriver(String name) {
    String resourcePath = "/" + name + plaformExt();

    File f = resourceToFile.get(resourcePath);

    try {
      f = File.createTempFile(name, "tmp");
      f.setExecutable(true);
      f.deleteOnExit();
      InputStream stream = BinaryUtil.class.getResourceAsStream("/" + name + plaformExt());

      int readBytes;
      byte[] buffer = new byte[4096];
      FileOutputStream resStreamOut = new FileOutputStream(f);
      while ((readBytes = stream.read(buffer)) > 0) {
          resStreamOut.write(buffer, 0, readBytes);
      }
      resStreamOut.close();

      resourceToFile.put(resourcePath, f);
    } catch (Exception ex) {
      throw new RuntimeException("Failed to copy " + name, ex);
    }

    return f.getPath();
  }

  public static String plaformExt() {
      if (OS.isFamilyWindows()) {
          return ".exe";
      }
      return "";
  }
}
