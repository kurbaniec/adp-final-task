package io.chirper.test;

import org.springframework.http.HttpEntity;
import org.springframework.util.LinkedMultiValueMap;

/**
 * @author Kacper Urbaniec
 * @version 2024-03-09
 */
public class FileUtil {


    public static HttpEntity<byte[]> mockPngImage(String name, String body) {
        // Based on https://stackoverflow.com/a/52005690/12347616
        var headerMap = new LinkedMultiValueMap<String, String >();
        headerMap.add("Content-disposition", "form-data; name=file; filename=" + name);
        headerMap.add("Content-type", "image/png");
        return new HttpEntity<>(body.getBytes(), headerMap);
    }

}
