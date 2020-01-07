package com.concourse.models.posts;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Post {

    @Id
    private String id;
    private String courseId;
    private Long postDate;
    private Integer likeCount;

    @Lob
    private String content;

    private String authorUserId;
    private String authorName;
    private String authorType;
}
