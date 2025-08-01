package com.cityconnect.app.repository;

import com.cityconnect.app.model.Idea;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdeaRepository extends JpaRepository<Idea, Long> {}
