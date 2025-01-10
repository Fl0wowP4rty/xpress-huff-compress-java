import org.junit.jupiter.api.Test;
import tech.skidonion.compress.xpresshuff.XpressHuffDecompress;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PrefetchTest {
    @Test
    void read() {
        try (InputStream stream = PrefetchTest.class.getResourceAsStream("/JAVA.EXE-DE93B7A0.pf");) {
//        try (InputStream stream = PrefetchTest.class.getResourceAsStream("/QQ.EXE-028D401B.pf");) {

            if (stream == null) return;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = stream.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            byte[] data = baos.toByteArray();
            int tl = (data[4] & 0xFF) + ((data[5] & 0xFF) << 8) + ((data[6] & 0xFF) << 16) + ((data[7] & 0xFF) << 24);
            System.out.println("Total length: " + tl);

            byte[] remaining = new byte[data.length - 8];
            System.arraycopy(data, 8, remaining, 0, remaining.length);
            ByteBuffer out;
            XpressHuffDecompress.decompress(ByteBuffer.wrap(remaining), out = ByteBuffer.allocate(tl));
            Files.write(Paths.get("ASM.txt"), out.array());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
