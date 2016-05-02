package com.googlecode.usc.folder.compression;

import java.io.File;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;

import com.googlecode.usc.folder.compression.CompressionType;
import com.googlecode.usc.folder.compression.Context;
import com.googlecode.usc.folder.compression.StrategyFactory;
import com.googlecode.usc.folder.compression.utils.CompressionUtil;
import com.googlecode.usc.folder.compression.utils.PrintTimerTask;

/**
 *
 * @author ShunLi
 */
public class App1 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("请输入文件夹路径：");
        String folderPath = scanner.next();

        CompressionType[] compressionTypes = CompressionType.values();

        System.out.print("请选择压缩类型");

        for (int i = 0; i < compressionTypes.length; i++) {
            System.out.print("\n" + (i + 1) + "). " + compressionTypes[i]);
            if (i == 0) {
                System.out.print("(默认)");
            }
        }
        System.out.print("\n(选择数字)：");

        int choose = scanner.nextInt();
        if (choose < 1 || choose > compressionTypes.length) {
            choose = 0;
        } else {
            choose--;
        }

        CompressionType compressionType = compressionTypes[choose];

        System.out.print("是否按照默认的过滤规则(" + CompressionUtil.DEFAULT_EXCLUDED_WORDS + ")来压缩？(Y/N)：");

        boolean isUseDefault = CompressionUtil.isYse(scanner.next());

        String excludedWords = "";

        String isAgain = null;
        if (isUseDefault) {
            excludedWords = CompressionUtil.DEFAULT_EXCLUDED_WORDS;

            System.out.print("是否还添加过滤规则？(Y/N)：");
            isAgain = scanner.next();
        }

        if (!isUseDefault || CompressionUtil.isYse(isAgain)) {
            System.out.print("请添加过滤规则，多个过滤词以\"" + CompressionUtil.SPILT_CHAR + "\"分割：");
            scanner.nextLine();
            excludedWords = excludedWords + (!"".equals(excludedWords) ? CompressionUtil.SPILT_CHAR : "") + scanner.nextLine(); //
        }

        // System.out.println(excludedWords);

        List<String> excludedKeys = CompressionUtil.initExcludedKeys(excludedWords);

        File file = new File(folderPath);
        File[] files = file.listFiles();

        if (files != null) {
            File out = new File(file.getAbsolutePath() + compressionType.getSuffix());

            StrategyFactory sf = new StrategyFactory();
            sf.createStrategy(compressionType);

            Timer timer = new Timer();
            timer.schedule(new PrintTimerTask(), 0, 1000);

            System.out.println("压缩开始，请等待");
            new Context(sf.createStrategy(compressionType)).doCompress(files, out, excludedKeys);

            timer.cancel();
            System.out.println("\n压缩成功，请查看 " + out);
        } else {
            System.err.println("文件夹不存在或者输入的是文件!");
            System.exit(1);
        }

    }

}

