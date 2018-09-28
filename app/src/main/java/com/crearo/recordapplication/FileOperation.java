package com.crearo.recordapplication;

import android.annotation.SuppressLint;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by aa on 2018/6/1.
 */

public class FileOperation {

    private static final int TRY_WRITE_TIMES = 3; // 保存文件重试次数

    @SuppressLint("NewApi")
    public static boolean Buf2File(String fileName, byte[] buf, int start,int end,boolean append )
    {
        if (fileName.isEmpty()) return false;

        int try_write_times = TRY_WRITE_TIMES;
        while (try_write_times > 0)
        {
            FileOutputStream out = null;
            try
            {
                out = new FileOutputStream(new File(fileName),append);
                out.write(buf, start, end - start);
                out.flush();
                out.getFD().sync();// 确保是文件流才可以用这个方法
                try_write_times = -1;
            }
            catch (Exception e)
            {
                --try_write_times;
                if (try_write_times == 0)
                {
                    e.printStackTrace();
                    return false;
                }
            }

            try
            {
                if (out != null) out.close();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return true;
    }
}
