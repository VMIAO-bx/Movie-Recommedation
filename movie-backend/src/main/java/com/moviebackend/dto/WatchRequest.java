package com.moviebackend.dto;

import javax.validation.constraints.NotBlank;

/**
 * Watchlist add/update request DTO
 */
public class WatchRequest {

    @NotBlank(message = "电影ID不能为空")
    private String movieId;

    @NotBlank(message = "电影标题不能为空")
    private String movieTitle;

    private String moviePoster;

    /** Status: 想看 or 看过 */
    private String status;

    public WatchRequest() {}

    public WatchRequest(String movieId, String movieTitle, String moviePoster, String status) {
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.moviePoster = moviePoster;
        this.status = status;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
