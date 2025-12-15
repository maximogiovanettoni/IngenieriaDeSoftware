package ingsoft.patrones.composite;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FileSystemTest {
  @Test
  public void testFileSize() {
    File file = new File("document.txt", 100);
    assertEquals("document.txt", file.getName());
    assertEquals(100, file.getSize());
  }

  @Test
  public void testFolderWithFiles() {
    Folder folder = new Folder("Documents");
    File file1 = new File("file1.txt", 50);
    File file2 = new File("file2.txt", 30);

    folder.add(file1);
    folder.add(file2);

    assertEquals("Documents", folder.getName());
    assertEquals(80, folder.getSize());
  }

  @Test
  public void testNestedFolders() {
    Folder root = new Folder("root");
    Folder subfolder = new Folder("subfolder");
    
    File file1 = new File("file1.txt", 100);
    File file2 = new File("file2.txt", 200);
    File file3 = new File("file3.txt", 150);

    subfolder.add(file2);
    subfolder.add(file3);
    
    root.add(file1);
    root.add(subfolder);

    assertEquals("root", root.getName());
    assertEquals(450, root.getSize());
  }

  @Test
  public void testRemoveComponent() {
    Folder folder = new Folder("Downloads");
    File file1 = new File("image.jpg", 500);
    File file2 = new File("video.mp4", 5000);

    folder.add(file1);
    folder.add(file2);
    assertEquals(5500, folder.getSize());

    folder.remove(file2);
    assertEquals(500, folder.getSize());
  }
}
