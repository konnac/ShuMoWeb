package com.konnac.mapper;

import com.konnac.pojo.Document;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface DocumentsMapper {

    void addDocument(Document document);

    void updateDocument(Document document);

    void deleteDocument(Integer id);

    Document getDocumentById(Integer id);

    List<Document> listByProjectId(@Param("projectId") Integer projectId,
                                    @Param("category") String category,
                                    @Param("fileName") String fileName);

    @Select("select * from documents where project_id = #{projectId}")
    List<Document> listAllByProjectId(Integer projectId);

    void deleteByProjectId(Integer projectId);

    long countByProjectId(Integer projectId);

    List<Document> list(@Param("projectId") Integer projectId,
                        @Param("fileType") String fileType,
                        @Param("category") Document.DocumentCategory category,
                        @Param("fileName") String fileName,
                        @Param("begin") LocalDate begin,
                        @Param("end") LocalDate end);
}
