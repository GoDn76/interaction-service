package org.godn.interactionservice.repository;

import org.godn.interactionservice.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {
    @Query("SELECT c.depthLevel FROM Comment c WHERE c.id = :id")
    Integer findDepthLevelById(@Param("id") UUID id);
}
