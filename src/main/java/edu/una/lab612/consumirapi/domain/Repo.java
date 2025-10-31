package edu.una.lab612.consumirapi.domain;


import com.fasterxml.jackson.annotation.JsonProperty;

public class Repo {
    public long id;
    public String name;
    public String description;
    public String language;
    @JsonProperty("stargazers_count")
    public int stargazersCount;
    @JsonProperty("forks_count")
    public int forksCount;
    @JsonProperty("updated_at")
    public String updatedAt;
    @JsonProperty("html_url")
    public String htmlUrl;
}
