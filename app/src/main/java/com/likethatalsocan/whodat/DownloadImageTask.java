package com.likethatalsocan.whodat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.URL;

/**
 * Created by sathy on 27/12/17.
 */

public class DownloadImageTask extends AsyncTask<String,Void,Bitmap> {

    ImageView imageView;

    public DownloadImageTask(ImageView imageView){
        this.imageView = imageView;
    }

    protected Bitmap doInBackground(String...urls){
        String urlOfImage = urls[0];
        if (urlOfImage == null ) return null;

        Bitmap logo = null;
        try{
            InputStream is = new URL(urlOfImage).openStream();
            logo = BitmapFactory.decodeStream(is);
        }catch(Exception e){
            e.printStackTrace();
        }
        return logo;
    }

    protected void onPostExecute(Bitmap result){
        if (result !=null) imageView.setImageBitmap(result);
        else imageView.setImageResource(R.drawable.profilepic);
    }
}
