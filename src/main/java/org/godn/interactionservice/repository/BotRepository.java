package org.godn.interactionservice.repository;

import org.godn.interactionservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BotRepository extends JpaRepository<User, UUID> {
}
