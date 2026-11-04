package io.github.alecredmond.internal.fileio;

import io.github.alecredmond.export.network.serialized.SerializedBayesianNetwork;
import java.io.*;
import javax.swing.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NetworkFileIO {
  private final NetworkFileIoProperties properties;

  public NetworkFileIO() {
    this.properties = new NetworkFileIoProperties();
  }

  public boolean saveNetwork(SerializedBayesianNetwork sbn, String filePath) {
    return saveNetwork(sbn, new File(filePath));
  }

  public boolean saveNetwork(SerializedBayesianNetwork sbn, File selectedFile) {
    selectedFile = checkAddExtension(selectedFile);
    try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(selectedFile))) {
      out.writeObject(sbn);
      log.info("Network saved to {}", selectedFile.getPath());
    } catch (IOException e) {
      log.error("Exception attempting to save network to file: {}", e.getMessage());
      return false;
    }
    return true;
  }

  private File checkAddExtension(File file) {
    String fileName = file.getAbsolutePath();
    String extension = properties.getExtension();
    return fileName.endsWith(extension) ? file : new File(fileName + extension);
  }

  public SerializedBayesianNetwork loadNetwork(String filePath) {
    return loadNetwork(new File(filePath));
  }

  public SerializedBayesianNetwork loadNetwork(File selectedFile) {
    try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(selectedFile))) {
      return (SerializedBayesianNetwork) in.readObject();
    } catch (IOException e) {
      log.error("IOException attempting to load network {}", e.getMessage());
    } catch (ClassNotFoundException e) {
      log.error(
          "Selected file was not a recognised instance of a Bayesian Network! {}", e.getMessage());
    }
    return null;
  }
}
