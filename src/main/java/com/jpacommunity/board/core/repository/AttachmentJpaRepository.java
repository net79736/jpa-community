package com.jpacommunity.board.core.repository;


import com.jpacommunity.board.core.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentJpaRepository extends JpaRepository<Attachment, Long> {
}
