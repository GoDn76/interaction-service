package org.godn.interactionservice.repository;

import org.godn.interactionservice.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

}
