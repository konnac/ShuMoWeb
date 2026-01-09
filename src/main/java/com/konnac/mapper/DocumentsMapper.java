package com.konnac.mapper;

import com.konnac.pojo.Document;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface DocumentsMapper {
    //添加文档记录
    void addDocument(Document document);
    //修改文档记录
    void updateDocument(Document document);
    //删除文档记录
    void deleteDocument(Integer id);
    //根据id查询文档记录
    Document getDocumentById(Integer id);
    //根据项目id删除文档记录
    void deleteByProjectId(Integer projectId);
    //统计指定项目下的文档数量
    long countByProjectId(Integer projectId);

    //分页条件查询
    List<Document> list(@Param("projectId") Integer projectId,
                        @Param("fileType") String fileType,
                        @Param("category") Document.DocumentCategory category,
                        @Param("fileName") String fileName,
                        @Param("begin") LocalDate begin,
                        @Param("end") LocalDate end);
}
