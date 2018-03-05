package com.example.adima.familyalbumproject.Album.Model;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.example.adima.familyalbumproject.Entities.Image;
import com.example.adima.familyalbumproject.ImageUrl.Model.ImageFirebase;
import com.example.adima.familyalbumproject.MyApplication;

import java.util.List;

/**
 * Created by adima on 05/03/2018.
 */

public class ImageRepository {


    public static final ImageRepository instance = new ImageRepository();

    ImageRepository() {
    }
    MutableLiveData<List<Image>> imagesListliveData;

    public LiveData<List<Image>> getImagesList(String albumId) {
        synchronized (this) {
            if (imagesListliveData == null) {
                Log.d("TAG", "images live data is null");

                imagesListliveData = new MutableLiveData<List<Image>>();

                ImageFirebase.getAllImagesAndObserve(albumId,new ImageFirebase.Callback<List<Image>>() {
                    @Override
                    public void onComplete(List<Image> data) {

                        if (data != null) imagesListliveData.setValue(data);
                        Log.d("TAG", "got comments data");

                    }
                });


            }
        }
        return imagesListliveData;
    }

        public LiveData<List<Image>> getAllImages(final String albumId) {
            synchronized (this) {
                if (imagesListliveData == null) {
                    Log.d("TAG", "Live data is null");
                    imagesListliveData = new MutableLiveData<List<Image>>();

                    //1. get the last update date
                    long lastUpdateDate = 0;
                    try {
                        lastUpdateDate = MyApplication.getMyContext()
                                .getSharedPreferences("TAG", Context.MODE_PRIVATE).getLong("lastUpdateDate", 0);
                    } catch (Exception e) {

                    }


                    ImageFirebase.getAllImagesAndObserve(albumId, lastUpdateDate, new ImageFirebase.Callback<List<Image>>() {
                        @Override
                        public void onComplete(List<Image> data) {
                            updateImageDataInLocalStorage(data, albumId);
                        }
                    });
                }
                return imagesListliveData;
            }
        }

    private void updateImageDataInLocalStorage(List<Image> data,String albumId) {
        Log.d("TAG", "got items from firebase: " + data.size());
        ImageRepository.MyTask task = new ImageRepository.MyTask();

        task.setAlbumId(albumId);

        task.execute(data);
    }

    class MyTask extends AsyncTask<List<Image>,String,List<Image>> {
        private String albumId;

        public void setAlbumId(String albumId) {
            this.albumId = albumId;
        }
        @Override
        protected List<Image> doInBackground(List<Image>[] lists) {
            Log.d("TAG","starting updateAlbumDataInLocalStorage in thread");
            if (lists.length > 0) {
                List<Image> data = lists[0];
                long lastUpdateDate = 0;
                try {
                    lastUpdateDate = MyApplication.getMyContext()
                            .getSharedPreferences("TAG", Context.MODE_PRIVATE).getLong("lastUpdateDate", 0);

                    Log.d("Tag","got the last update date");
                }catch (Exception e){
                    Log.d("Tag","in the exception");


                }
                if (data != null && data.size() > 0) {
                    //3. update the local DB
                    long reacentUpdate = lastUpdateDate;

                    for (Image image : data) {


                        AppLocalStore.db.imageDao().insertAll(image);
                        Log.d("Tag","after insert all");

                        if (image.getLastUpdated() > reacentUpdate) {
                            reacentUpdate = image.getLastUpdated();
                        }
                        Log.d("TAG", "updating: " + image.toString());
                    }
                    SharedPreferences.Editor editor = MyApplication.getMyContext().getSharedPreferences("TAG", Context.MODE_PRIVATE).edit();
                    editor.putLong("lastUpdateDate", reacentUpdate);
                    editor.commit();
                }
                //return the complete student list to the caller
                List<Image> imagesList = AppLocalStore.db.imageDao().loadAllByIds(albumId);
                Log.d("TAG","finish updateEmployeeDataInLocalStorage in thread");

                return imagesList;
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Image> images) {
            super.onPostExecute(images);
            imagesListliveData.setValue(images);
            Log.d("TAG","update updateImageDataInLocalStorage in main thread");
            Log.d("TAG", "got items from local db: " + images.size());

        }
    }
}