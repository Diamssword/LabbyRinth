package com.diamssword.labbyrinth.utils;

import org.apache.commons.io.FileUtils;

import java.lang.management.ManagementFactory;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TextUtils {
    public static String capitalizeWords(final String words) {
        return Stream.of(words.trim().split("\\s"))
                .filter(word -> word.length() > 0)
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                .collect(Collectors.joining(" "));
    }
    public static int getRam()
    {
        com.sun.management.OperatingSystemMXBean mxbean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        String m= FileUtils.byteCountToDisplaySize(mxbean.getTotalMemorySize());
        return Integer.parseInt(m.split(" ")[0])-1;
    }
}
