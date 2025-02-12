package cn.wantu.uumusic.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class AudioFormatChecker {

    public enum AudioFormat {
        MP3,
        FLAC,
        WAV,
        OGG,
        MP4_M4A,
        AAC_ADTS,
        UNKNOWN
    }

    public static AudioFormat getAudioFormat(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return AudioFormat.UNKNOWN;
        }

        // 读取前 16 个字节（大部分情况下足够）
        byte[] header = new byte[16];
        try (FileInputStream fis = new FileInputStream(file)) {
            int bytesRead = fis.read(header, 0, 16);
            if (bytesRead < 4) {
                return AudioFormat.UNKNOWN;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return AudioFormat.UNKNOWN;
        }

        // 1. ID3 (MP3, ID3v2)
        if (header[0] == 'I' && header[1] == 'D' && header[2] == '3') {
            return AudioFormat.MP3;
        }

        // 2. fLaC (FLAC)
        if (header[0] == 'f' && header[1] == 'L' && header[2] == 'a' && header[3] == 'C') {
            return AudioFormat.FLAC;
        }

        // 3. RIFF....WAVE (WAV)
        // "RIFF" = 52 49 46 46; "WAVE" = 57 41 56 45
        if (header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F') {
            // 再检查一下后面是否包含 WAVE
            // bytes [8..11] 可能是 "WAVE"
            // 当然也可以进一步解析 chunk，但这里先做简单判断
            if (header[8] == 'W' && header[9] == 'A' && header[10] == 'V' && header[11] == 'E') {
                return AudioFormat.WAV;
            }
        }

        // 4. OggS (OGG)
        if (header[0] == 'O' && header[1] == 'g' && header[2] == 'g' && header[3] == 'S') {
            return AudioFormat.OGG;
        }

        // 5. ftyp (MP4 / M4A 等)
        // 前四字节可能是 "...." 但在第 4~8 字节会出现 ftyp
        // 这里简单做个搜索
        String headerStr = new String(header, StandardCharsets.US_ASCII);
        if (headerStr.contains("ftyp")) {
            return AudioFormat.MP4_M4A;
        }

        // 6. ADTS (AAC)
        // FF F1 or FF F9
        if ((header[0] & 0xFF) == 0xFF
                && ((header[1] & 0xF6) == 0xF0)) {
            // ADTS 帧同步判断 (0xFFFx)
            // 如果需要更精确可以继续解析
            return AudioFormat.AAC_ADTS;
        }

        // 7. 无法识别则返回 UNKNOWN
        return AudioFormat.UNKNOWN;
    }
}