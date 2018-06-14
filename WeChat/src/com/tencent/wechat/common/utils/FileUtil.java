package com.tencent.wechat.common.utils;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by qxb-810 on 2016/12/23.
 */
public class FileUtil {


    public static void copyAssets(Context context, String srcPath, String desPath) {

        AssetManager assetManager = context.getAssets();

        File outFile = new File(desPath);
        if (!outFile.getParentFile().exists()) {// 父目录不存在，则创建
            outFile.getParentFile().mkdirs();
        }

        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(srcPath);
            out = new FileOutputStream(outFile);
            copyStream(in, out);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    /**
     * 解压函数
     *
     * @param sourceFile 压缩包所在路径
     * @param destDir    解压目的路径
     * @return 解压成功, 返回true 解压失败,返回false
     */
    public static boolean unZip(String sourceFile, String destDir) {
        OutputStream outStream = null;
        BufferedOutputStream bufOutStream = null;
        InputStream inputStram = null;
        destDir = destDir.endsWith(File.separator) ? destDir : (destDir + File.separator);
        byte b[] = new byte[1024];
        int length;
        ZipFile readZipFile = null;

        try {
            readZipFile = new ZipFile(new File(sourceFile));
            Enumeration enumeration = readZipFile.entries();
            ZipEntry zipEntry = null;

            while (enumeration.hasMoreElements()) {
                zipEntry = (ZipEntry) enumeration.nextElement();
                File outPutFile = new File(destDir + zipEntry.getName());

                if (zipEntry.isDirectory()) {// 当前条目是目录
                    outPutFile.mkdirs();
                } else {// 当前条目是文件

                    if (!outPutFile.getParentFile().exists()) {// 父目录不存在，则创建
                        outPutFile.getParentFile().mkdirs();
                    }

                    outStream = new FileOutputStream(outPutFile);
                    bufOutStream = new BufferedOutputStream(outStream);
                    inputStram = readZipFile.getInputStream(zipEntry);

                    // 解压文件
                    while ((length = inputStram.read(b)) > 0) {
                        bufOutStream.write(b, 0, length);
                    }
                    bufOutStream.flush();

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                if (outStream != null) {
                    outStream.close();
                }
                if (bufOutStream != null) {
                    bufOutStream.close();
                }
                if (inputStram != null) {
                    inputStram.close();
                }
                if (readZipFile != null) {
                    readZipFile.close();
                }
            } catch (IOException e1) {
                e.printStackTrace();
            }
            return false;
        } finally {
            try {
                if (outStream != null) {
                    outStream.close();
                }
                if (bufOutStream != null) {
                    bufOutStream.close();
                }
                if (inputStram != null) {
                    inputStram.close();
                }
                if (readZipFile != null) {
                    readZipFile.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }


    public static void writeToFile(String path, String content) {
        File outFile = new File(path);
        if (!outFile.getParentFile().exists()) {
            outFile.getParentFile().mkdirs();
        }
        FileWriter fw = null;
        try {
            fw = new FileWriter(outFile);
            fw.write(content);
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}








