package com.example.team1ca;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DisplayImageAsyncTask extends AsyncTask<String, Bitmap, Void> {
    ICallback callback;

    public DisplayImageAsyncTask(ICallback callback) {
        this.callback = callback;
    }

    @Override
    protected Void doInBackground(String... urls) {

        try {
            List<String> imgSrcList = extractImgSrcList(urls[0]);

            downloadImage(imgSrcList);

            if (imgSrcList.size() < MainActivity.imageViewId.length) {
                throw new Exception("Images of given website is not enough");
            }

            return null;

        } catch (Exception e) {
            return null;
        }
    }

    public void onProgressUpdate(Bitmap... bitmaps) {
        if (this.callback != null) {
            this.callback.onCompleted(bitmaps[0]);
        }
    }

    private List<String> extractImgSrcList(String urlString) throws IOException {

        URL url= new URL(urlString);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        int responseCode = con.getResponseCode();

        List<String> imgSrcList = new ArrayList<>();
        if(responseCode == 200) {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                StringBuffer imgUrl = new StringBuffer();
                if (inputLine.contains("img src=") && inputLine.contains(".jpg")) {
                    String imgSrcString = inputLine.substring(inputLine.indexOf("img src=") + 9, inputLine.indexOf(".jpg") + 4);
                    imgSrcList.add(imgSrcString);
                }
            }
            in.close();
        }

        return imgSrcList;
    }

    private void downloadImage(List<String> imgSrcList) throws IOException {

        for (int i = 0; i < MainActivity.imageViewId.length; i++) {

            if(this.isCancelled()) {
                return;
            }

            URL url = new URL(imgSrcList.get(i));
            Bitmap bitmap = BitmapFactory.decodeStream(new BufferedInputStream(url.openStream()));

            publishProgress(bitmap);

            // sleep for 1 seconds for demo
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    public interface ICallback {
        void onCompleted(Bitmap bitmap);
    }

}
