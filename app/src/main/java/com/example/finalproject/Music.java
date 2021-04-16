package com.example.finalproject;

public class Music {
    int index_;
    String songTitle;
    String songArtist;
    String songUrl;
    String imgUrl;

    public String getSongTitles() {
        return songTitle;
    }
    public void setSongTitles(String songTitle) {
        this.songTitle = songTitle;
    }

    public String getSongArtist() {
        return songArtist;
    }
    public void setSongArtist(String songArtist) {
        this.songArtist = songArtist;
    }

    public String getSongUrl() {
        return songUrl;
    }
    public void setSongUrl(String songUrl) {
        this.songUrl = songUrl;
    }

    public String getImgUrl() {
        return imgUrl;
    }
    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public int getIndex_()  {
        return index_;
    }

    public Music(int index_,String songTitle,String songArtist,String songUrl,String imgUrl) {
        super();
        this.index_ = index_;
        this.songTitle = songTitle;
        this.songArtist = songArtist;
        this.songUrl = songUrl;
        this.imgUrl = imgUrl;
    }
}
