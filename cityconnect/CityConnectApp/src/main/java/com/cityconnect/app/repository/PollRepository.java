package com.cityconnect.app.repository;

import com.cityconnect.app.model.Poll;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PollRepository extends JpaRepository<Poll, Long> {}
