package com.asura.library.posters;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class RemoteVideo extends VideoPoster implements Parcelable {
    private Uri uri;
    String urlDetail;
    String name;

    public RemoteVideo(Uri uri, String urlDetail, String name) {
        this.uri = uri;
        this.urlDetail = urlDetail;
        this.name = name;
    }

    public Uri getUri() {
        return uri;
    }

    public String getName() {
        return name;
    }

    public String getUrlDetail() {
        return urlDetail;
    }

    public RemoteVideo(Parcel in){
        uri = in.readParcelable(Uri.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(uri,flags);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<RemoteVideo> CREATOR = new Creator<RemoteVideo>() {
        @Override
        public RemoteVideo createFromParcel(Parcel in) {
            return new RemoteVideo(in);
        }

        @Override
        public RemoteVideo[] newArray(int size) {
            return new RemoteVideo[size];
        }
    };
}
