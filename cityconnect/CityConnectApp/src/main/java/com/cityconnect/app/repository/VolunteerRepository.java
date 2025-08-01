package com.cityconnect.app.repository;

import com.cityconnect.app.model.Volunteer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VolunteerRepository extends JpaRepository<Volunteer, Long> {}
