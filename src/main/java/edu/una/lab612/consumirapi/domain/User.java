
package edu.una.lab612.consumirapi.domain;


import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
    public String login;
    public String name;
    public String bio;
    public String location;
    public String blog;
    @JsonProperty("avatar_url")
    public String avatarUrl;
    @JsonProperty("created_at")
    public String createdAt;
    @JsonProperty("public_repos")
    public int publicRepos;
    public int followers;
    public int following;
}
