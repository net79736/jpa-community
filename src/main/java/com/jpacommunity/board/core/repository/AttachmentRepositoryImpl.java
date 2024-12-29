package com.jpacommunity.board.core.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AttachmentRepositoryImpl {

    private final AttachmentJpaRepository attachmentJpaRepository;

}