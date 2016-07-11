/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package music.com.example.liuzhe.music.util;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.LruCache;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import music.com.example.liuzhe.music.MusicApplication;

/**
 * Implements a basic cache of album arts, with async loading support.
 */
public final class AlbumArtCache {
    private static final String TAG = "ArtCache";

    private static final int MAX_ALBUM_ART_CACHE_SIZE = 12 * 1024 * 1024;  // 12 MB
    private static final int MAX_ART_WIDTH = 800;  // pixels
    private static final int MAX_ART_HEIGHT = 480;  // pixels
    // Max read limit that we allow our input stream to mark/reset.
    private static final int MAX_READ_LIMIT_PER_IMG = 1024 * 1024;

    // Resolution reasonable for carrying around as an icon (generally in
    // MediaDescription.getIconBitmap). This should not be bigger than necessary, because
    // the MediaDescription object should be lightweight. If you set it too high and try to
    // serialize the MediaDescription, you may get FAILED BINDER TRANSACTION errors.
    private static final int MAX_ART_WIDTH_ICON = 128;  // pixels
    private static final int MAX_ART_HEIGHT_ICON = 128;  // pixels

    private static final int BIG_BITMAP_INDEX = 0;
    private static final int ICON_BITMAP_INDEX = 1;

    private final LruCache<String, Bitmap[]> mCache;

    private static final AlbumArtCache sInstance = new AlbumArtCache();

    public static AlbumArtCache getInstance() {
        return sInstance;
    }

    private static Set<String> tasks;

    private AlbumArtCache() {
        // Holds no more than MAX_ALBUM_ART_CACHE_SIZE bytes, bounded by maxmemory/4 and
        // Integer.MAX_VALUE:
        int maxSize = Math.min(MAX_ALBUM_ART_CACHE_SIZE,
                (int) (Math.min(Integer.MAX_VALUE, Runtime.getRuntime().maxMemory() / 4)));
        mCache = new LruCache<String, Bitmap[]>(maxSize) {
            @Override
            protected int sizeOf(String key, Bitmap[] value) {
                return value[BIG_BITMAP_INDEX].getByteCount()
                        + value[ICON_BITMAP_INDEX].getByteCount();
            }
        };
        tasks = new HashSet<>();
    }

    public Bitmap getBigImage(String artUrl) {
        Bitmap[] result = mCache.get(artUrl);
        return result == null ? null : result[BIG_BITMAP_INDEX];
    }

    public Bitmap getIconImage(String artUrl) {
        Bitmap[] result = mCache.get(artUrl);
        return result == null ? null : result[ICON_BITMAP_INDEX];
    }

    public void fetch(final String artUrl, final FetchListener listener) {
        // WARNING: for the sake of simplicity, simultaneous multi-thread fetch requests
        // are not handled properly: they may cause redundant costly operations, like HTTP
        // requests and bitmap rescales. For production-level apps, we recommend you use
        // a proper image loading library, like Glide.
        Bitmap[] bitmap = mCache.get(artUrl);
        //检查是否已经缓存，如已经缓存，则直接返回
        if (bitmap != null) {
            Log.i(TAG, "getOrFetch: album art is in cache, using it" + artUrl);
            listener.onFetched(artUrl, bitmap[BIG_BITMAP_INDEX], bitmap[ICON_BITMAP_INDEX]);
            return;
        }
        if(!tasks.contains(artUrl)) {
            tasks.add(artUrl);
            StorageReference reference = MusicApplication.getStorageReference().child("DiscoArtistImage/" + artUrl + ".jpg");
            final long ONE_MEGABYTE = 100 * 1024;  // 100kb

            Log.i(TAG, "storage reef is :" + reference.getPath());
            Log.i(TAG, "getOrFetch: starting asynctask to fetch " + artUrl);
            reference.getBytes(ONE_MEGABYTE).addOnCompleteListener(new OnCompleteListener<byte[]>() {
                @Override
                public void onComplete(@NonNull Task<byte[]> task) {
                    if (task.isSuccessful()) {
                        Bitmap[] bitmaps;//保存相应的bitmap和icon
                        BufferedInputStream is = null;
                        is = new BufferedInputStream(new ByteArrayInputStream(task.getResult()));
                        is.mark(MAX_READ_LIMIT_PER_IMG);
                        int scaleFactor = BitmapHelper.findScaleFactor(MAX_ART_WIDTH, MAX_ART_HEIGHT, is);
                        Log.i(TAG, "Scaling bitmap " + artUrl + " by factor " + scaleFactor + " to support " +
                                MAX_ART_HEIGHT + "x" + MAX_ART_HEIGHT + "requested dimension");
                        try {
                            is.reset();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Bitmap bitmap = BitmapHelper.scaleBitmap(scaleFactor, is);
                        Bitmap icon = BitmapHelper.scaleBitmap(bitmap,
                                MAX_ART_WIDTH_ICON, MAX_ART_HEIGHT_ICON);
                        bitmaps = new Bitmap[]{bitmap, icon};
                        mCache.put(artUrl, bitmaps);

                        Log.i(TAG, "doInBackground: putting bitmap in cache. cache size=" +
                                mCache.size());

                        Log.i(TAG, "keys od cache is :" + mCache.hitCount());

                        listener.onFetched(artUrl,
                                bitmaps[BIG_BITMAP_INDEX], bitmaps[ICON_BITMAP_INDEX]);
                    } else {
                        Log.e(TAG, "获取图片错误:" + task.getException().getMessage());
                    }
                }
            });
        }

//        new AsyncTask<Void, Void, Bitmap[]>() {
//            @Override
//            protected Bitmap[] doInBackground(Void[] objects) {
//                Bitmap[] bitmaps;
//                try {
//                    Bitmap bitmap = BitmapHelper.fetchAndRescaleBitmap(artUrl,
//                        MAX_ART_WIDTH, MAX_ART_HEIGHT);
//                    Bitmap icon = BitmapHelper.scaleBitmap(bitmap,
//                        MAX_ART_WIDTH_ICON, MAX_ART_HEIGHT_ICON);
//                    bitmaps = new Bitmap[] {bitmap, icon};
//                    mCache.put(artUrl, bitmaps);
//                } catch (IOException e) {
//                    return null;
//                }
//                Log.d(TAG, "doInBackground: putting bitmap in cache. cache size=" +
//                    mCache.size());
//                return bitmaps;
//            }
//
//            @Override
//            protected void onPostExecute(Bitmap[] bitmaps) {
//                if (bitmaps == null) {
//                    listener.onError(artUrl, new IllegalArgumentException("got null bitmaps"));
//                } else {
//                    listener.onFetched(artUrl,
//                        bitmaps[BIG_BITMAP_INDEX], bitmaps[ICON_BITMAP_INDEX]);
//                }
//            }
//        }.execute();
    }

    public static abstract class FetchListener {
        public abstract void onFetched(String artUrl, Bitmap bigImage, Bitmap iconImage);

        public void onError(String artUrl, Exception e) {
            Log.e(TAG, "AlbumArtFetchListener: error while downloading " + artUrl);
        }
    }
}
