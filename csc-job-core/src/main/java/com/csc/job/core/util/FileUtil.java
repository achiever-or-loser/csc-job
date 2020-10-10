package com.csc.job.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @Description:
 * @PackageName: com.csc.job.core.util
 * @Author: 陈世超
 * @Create: 2020-10-10 16:29
 * @Version: 1.0
 */
public class FileUtil {
    private static Logger logger = LoggerFactory.getLogger(FileUtil.class);


    /**
     * delete recursively
     *
     * @param root
     * @return
     */
    public static boolean deleteRecursively(File root) {
        if (root != null && root.exists()) {
            if (root.isDirectory()) {
                File[] children = root.listFiles();
                if (children != null) {
                    for (File child : children) {
                        deleteRecursively(child);
                    }
                }
            }
            return root.delete();
        }
        return false;
    }


}
