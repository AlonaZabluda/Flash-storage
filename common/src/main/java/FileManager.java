import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileManager implements Serializable {
    private String fileName;
    private String directoryName;
    private long size;
    private FileType type;
    private String path;

    public enum FileType {
        FILE("F"), DIRECTORY("D");
        private String type;

        public String getType() {
            return type;
        }

        FileType(String type) {
            this.type = type;
        }
    }

    public String getFileName() {
        return fileName;
    }

    public long getSize() {
        return size;
    }

    public FileType getType() {
        return type;
    }

    public String getDirectoryName() {
        return directoryName;
    }

    public String getPath() {
        return path;
    }

    public FileManager(Path path) {
        try {
            this.fileName = path.getFileName().toString();
            this.size = Files.size(path);
            this.type = Files.isDirectory(path) ? FileType.DIRECTORY : FileType.FILE;
            if (this.type == FileType.DIRECTORY) {
                this.directoryName = path.getFileName().toString();
                this.size = -1L;
            }
            this.path = path.normalize().toString();

        } catch (IOException e) {
            throw new RuntimeException("Unable to create file info from path");
        }

    }
}
