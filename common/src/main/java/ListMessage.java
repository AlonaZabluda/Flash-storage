import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class ListMessage extends AbstractMessage {

    private List<FileManager> filesList;

    public List<FileManager> getFilesList() {
        return filesList;
    }

    public void createList(Path path) throws IOException {
        filesList = Files.list(path)
                .map(FileManager::new)
                .collect(Collectors.toList());
    }

    public void createList(Path path, String nick, List<String> list) throws IOException {
            filesList = Files.list(path)
                    .map(FileManager::new)
                    .filter(o -> o.getFileName().equals(nick) || list.contains(o.getFileName()))
                    .collect(Collectors.toList());

    }

}
