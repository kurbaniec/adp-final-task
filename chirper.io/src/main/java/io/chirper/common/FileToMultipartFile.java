package io.chirper.common;

/**
 * @author Kacper Urbaniec
 * @version 2024-03-12
 */
import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Files;

public class FileToMultipartFile implements MultipartFile {
    private final File file;
    private static final Tika tika = new Tika();

    public FileToMultipartFile(File file) {
        this.file = file;
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public String getOriginalFilename() {
        return file.getName();
    }

    @Override
    public String getContentType() {
        try {
            return tika.detect(file);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public boolean isEmpty() {
        return file.length() == 0L;
    }

    @Override
    public long getSize() {
        return file.length();
    }

    @Override
    public byte[] getBytes() throws IOException {
        return Files.readAllBytes(file.toPath());
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        file.renameTo(dest);
    }
}

