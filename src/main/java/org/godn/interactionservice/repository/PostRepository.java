package org.godn.interactionservice.repository;

import org.godn.interactionservice.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {
    // Just fetch the UUID!
    @Query("SELECT p.authorId FROM Post p WHERE p.id = :id")
    UUID findAuthorIdById(@Param("id") UUID id);
}
