package com.moviebackend.dto;

import javax.validation.constraints.NotBlank;

/**
 * Favorite add/remove request DTO
 */
public class FavoriteRequest {

    @NotBlank(message = "电影ID不能为空")
    private String movieId;

    @NotBlank(message = "电影标题不能为空")
    private String movieTitle;

    private String moviePoster;

    public FavoriteRequest() {}

    public FavoriteRequest(String movieId, String movieTitle, String moviePoster) {
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.moviePoster = moviePoster;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getMoviePoster() {
        return moviePoster;
    }

    public void setMoviePoster(String moviePoster) {
        this.moviePoster = moviePoster;
    }
}
