package com.cityconnect.app.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.stream.Collectors;


@Entity
public class Poll {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String question;
    private String options;  // comma-separated options
    private LocalDate date;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getOptions() { return options; }
    public void setOptions(String options) { this.options = options; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PollVote> votes = new ArrayList<>();

    public List<PollVote> getVotes() {
        return votes;
    }

    public void setVotes(List<PollVote> votes) {
        this.votes = votes;
    }
    @Transient
    private List<String> optionList;

    public List<String> getOptionList() {
        if (this.options != null) {
            return Arrays.stream(this.options.split(","))
                         .map(String::trim)
                         .collect(Collectors.toList());
        }
        return List.of();
    }

    


}
