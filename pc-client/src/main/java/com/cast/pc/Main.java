package com.cast.pc;

import com.cast.pc.ui.MainWindow;
import javafx.application.Application;

/**
 * 电脑端投屏程序入口
 * 
 * 功能说明：
 * 1. 捕获电脑屏幕画面
 * 2. 编码为H.264视频流
 * 3. 通过网络传输到安卓TV
 */
public class Main {
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("    电脑投屏到安卓TV - 发送端 v1.0");
        System.out.println("========================================");
        
        // 启动JavaFX界面
        Application.launch(MainWindow.class, args);
    }
}
