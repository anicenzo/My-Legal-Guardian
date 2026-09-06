import java.io.*;
import java.util.zip.*;

public class CheckElfAlignment {
    public static void main(String[] args) throws Exception {
        ZipFile zip = new ZipFile("app/build/outputs/bundle/release/app-release.aab");
        var entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.getName().endsWith(".so") && entry.getName().contains("arm64-v8a")) {
                InputStream is = zip.getInputStream(entry);
                byte[] bytes = is.readAllBytes();
                is.close();

                // Parse 64-bit ELF header
                // bytes[0..3] = 0x7F, 'E', 'L', 'F'
                // bytes[4] = 2 (64-bit)
                // e_phoff is at offset 32 (8 bytes, little endian)
                // e_phentsize is at offset 54 (2 bytes)
                // e_phnum is at offset 56 (2 bytes)
                long phoff = getLongLE(bytes, 32);
                int phentsize = getShortLE(bytes, 54);
                int phnum = getShortLE(bytes, 56);

                long minAlign = Long.MAX_VALUE;
                long maxAlign = 0;

                for (int i = 0; i < phnum; i++) {
                    int offset = (int) (phoff + i * phentsize);
                    int p_type = getIntLE(bytes, offset);
                    if (p_type == 1) { // PT_LOAD
                        long p_align = getLongLE(bytes, offset + 48);
                        if (p_align < minAlign) minAlign = p_align;
                        if (p_align > maxAlign) maxAlign = p_align;
                    }
                }

                System.out.printf("%-50s PT_LOAD align: min=%d (0x%X), max=%d (0x%X) -> %s\n",
                    entry.getName(), minAlign, minAlign, maxAlign, maxAlign,
                    (minAlign >= 16384 ? "16 KB OK" : "4 KB FAIL!"));
            }
        }
        zip.close();
    }

    static int getShortLE(byte[] b, int off) {
        return (b[off] & 0xFF) | ((b[off + 1] & 0xFF) << 8);
    }
    static int getIntLE(byte[] b, int off) {
        return (b[off] & 0xFF) | ((b[off + 1] & 0xFF) << 8) | ((b[off + 2] & 0xFF) << 16) | ((b[off + 3] & 0xFF) << 24);
    }
    static long getLongLE(byte[] b, int off) {
        long res = 0;
        for (int i = 0; i < 8; i++) {
            res |= ((long) (b[off + i] & 0xFF)) << (8 * i);
        }
        return res;
    }
}
