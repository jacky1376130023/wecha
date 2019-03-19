package com.sk.weichat.ui.tool;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/8/18.
 */

public class HtmlFactory {

    // 单例
    private static HtmlFactory sington = null;

    private HtmlFactory() {
    }

    public static HtmlFactory instance() {
        if (sington == null) {
            synchronized (HtmlFactory.class) {
                if (sington == null) {
                    sington = new HtmlFactory();
                }
            }
        }
        return sington;
    }

    private List<String> datas = new ArrayList<>();
    private DataListener mListener;

    private Handler mHandler = new Handler() {

        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            switch (msg.what) {
                case -1:
                    mListener.onError("错误");
                    break;
                case 0:
                    datas.clear();
                    break;
                case 200:
                    datas.add((String) msg.obj);
                    break;
                case 401:
                    mListener.onResponse(datas, (String) msg.obj);
                    break;
            }
        }
    };

    public void queryImage(final String url, DataListener listener) {
        mListener = listener;
        new Thread(new Runnable() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(0); // 准备
                try {
                    Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (Linux; U; Android 4.0.3; zh-cn; M032 Build/IML74K) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30")
                            .timeout(6000).get();

                    Elements imgs = doc.select("img[src]");
                    for (Element img : imgs) {
                        // 开始填装数据
                        String url = img.attr("src");
                        Log.e("xuan", "queryImage: " + url);
                        Message msg = new Message();
                        msg.what = 200;
                        msg.obj = url;
                        mHandler.sendMessage(msg);
                    }

                    Message message = new Message();
                    message.what = 401;
                    message.obj = doc.title();
                    mHandler.sendMessage(message); // 结束，将标题也传过去
                } catch (IOException e) {
                    e.printStackTrace();
                    mHandler.sendEmptyMessage(-1); // 异常
                }
            }
        }).start();
    }

    public interface DataListener<T> {
        void onResponse(List<T> datas, String title);

        void onError(String err);
    }
}
