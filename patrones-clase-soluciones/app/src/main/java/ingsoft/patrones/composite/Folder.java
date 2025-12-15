package ingsoft.patrones.composite;

import java.util.ArrayList;
import java.util.List;

public class Folder implements FileSystemComponent {
  private String name;
  private List<FileSystemComponent> components;

  public Folder(String name) {
    this.name = name;
    this.components = new ArrayList<>();
  }

  public void add(FileSystemComponent component) {
    components.add(component);
  }

  public void remove(FileSystemComponent component) {
    components.remove(component);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public long getSize() {
    long totalSize = 0;
    for (FileSystemComponent component : components) {
      totalSize += component.getSize();
    }
    return totalSize;
  }
}
