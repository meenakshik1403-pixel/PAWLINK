package com.pawlink.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DocumentDAO {

    private final JdbcTemplate jdbcTemplate;

    public DocumentDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveDocument(Long userId, String documentType, String fileName, String filePath, long fileSize) {
        String sql = "INSERT INTO user_documents (user_id, document_type, file_name, file_path, file_size) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, userId, documentType, fileName, filePath, fileSize);
    }

    public List<UserDocumentDTO> findByUserId(Long userId) {
        String sql = "SELECT id, user_id, document_type, file_name, file_path, file_size, uploaded_at FROM user_documents WHERE user_id = ? ORDER BY uploaded_at DESC";
        return jdbcTemplate.query(sql, rowMapper, userId);
    }

    public Optional<UserDocumentDTO> findById(Long id) {
        String sql = "SELECT id, user_id, document_type, file_name, file_path, file_size, uploaded_at FROM user_documents WHERE id = ?";
        List<UserDocumentDTO> list = jdbcTemplate.query(sql, rowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    private final RowMapper<UserDocumentDTO> rowMapper = (rs, rowNum) -> new UserDocumentDTO(
            rs.getLong("id"),
            rs.getLong("user_id"),
            rs.getString("document_type"),
            rs.getString("file_name"),
            rs.getString("file_path"),
            rs.getLong("file_size"),
            rs.getTimestamp("uploaded_at").toLocalDateTime()
    );
}

