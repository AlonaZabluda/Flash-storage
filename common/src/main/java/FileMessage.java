import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileMessage extends AbstractMessage{

    private String name;
    private byte [] data;


    public String getName() {
        return name;
    }

    public byte[] getData() {
        return data;
    }


    public FileMessage(Path path) throws IOException {
        name = path.getFileName().toString();
        data = Files.readAllBytes(path);
    }
}
