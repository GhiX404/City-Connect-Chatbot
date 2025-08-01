package com.cityconnect.app.repository;

import com.cityconnect.app.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;
import com.cityconnect.app.model.PollVote;


public interface PollVoteRepository extends JpaRepository<PollVote, Long> {
    List<PollVote> findByPollId(Long pollId);
}

